package com.example.fitbit;

import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 認証済みクライアントが取得した最新の心拍データを、未認証クライアントへ共有する。
 */
@Service
public class HeartRateSnapshotService {

  private final FitbitProxy fitbitProxy;
  private volatile List<FitbitProxy.OnetimeHeartRate> lastHeartRates = List.of();

  /**
   * Fitbit API 呼び出しを担当する proxy を受け取る。
   */
  public HeartRateSnapshotService(FitbitProxy fitbitProxy) {
    this.fitbitProxy = fitbitProxy;
  }

  /**
   * 認証済みなら Fitbit から最新値を取得し、未認証なら最後に取得した値を返す。
   */
  public List<FitbitProxy.OnetimeHeartRate> getHeartRate(Authentication authentication) {
    if (!isFitbitUser(authentication)) {
      return lastHeartRates;
    }
    lastHeartRates = List.copyOf(fitbitProxy.getHeartRate(authentication));
    return lastHeartRates;
  }

  /**
   * Spring Security の anonymous principal を Fitbit 認証済みユーザーから除外する。
   */
  private boolean isFitbitUser(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
