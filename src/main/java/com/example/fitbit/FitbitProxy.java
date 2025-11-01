package com.example.fitbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

/**
 * Fitbit API から心拍データを取得し、1分間キャッシュしたものを提供するサービス。
 * 認証済みユーザーごとにアクセストークンを解決し、WebSocket配信側から共通利用される。
 */
@Slf4j
@Service
public class FitbitProxy {

  private static final Duration CACHE_TTL = Duration.ofMinutes(1);
  private static final String CLIENT_REGISTRATION_ID = "fitbit";

  record FitBitHeartActivity(@JsonProperty("activities-heart-intraday") IntradayHeartRate intraDay) {
  }

  record IntradayHeartRate(@JsonProperty("dataset") List<OnetimeHeartRate> dataset) {
  }

  public record OnetimeHeartRate(@JsonProperty("time") LocalTime time,
                                 @JsonProperty("value") int value) {
  }

  /**
   * 直近の取得結果と取得時刻を覚えておくための簡易キャッシュ。
   */
  private record CacheEntry(List<OnetimeHeartRate> data, Instant fetchedAt) {

    boolean isFresh(Duration ttl) {
      return fetchedAt != null && Instant.now().isBefore(fetchedAt.plus(ttl));
    }
  }

  private final OAuth2AuthorizedClientManager authorizedClientManager;
  private final RestClient restClient;
  /**
   * キー: Fitbitユーザー(encodedId)。値: 直近期の心拍データ。TTL判定はCacheEntry側で行う。
   */
  private final Map<String, CacheEntry> heartRateCache = new ConcurrentHashMap<>();

  public FitbitProxy(OAuth2AuthorizedClientManager authorizedClientManager, RestClient fitbitRestClient) {
    this.authorizedClientManager = authorizedClientManager;
    this.restClient = fitbitRestClient;
  }

  /**
   * FitbitのAccessTokenはユーザー単位で管理されるため、Authenticationを受け取ってAPIを呼び出す。
   */
  public List<OnetimeHeartRate> getHeartRate(Authentication authentication) {
    if (authentication == null) {
      throw new IllegalStateException("Fitbitにログインしてから再度アクセスしてください。");
    }

    // 1. 直近キャッシュが生きていればそのまま返す。
    CacheEntry cached = heartRateCache.get(authentication.getName());
    if (cached != null && cached.isFresh(CACHE_TTL)) {
      return cached.data();
    }

    // 2. 有効なアクセストークンを確保し Fitbit API をコール。
    OAuth2AuthorizedClient authorizedClient = authorize(authentication);
    FitBitHeartActivity activity = restClient.get()
        .uri("/1/user/-/activities/heart/date/today/1d/1min.json")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
        .retrieve()
        .body(FitBitHeartActivity.class);

    // 3. 欠損を埋めた形へ整形しキャッシュへ保存。
    List<OnetimeHeartRate> normalized = normalize(activity);
    heartRateCache.put(authentication.getName(), new CacheEntry(normalized, Instant.now()));
    log.debug("heart rate data size: {}", normalized.size());
    return normalized;
  }

  /**
   * OAuth2AuthorizedClientManager を使ってアクセストークン/リフレッシュトークンを刷新する。
   */
  private OAuth2AuthorizedClient authorize(Authentication authentication) {
    OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
        .principal(authentication)
        .build();
    OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
    if (client == null) {
      throw new OAuth2AuthorizationException(
          new OAuth2Error("unauthorized_client"),
          "Fitbitのアクセストークンを取得できませんでした。"
      );
    }
    return client;
  }

  /**
   * Fitbitの生データは記録のない時間帯が抜け落ちているため、0時から最後の計測時刻まで1分刻みで埋める。
   */
  private List<OnetimeHeartRate> normalize(FitBitHeartActivity activity) {
    if (activity == null || activity.intraDay() == null
        || CollectionUtils.isEmpty(activity.intraDay().dataset())) {
      return List.of();
    }
    List<OnetimeHeartRate> dataset = activity.intraDay().dataset();
    Map<LocalTime, Integer> byTime = dataset.stream()
        .collect(Collectors.toMap(OnetimeHeartRate::time, OnetimeHeartRate::value, (left, right) -> left, java.util.LinkedHashMap::new));

    LocalTime lastTime = dataset.get(dataset.size() - 1).time();
    long minutesPassed = ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, lastTime);

    List<OnetimeHeartRate> expanded = new ArrayList<>();
    for (long minute = 0; minute <= minutesPassed; minute++) {
      LocalTime time = LocalTime.MIDNIGHT.plusMinutes(minute);
      int value = byTime.getOrDefault(time, 0);
      expanded.add(new OnetimeHeartRate(time, value));
    }
    return Collections.unmodifiableList(expanded);
  }
}
