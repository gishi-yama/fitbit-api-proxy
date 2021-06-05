package com.example.fitbit;

import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FitBitConfig {

  @Value("${fitbit.api.clientId}")
  private String clientId;

  @Value("${fitbit.api.clientSecret}")
  private String clientSecret;

  @Value("${fitbit.api.callbackURL}")
  private String callbackURL;

  @Bean
  public OAuth20Service oAuth20Service() {
    return new ServiceBuilder(clientId)
      .apiSecret(clientSecret)
      .defaultScope("activity heartrate location nutrition profile settings sleep social weight")
      .callback(callbackURL)
      .build(FitbitApi20.instance());
  }

}
