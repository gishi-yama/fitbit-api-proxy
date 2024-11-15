package com.example.fitbit;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
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
  public void auth(HttpServletResponse response) throws IOException {
    response.sendRedirect(fitbitProxy.getAuthnURL());
  }

  @GetMapping("save")
  public String save(@RequestParam String code) throws IOException, ExecutionException, InterruptedException {
    return fitbitProxy.saveCode(code);
  }

  @GetMapping("heart")
  public List<FitbitProxy.OnetimeHeartRate> heart(@RequestParam String gakuseki) throws IOException, ExecutionException, InterruptedException {
    accessLogger.logOnAPI(gakuseki);
    return fitbitProxy.getHeartRate();
  }
}
