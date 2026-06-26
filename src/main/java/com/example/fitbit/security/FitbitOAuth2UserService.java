package com.example.fitbit.security;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class FitbitOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private static final String USER_NODE_KEY = "user";
  private static final String USERNAME_ATTRIBUTE = "encodedId";

  private final RestClient restClient;

  public FitbitOAuth2UserService(RestClient fitbitRestClient) {
    this.restClient = fitbitRestClient;
  }

  /**
   * Fitbitのユーザー情報は"rawProfile.user"に格納されているので、encodedIdを抽出してPrincipalを生成し直す。
   */
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    Map<String, Object> rawAttributes = fetchUserInfo(userRequest);
    Object userNode = rawAttributes.get(USER_NODE_KEY);

    if (!(userNode instanceof Map<?, ?> userAttributes)) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response"),
          "Fitbitのユーザー情報に'user'フィールドが存在しません。");
    }

    Object encodedIdObject = userAttributes.get(USERNAME_ATTRIBUTE);
    String encodedId = encodedIdObject instanceof String id ? id : null;
    if (!StringUtils.hasText(encodedId)) {
      throw new OAuth2AuthenticationException(new OAuth2Error("missing_user_name_attribute"),
          "Fitbitのユーザー情報にencodedIdが含まれていません。");
    }

    Map<String, Object> flattenedAttributes = new LinkedHashMap<>();
    userAttributes.forEach((key, value) -> {
      if (key instanceof String stringKey) {
        flattenedAttributes.put(stringKey, value);
      }
    });
    flattenedAttributes.put(USERNAME_ATTRIBUTE, encodedId);
    flattenedAttributes.put("rawProfile", rawAttributes);

    Set<GrantedAuthority> authorities = buildAuthorities(userRequest.getAccessToken(), flattenedAttributes);
    return new DefaultOAuth2User(authorities, flattenedAttributes, USERNAME_ATTRIBUTE);
  }

  private Map<String, Object> fetchUserInfo(OAuth2UserRequest userRequest) {
    String userInfoEndpointUri = userRequest.getClientRegistration()
        .getProviderDetails()
        .getUserInfoEndpoint()
        .getUri();

    if (!StringUtils.hasText(userInfoEndpointUri)) {
      throw new OAuth2AuthenticationException(new OAuth2Error("missing_user_info_uri"),
          "user-info-uri が設定されていません。");
    }

    try {
      return restClient.get()
          .uri(userInfoEndpointUri)
          .header(HttpHeaders.AUTHORIZATION,
              "Bearer " + userRequest.getAccessToken().getTokenValue())
          .retrieve()
          .body(new ParameterizedTypeReference<Map<String, Object>>() {
          });
    } catch (Exception ex) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info_response"),
          "Fitbitのユーザー情報取得に失敗しました。", ex);
    }
  }

  private Set<GrantedAuthority> buildAuthorities(OAuth2AccessToken accessToken,
      Map<String, Object> attributes) {
    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    authorities.add(new OAuth2UserAuthority(attributes));
    if (accessToken != null && accessToken.getScopes() != null) {
      accessToken.getScopes()
          .forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));
    }
    return authorities;
  }
}
