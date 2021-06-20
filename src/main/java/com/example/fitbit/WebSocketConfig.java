package com.example.fitbit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

//  private final FitBitWebSocketHandler fitBitWebSocketHandler;
//
//  @Autowired
//  public WebSocketConfig(FitBitWebSocketHandler fitBitWebSocketHandler) {
//    this.fitBitWebSocketHandler = fitBitWebSocketHandler;
//  }
//
//  @Override
//  public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
//    webSocketHandlerRegistry.addHandler(fitBitWebSocketHandler, "/edge")
//      .setAllowedOrigins("*");
//  }

}
