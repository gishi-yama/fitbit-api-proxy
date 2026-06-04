package com.example.fitbit.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityConfigTest {

  /**
   * 未ログインで静的ページへアクセスした場合、OAuth2ログイン後に元のURLへ戻す設定であることを確認する。
   */
  @Test
  void usesSavedRequestAfterOauthLogin() {
    assertThat(SecurityConfig.alwaysUseDefaultSuccessUrl()).isFalse();
  }
}
