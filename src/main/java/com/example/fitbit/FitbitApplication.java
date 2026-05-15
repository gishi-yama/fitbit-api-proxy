package com.example.fitbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FitbitApplication {

  public static void main(String[] args) {
    SpringApplication.run(FitbitApplication.class, args);
  }
}

