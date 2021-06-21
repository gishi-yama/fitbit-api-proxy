package com.example.fitbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

@Component
public class MyHeartRateEdge extends TextWebSocketHandler {

  private final FitbitProxy fitbitProxy;
  private final ObjectMapper mapper;
  private final PlainAccessLogger accessLogger;
  private final List<WebSocketSession> heldSessions;

  private static final Pattern GAKUSEKI_QUERY_PATTERN = Pattern.compile("^gakuseki=([bdmp][0-9]{7})");

  @Autowired
  public MyHeartRateEdge(FitbitProxy fitbitProxy, ObjectMapper mapper, PlainAccessLogger accessLogger) {
    this.fitbitProxy = fitbitProxy;
    this.mapper = mapper;
    this.accessLogger = accessLogger;
    this.heldSessions = new ArrayList<>();
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    if (newerSession(session)) {
      String gakuseki = splitGakusekiFromQueryOf(session);
      accessLogger.logOnEdge(gakuseki);
      heldSessions.add(session);
    }
    session.sendMessage(makeMessage());
  }

  String splitGakusekiFromQueryOf(WebSocketSession session) {
    var array = Objects.requireNonNull(session.getUri()).getQuery().split("&");
    return Arrays.stream(array)
      .map(MyHeartRateEdge::gakusekiFrom)
      .filter(StringUtils::hasText)
      .findFirst().orElseThrow(() -> new RuntimeException("学籍番号がない"));
  }

  static String gakusekiFrom(String query) {
    var matcher = GAKUSEKI_QUERY_PATTERN.matcher(query);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return "";
  }

  boolean newerSession(WebSocketSession session) {
    return heldSessions.stream()
      .noneMatch(held -> held.getId().equals(session.getId()));
  }

  TextMessage makeMessage() throws IOException, ExecutionException, InterruptedException {
    var json = mapper.writeValueAsString(fitbitProxy.getHeartRate());
    return new TextMessage(json);
  }

}
