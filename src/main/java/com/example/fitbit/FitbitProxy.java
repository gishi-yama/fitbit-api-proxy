package com.example.fitbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Log4j2
@Service
public class FitbitProxy {

  record FitBitHeartActivity(@JsonProperty("activities-heart-intraday") IntradayHeartRate intraDay) {
  }

  record IntradayHeartRate(@JsonProperty("dataset") List<OnetimeHeartRate> dataset) {
  }

  record OnetimeHeartRate(@JsonProperty("time") LocalTime time, @JsonProperty("value") int value) {
  }

  private final OAuth20Service oAuth20Service;

  private final ObjectMapper objectMapper;

  private FitBitOAuth2AccessToken token;

  private String accessCode;

  private String responseBody;

  @Autowired
  public FitbitProxy(OAuth20Service oAuth20Service, ObjectMapper objectMapper) {
    this.oAuth20Service = oAuth20Service;
    this.objectMapper = objectMapper;
  }

  public String getAuthnURL() {
    if (ObjectUtils.isEmpty(accessCode)) {
      final String authorizationUrl = oAuth20Service.getAuthorizationUrl();
      log.info(authorizationUrl);
      return authorizationUrl;
    }
    return "認証済";
  }

  public String saveCode(String code) throws IOException, ExecutionException, InterruptedException {
    this.accessCode = code;
    this.token = (FitBitOAuth2AccessToken) oAuth20Service.getAccessToken(code);
    return "認証完了";
  }

  public List<OnetimeHeartRate> getHeartRate() throws IOException, ExecutionException, InterruptedException {
    getCachedResponseBody();
    FitBitHeartActivity heartActivity = objectMapper.readValue(responseBody, FitBitHeartActivity.class);
    IntradayHeartRate intradayHeartRate = heartActivity.intraDay();
    log.info(intradayHeartRate.dataset.size());
    return intradayHeartRate.dataset;
  }

  @Cacheable("responseBody")
  public String getCachedResponseBody() throws IOException, ExecutionException, InterruptedException {
    if (ObjectUtils.isEmpty(responseBody)) {
      resetHeartRate();
    }
    return responseBody;
  }

  @Scheduled(fixedDelayString = "PT5M")
  @CachePut("responseBody")
  public void resetHeartRate() throws IOException, ExecutionException, InterruptedException {
    if (ObjectUtils.isEmpty(token)) {
      return;
    }
    final OAuthRequest request = new OAuthRequest(Verb.GET,
      String.format("https://api.fitbit.com/1/user/%s/activities/heart/date/today/1d/1min.json", token.getUserId()));
    request.addHeader("x-li-format", "json");
    oAuth20Service.signRequest(token, request);

    try (Response response = oAuth20Service.execute(request)) {
      log.info(response.getCode());
      this.responseBody = response.getBody();
    }
  }

  @Scheduled(fixedDelayString = "PT7H15M")
  void doRefreshToken() throws IOException, ExecutionException, InterruptedException {
    if (ObjectUtils.isEmpty(token)) {
      return;
    }
    this.token = (FitBitOAuth2AccessToken) oAuth20Service.refreshAccessToken(token.getRefreshToken());
    log.info("do refresh token.");
  }

}
