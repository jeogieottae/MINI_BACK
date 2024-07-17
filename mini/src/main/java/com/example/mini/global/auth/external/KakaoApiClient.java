package com.example.mini.global.auth.external;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class KakaoApiClient extends OAuthApiClient {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Override
    protected String getClientId() {
        return clientId;
    }

    @Override
    protected String getClientSecret() {
        return clientSecret;
    }

    @Override
    protected String getRedirectUri() {
        return redirectUri;
    }

    @Override
    protected String getTokenUri() {
        return TOKEN_URI;
    }

    @Override
    protected String getUserInfoUri() {
        return USER_INFO_URI;
    }

    public KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
        Map<String, Object> attributes = getUserInfo(kakaoAccessToken);
        return new KakaoUserInfo(attributes);
    }
}
