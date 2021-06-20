package com.example.fitbit;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class FitBitWebSocketHandler extends TextWebSocketHandler {

//  private final FitbitProxy fitbitProxy;
//  private final ObjectMapper mapper;
//  private final List<WebSocketSession> heldSessions;
//
//  @Autowired
//  public FitBitWebSocketHandler(FitbitProxy fitbitProxy, ObjectMapper mapper) {
//    this.fitbitProxy = fitbitProxy;
//    this.mapper = mapper;
//    this.heldSessions = new ArrayList<>();
//  }
//
//
//  @Override
//  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//    if (newerSession(session)) {
////      Optional.ofNullable(session.getUri()).ifPresent(learningLog::log);
//      heldSessions.add(session);
//    }
//    session.sendMessage(makeMessage());
//  }
//
//  boolean newerSession(WebSocketSession session) {
//    return heldSessions.stream()
//      .noneMatch(held -> held.getId().equals(session.getId()));
//  }
//
//  TextMessage makeMessage() throws IOException, ExecutionException, InterruptedException {
//    var json = mapper.writeValueAsString(fitbitProxy.getHeartRate());
//    return new TextMessage(json);
//  }

}
