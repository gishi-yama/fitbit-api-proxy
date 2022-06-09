package com.example.fitbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
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

@Log4j2
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
    MyHeartRateEdge.send(session, makeMessage());
  }

  @Scheduled(fixedDelayString = "PT1M")
  public void pushMessage() throws IOException, ExecutionException, InterruptedException {
    log.info("push message to " + heldSessions.size() + " clients.");
    var newMessage = makeMessage();
    heldSessions.stream()
      .filter(WebSocketSession::isOpen)
      .forEach(held -> MyHeartRateEdge.send(held, newMessage));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    heldSessions.removeIf(held -> isSameId(held, session));
  }

  static void send(WebSocketSession session, TextMessage message) {
    try {
      session.sendMessage(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static boolean isSameId(WebSocketSession session1, WebSocketSession session2) {
    return Objects.equals(session1.getId(), session2.getId());
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
      .noneMatch(held -> isSameId(held, session));
  }

  TextMessage makeMessage() throws IOException, ExecutionException, InterruptedException {
    var json = mapper.writeValueAsString(fitbitProxy.getHeartRate());
    return new TextMessage(json);
  }

}
