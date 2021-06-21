package com.example.fitbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class FitBitWebSocketHandler extends TextWebSocketHandler {

  private final FitbitProxy fitbitProxy;
  private final ObjectMapper mapper;
  private final PlainAccessLogger accessLogger;
  private final List<WebSocketSession> heldSessions;

  @Autowired
  public FitBitWebSocketHandler(FitbitProxy fitbitProxy, ObjectMapper mapper, PlainAccessLogger accessLogger) {
    this.fitbitProxy = fitbitProxy;
    this.mapper = mapper;
    this.accessLogger = accessLogger;
    this.heldSessions = new ArrayList<>();
  }


  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    if (newerSession(session)) {
      accessLogger.log(session.getUri());
      heldSessions.add(session);
    }
    session.sendMessage(makeMessage());
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
