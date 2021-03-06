package com.example.fitbit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.standard.WebSocketContainerFactoryBean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final MyHeartRateEdge fitBitWebSocketHandler;

  @Autowired
  public WebSocketConfig(MyHeartRateEdge fitBitWebSocketHandler) {
    this.fitBitWebSocketHandler = fitBitWebSocketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
    webSocketHandlerRegistry.addHandler(fitBitWebSocketHandler, "/edge")
      .setAllowedOrigins("*");
  }

  @Bean
  public WebSocketContainerFactoryBean createWebSocketContainer() {
    WebSocketContainerFactoryBean container = new WebSocketContainerFactoryBean();
    container.setMaxTextMessageBufferSize(8192);
    container.setMaxSessionIdleTimeout(TimeUnit.MINUTES.toMillis(30));
    return container;
  }

}
