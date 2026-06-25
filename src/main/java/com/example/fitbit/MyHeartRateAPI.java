package com.example.fitbit;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
public class MyHeartRateAPI {

  private final HeartRateSnapshotService heartRateSnapshotService;
  private final PlainAccessLogger accessLogger;

  /**
   * Web API 用に心拍データ共有サービスと access log を受け取る。
   */
  @Autowired
  public MyHeartRateAPI(HeartRateSnapshotService heartRateSnapshotService,
      PlainAccessLogger accessLogger) {
    this.heartRateSnapshotService = heartRateSnapshotService;
    this.accessLogger = accessLogger;
  }

  /**
   * /edge と同じく、認証済みなら最新値、未認証なら最後に取得済みの値を返す。
   */
  @GetMapping("heart")
  public List<FitbitProxy.OnetimeHeartRate> heart(@RequestParam String gakuseki,
      Authentication authentication) {
    accessLogger.logOnAPI(gakuseki);
    return heartRateSnapshotService.getHeartRate(authentication);
  }
}
