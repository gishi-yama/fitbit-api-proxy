package com.example.fitbit.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * リバースプロキシ（ngrok等）経由でも正しいredirect_uriを算出するためのResolver。
 */
public class DynamicRedirectAuthorizationRequestResolver implements
    OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver delegate;

  public DynamicRedirectAuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
    return customizeRedirectUri(request, authorizationRequest);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
    return customizeRedirectUri(request, authorizationRequest);
  }

  private OAuth2AuthorizationRequest customizeRedirectUri(HttpServletRequest request,
      OAuth2AuthorizationRequest original) {
    if (original == null) {
      return null;
    }

    String registrationId = extractRegistrationId(request, original);
    if (!StringUtils.hasText(registrationId)) {
      return original;
    }

    String redirectUri = buildRedirectUri(request, registrationId);
    String state = original.getState();
    if (!StringUtils.hasText(state)) {
      state = UUID.randomUUID().toString(); // Fitbitがstate未指定時に#_=_を付与するため念のため常にstateを確保
    }

    return OAuth2AuthorizationRequest.from(original)
        .state(state)
        .redirectUri(redirectUri)
        .build();
  }

  private String extractRegistrationId(HttpServletRequest request,
      OAuth2AuthorizationRequest authorizationRequest) {
    Object attribute = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
    if (attribute instanceof String attributeText && StringUtils.hasText(attributeText)) {
      return attributeText;
    }
    String requestUri = Optional.ofNullable(request.getRequestURI()).orElse("");
    int separatorIndex = requestUri.lastIndexOf('/');
    if (separatorIndex >= 0 && separatorIndex < requestUri.length() - 1) {
      return requestUri.substring(separatorIndex + 1);
    }
    return null;
  }

  private String buildRedirectUri(HttpServletRequest request, String registrationId) {
    // X-Forwarded-* ヘッダーを直接読む（ForwardedHeaderFilter が Spring Boot 4.0 で正しく機能しないため）。
    String xForwardedProto = request.getHeader("X-Forwarded-Proto");
    boolean forwarded = StringUtils.hasText(xForwardedProto);
    String scheme = forwarded ? xForwardedProto.split(",")[0].trim() : request.getScheme();

    String xForwardedHost = request.getHeader("X-Forwarded-Host");
    String host = StringUtils.hasText(xForwardedHost)
        ? xForwardedHost.split(",")[0].trim()
        : request.getServerName();

    // X-Forwarded-Port を優先。なければ、プロキシ経由なら scheme の標準ポート、そうでなければ実ポート。
    String xForwardedPort = request.getHeader("X-Forwarded-Port");
    int port;
    if (StringUtils.hasText(xForwardedPort)) {
      port = Integer.parseInt(xForwardedPort.split(",")[0].trim());
    } else if (forwarded) {
      port = "https".equals(scheme) ? 443 : 80;
    } else {
      port = request.getServerPort();
    }

    boolean defaultPort = ("http".equals(scheme) && port == 80)
        || ("https".equals(scheme) && port == 443);

    UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
        .scheme(scheme)
        .host(host);

    if (!defaultPort) {
      builder.port(port);
    }

    String contextPath = request.getContextPath();
    if (StringUtils.hasText(contextPath)) {
      builder.path(contextPath);
    }
    builder.path("/login/oauth2/code/").path(registrationId);
    return builder.build().toUriString();
  }
}
