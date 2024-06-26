package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    private String kakaoLogoutRedirectUri = "http://localhost:8080/api/auth/kakao/home";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
    }


    @GetMapping("/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {

        log.info("kakao login url: {}", getKakaoAuthUrl());

        response.sendRedirect(getKakaoAuthUrl());
    }

    @GetMapping("/logout")
    public void kakaoLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id=" + kakaoClientId
                + "&logout_redirect_uri=" + kakaoLogoutRedirectUri);

        // 쿠키 삭제
        CookieUtil.deleteCookie(response, "kakaoAccessToken");
        CookieUtil.deleteCookie(response, "kakaoRefreshToken");
        CookieUtil.deleteCookie(response, "JSESSIONID");

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/home")
    public ResponseEntity<TokenResponse> kakaoCallback(@RequestParam(value = "code", required = false) String code,
                                                       @RequestParam(value = "error", required = false) String error,
                                                       HttpServletResponse response) throws IOException {
        log.info("code: {}", code);
        log.info("error: {}", error);

        TokenResponse tokenResponse = kakaoAuthService.getKakaoToken(code);
        KakaoUserInfo kakaoUserInfo = kakaoAuthService.getKakaoUserInfo(tokenResponse.getAccess_token());
        Member member = kakaoAuthService.saveKakaoMember(kakaoUserInfo);

        log.info("access token: {}", tokenResponse.getAccess_token());
        log.info("refresh token: {}", tokenResponse.getRefresh_token());

        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenResponse> kakaoRefresh(@RequestParam(value = "refresh_token") String refreshToken) {
        TokenResponse tokenResponse = kakaoAuthService.getKakaoRefreshedToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}
