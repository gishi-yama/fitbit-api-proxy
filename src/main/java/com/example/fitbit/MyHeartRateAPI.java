package com.example.fitbit;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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

  @GetMapping("auth")
  public String auth() {
    return fitbitProxy.getAuthnURL();
  }

  @GetMapping("save")
  public String save(@RequestParam String code) throws IOException, ExecutionException, InterruptedException {
    return fitbitProxy.saveCode(code);
  }

  @GetMapping("heart")
  public FitbitProxy.IntradayHeartRate heart(@RequestParam String gakuseki) throws IOException, ExecutionException, InterruptedException {
    accessLogger.logForAPI(gakuseki);
    return fitbitProxy.getHeartRate();
  }
}
