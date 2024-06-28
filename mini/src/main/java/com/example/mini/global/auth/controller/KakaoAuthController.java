package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        log.info("kakao login url: {}", kakaoAuthService.getKakaoAuthUrl());
        response.sendRedirect(kakaoAuthService.getKakaoAuthUrl());
    }

    @GetMapping("/logout")
    public void kakaoLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        kakaoAuthService.kakaoLogout(request, response);
        response.sendRedirect(kakaoAuthService.getKakaoLogoutRedirectUri());
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(@RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        LoginResponse loginResponse = kakaoAuthService.kakaoCallback(code);
        return ResponseEntity.ok(ApiResponse.OK(loginResponse));
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> kakaoRefresh(HttpServletRequest request) {
        kakaoAuthService.kakaoRefresh(request);
        return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        kakaoAuthService.withdraw(request, response);
        return ResponseEntity.ok(ApiResponse.DELETE());
    }

    @PostMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> changeNickname(
        HttpServletRequest request,
        @RequestBody ChangeNicknameRequest changeNicknameRequest) {

        kakaoAuthService.changeNickname(request, changeNicknameRequest.getNickname());
        return ResponseEntity.ok(ApiResponse.OK("닉네임이 성공적으로 변경되었습니다."));
    }
}
