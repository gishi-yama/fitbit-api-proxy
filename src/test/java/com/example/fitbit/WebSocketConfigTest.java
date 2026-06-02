package com.example.fitbit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class WebSocketConfigTest {

  /**
   * 実験・授業中にWebSocket接続が途切れにくいよう、idle timeoutが約1週間であることを確認する。
   */
  @Test
  void keepsWebSocketSessionIdleTimeoutForOneWeek() {
    WebSocketConfig config = new WebSocketConfig(null);

    long timeoutMillis = config.createWebSocketContainer().getMaxSessionIdleTimeout();

    assertThat(timeoutMillis).isEqualTo(Duration.ofDays(7).toMillis());
  }
}
