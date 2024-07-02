package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.api.exception.success.SuccessCode;
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

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(@RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        LoginResponse loginResponse = kakaoAuthService.kakaoCallback(code);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.KAKAO_LOGIN_SUCCESS, loginResponse));
    }

}
