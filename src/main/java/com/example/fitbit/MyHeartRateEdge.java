package com.example.fitbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import org.springframework.security.core.Authentication;

@Slf4j
@Component
public class MyHeartRateEdge extends TextWebSocketHandler {

  private final FitbitProxy fitbitProxy;
  private final ObjectMapper mapper;
  private final PlainAccessLogger accessLogger;
  private final List<WebSocketSession> heldSessions;
  /**
   * Fitbit認証済みクライアントから取得した最新の心拍データを保持し、未認証クライアントにも配信する。
   */
  private volatile String lastPayloadJson = "[]";

  private static final Pattern GAKUSEKI_QUERY_PATTERN = Pattern.compile("^gakuseki=([bdmp][0-9]{7})");

//  private static final Logger log = LoggerFactory.getLogger(MyHeartRateEdge.class);

  @Autowired
  public MyHeartRateEdge(FitbitProxy fitbitProxy, ObjectMapper mapper, PlainAccessLogger accessLogger) {
    this.fitbitProxy = fitbitProxy;
    this.mapper = mapper;
    this.accessLogger = accessLogger;
    this.heldSessions = new CopyOnWriteArrayList<>();
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    if (isNewSession(session)) {
      String gakuseki = splitGakusekiFromQueryOf(session);
      accessLogger.logOnEdge(gakuseki);
      heldSessions.add(session);
    }
    MyHeartRateEdge.send(session, makeMessage(session));
  }

  @Scheduled(fixedDelayString = "PT1M")
  public void pushMessage() {
    heldSessions.removeIf(s -> !s.isOpen());
    log.info("push message to {} clients.", heldSessions.size());
    heldSessions.forEach(held -> {
      try {
        MyHeartRateEdge.send(held, makeMessage(held));
      } catch (Exception ex) {
        log.warn("心拍データの送信に失敗しました。", ex);
      }
    });
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    heldSessions.removeIf(held -> isSameId(held, session));
  }

  static void send(WebSocketSession session, TextMessage message) {
    try {
      session.sendMessage(message);
    } catch (IOException e) {
      log.error("WebSocketメッセージ送信失敗: session={}", session.getId(), e);
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

  boolean isNewSession(WebSocketSession session) {
    return heldSessions.stream()
      .noneMatch(held -> isSameId(held, session));
  }

  TextMessage makeMessage(WebSocketSession session) throws IOException {
    Authentication authentication = extractAuthentication(session.getPrincipal());
    if (authentication == null) {
      return new TextMessage(lastPayloadJson);
    }
    var json = mapper.writeValueAsString(fitbitProxy.getHeartRate(authentication));
    lastPayloadJson = json;
    return new TextMessage(json);
  }

  private Authentication extractAuthentication(Principal principal) {
    if (principal instanceof Authentication authentication && authentication.isAuthenticated()) {
      return authentication;
    }
    return null;
  }

}
