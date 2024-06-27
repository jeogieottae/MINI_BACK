package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.GoogleAuthService;
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
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private String getGoogleAuthUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
            + "?client_id=" + googleClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                        + "&scope=email%20profile"
                        + "&access_type=offline"
                        + "&prompt=consent";
}

@GetMapping("/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        log.info("Google login url: {}", getGoogleAuthUrl());
        response.sendRedirect(getGoogleAuthUrl());
    }

    @GetMapping("/logout")
    public void googleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 사용자 비활성화 처리
        String accessToken = CookieUtil.getCookie(request, "googleAccessToken").getValue();
        googleAuthService.setMemberInactive(accessToken);

        // 로그 아웃 url 없음
        // 쿠키, 세션 삭제
        CookieUtil.deleteCookie(response, "googleAccessToken");
        CookieUtil.deleteCookie(response, "googleRefreshToken");  // 리프레시 토큰 쿠키 삭제
        CookieUtil.deleteCookie(response, "JSESSIONID");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect("http://localhost:8080/api/auth/kakao/home");
    }

    @GetMapping("/home")
    public ResponseEntity<TokenResponse> googleCallback(@RequestParam(value = "code", required = false) String code,
                                                        @RequestParam(value = "error", required = false) String error,
                                                        HttpServletResponse response) throws IOException {
        log.info("code: {}", code);
        log.info("error: {}", error);

        TokenResponse tokenResponse = googleAuthService.getGoogleToken(code);
        GoogleUserInfo googleUserInfo = googleAuthService.getGoogleUserInfo(tokenResponse.getAccess_token());
        Member member = googleAuthService.saveGoogleMember(googleUserInfo);

        log.info("access token: {}", tokenResponse.getAccess_token());
        // Google might not always return a refresh token
        if (tokenResponse.getRefresh_token() != null) {
            log.info("refresh token: {}", tokenResponse.getRefresh_token());
        }

        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenResponse> googleRefresh(@RequestParam(value = "refresh_token") String refreshToken) {
        TokenResponse tokenResponse = googleAuthService.getGoogleRefreshedToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}