package com.example.fitbit.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

  @GetMapping("/")
  public String root() {
    return "redirect:/HeartRate.html";
  }

  /**
   * 認証済みユーザーのFitbitプロフィールを簡単に確認できるデバッグ用エンドポイント。
   * ngrokでの疎通確認にも利用できる。
   */
  @GetMapping("/auth")
  @ResponseBody
  public Map<String, Object> me(@AuthenticationPrincipal OAuth2User oAuth2User) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("principalName", oAuth2User.getName());
    response.put("attributes", oAuth2User.getAttributes());
    return response;
  }
}

