package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
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

    private String kakaoLogoutRedirectUri = "http://localhost:8080/api/protected/home";

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

        // 사용자 비활성화 처리
        String accessToken = CookieUtil.getCookie(request, "kakaoAccessToken").getValue();
        kakaoAuthService.setMemberInactive(accessToken);

        // 쿠키 삭제
        CookieUtil.deleteCookie(response, "kakaoAccessToken");
        CookieUtil.deleteCookie(response, "kakaoRefreshToken");
        CookieUtil.deleteCookie(response, "JSESSIONID");

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id=" + kakaoClientId
                + "&logout_redirect_uri=" + kakaoLogoutRedirectUri);
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(@RequestParam(value = "code", required = false) String code,
                                                       @RequestParam(value = "error", required = false) String error,
                                                       HttpServletResponse response) throws IOException {
        if(error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        TokenResponse tokenResponse = kakaoAuthService.getKakaoToken(code);
        KakaoUserInfo kakaoUserInfo = kakaoAuthService.getKakaoUserInfo(tokenResponse.getAccess_token());
        Member member = kakaoAuthService.saveKakaoMember(kakaoUserInfo);

        return ResponseEntity.ok(ApiResponse.OK(LoginResponse.builder()
                .state(member.getState())
                .accessToken(tokenResponse.getAccess_token())
                .refreshToken(tokenResponse.getRefresh_token())
                .build()));
    }

    @GetMapping("/refresh")
    public  ResponseEntity<ApiResponse<String>> kakaoRefresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, "kakaoRefreshToken");
        if (refreshTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenResponse tokenResponse = kakaoAuthService.getKakaoRefreshedToken(refreshTokenCookie.getValue());
        return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
    }
}
