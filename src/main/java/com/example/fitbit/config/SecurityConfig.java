package com.example.fitbit.config;

import com.example.fitbit.security.DynamicRedirectAuthorizationRequestResolver;
import com.example.fitbit.security.FitbitOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Fitbitログイン後に静的ページへ戻すため、静的リソースも認証対象に含める。
   * 静的ファイルにアクセスして未ログインの場合は自動的にFitbitの認可画面へ遷移する。
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      OAuth2AuthorizationRequestResolver dynamicResolver,
      FitbitOAuth2UserService fitbitOAuth2UserService) throws Exception {

    http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers(
            "/HeartRate.html",
            "/HeartRate_goal.html",
            "/JSTraining.html",
            "/JSTraining_goal.html",
            "/auth",
            "/"
        ).authenticated()
        .anyRequest().permitAll()
    );

    http.oauth2Login(oauth2 -> oauth2
        .authorizationEndpoint(endpoint -> endpoint
            .authorizationRequestResolver(dynamicResolver))
        .userInfoEndpoint(userInfo -> userInfo
            .userService(fitbitOAuth2UserService))
        .defaultSuccessUrl("/HeartRate.html", true)
    );

    http.logout(Customizer.withDefaults());

    http.headers(headers -> headers
        .referrerPolicy(policy -> policy
            .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));

    http.csrf(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public OAuth2AuthorizationRequestResolver dynamicAuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    return new DynamicRedirectAuthorizationRequestResolver(clientRegistrationRepository);
  }

}
