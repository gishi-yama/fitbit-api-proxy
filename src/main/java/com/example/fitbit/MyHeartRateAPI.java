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

  private final FitbitProxy fitbitProxy;
  private final PlainAccessLogger accessLogger;

  @Autowired
  public MyHeartRateAPI(FitbitProxy fitbitProxy, PlainAccessLogger accessLogger) {
    this.fitbitProxy = fitbitProxy;
    this.accessLogger = accessLogger;
  }

  @GetMapping("heart")
  public List<FitbitProxy.OnetimeHeartRate> heart(@RequestParam String gakuseki,
      Authentication authentication) {
    accessLogger.logOnAPI(gakuseki);
    return fitbitProxy.getHeartRate(authentication);
  }
}
