package com.example.fitbit;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.standard.WebSocketContainerFactoryBean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private static final Duration SESSION_IDLE_TIMEOUT = Duration.ofDays(7);

  private final MyHeartRateEdge fitBitWebSocketHandler;

  /**
   * WebSocket handlerを受け取り、心拍データ配信用endpoint登録に利用する。
   */
  @Autowired
  public WebSocketConfig(MyHeartRateEdge fitBitWebSocketHandler) {
    this.fitBitWebSocketHandler = fitBitWebSocketHandler;
  }

  /**
   * 未認証の静的ページ利用者にも心拍データを配信できるよう、/edge endpointを公開する。
   */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
    webSocketHandlerRegistry.addHandler(fitBitWebSocketHandler, "/edge")
      .setAllowedOrigins("*");
  }

  /**
   * 実験・授業中にidle timeoutで接続が切れないよう、WebSocket sessionを約1週間保持する。
   */
  @Bean
  public WebSocketContainerFactoryBean createWebSocketContainer() {
    WebSocketContainerFactoryBean container = new WebSocketContainerFactoryBean();
    container.setMaxTextMessageBufferSize(8192);
    container.setMaxSessionIdleTimeout(SESSION_IDLE_TIMEOUT.toMillis());
    return container;
  }

}
