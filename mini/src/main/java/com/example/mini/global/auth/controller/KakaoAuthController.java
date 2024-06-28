package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final KakaoMemberService kakaoMemberService;
    private final KakaoApiClient kakaoApiClient;

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

        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(accessToken);
        kakaoMemberService.setMemberInactive(kakaoUserInfo.getEmail());

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

        TokenResponse tokenResponse = kakaoAuthService.authenticateKakao(code);
        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(tokenResponse.getAccess_token());
        Member member = kakaoMemberService.saveOrUpdateKakaoMember(kakaoUserInfo);

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

        kakaoAuthService.refreshKakaoToken(refreshTokenCookie.getValue());

        return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Cookie accessTokenCookie = CookieUtil.getCookie(request, "kakaoAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        kakaoAuthService.withdrawMember(accessTokenCookie.getValue());

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

        return ResponseEntity.ok(ApiResponse.DELETE());
    }

    @PostMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> changeNickname(
            HttpServletRequest request,
            @RequestBody ChangeNicknameRequest changeNicknameRequest) {

        Cookie accessTokenCookie = CookieUtil.getCookie(request, "kakaoAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = accessTokenCookie.getValue();
        String newNickname = changeNicknameRequest.getNickname();

        kakaoAuthService.updateNickname(accessToken, newNickname);

        return ResponseEntity.ok(ApiResponse.OK("닉네임이 성공적으로 변경되었습니다."));
    }
}
