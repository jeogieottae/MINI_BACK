package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.GoogleAuthService;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        CookieUtil.deleteCookie(response, "googleRefreshToken");
        CookieUtil.deleteCookie(response, "JSESSIONID");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect("http://localhost:8080/api/protected/home");
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> googleCallback(@RequestParam(value = "code", required = false) String code,
                                                        @RequestParam(value = "error", required = false) String error,
                                                        HttpServletResponse response) throws IOException {
        if(error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        TokenResponse tokenResponse = googleAuthService.getGoogleToken(code);
        GoogleUserInfo googleUserInfo = googleAuthService.getGoogleUserInfo(tokenResponse.getAccess_token());
        Member member = googleAuthService.saveGoogleMember(googleUserInfo);

        return ResponseEntity.ok(ApiResponse.OK(LoginResponse.builder()
                .state(member.getState())
                .accessToken(tokenResponse.getAccess_token())
                .refreshToken(tokenResponse.getRefresh_token())
                .build()));
    }

    @GetMapping("/refresh")
    public  ResponseEntity<ApiResponse<String>> googleRefresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, "googleRefreshToken");
        if (refreshTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenResponse tokenResponse = googleAuthService.getGoogleRefreshedToken(refreshTokenCookie.getValue());
        return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(HttpServletRequest request, HttpServletResponse response) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "googleAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = accessTokenCookie.getValue();
        googleAuthService.withdraw(accessToken);

        // 쿠키 삭제
        CookieUtil.deleteCookie(response, "googleAccessToken");
        CookieUtil.deleteCookie(response, "googleRefreshToken");
        CookieUtil.deleteCookie(response, "JSESSIONID");

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(ApiResponse.DELETE());
    }
}