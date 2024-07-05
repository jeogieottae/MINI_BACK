package com.example.mini.global.auth.controller;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.service.KakaoAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final String LOGIN_URI = "https://your-trip-pied.vercel.app/";

    @GetMapping("/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = kakaoAuthService.getKakaoAuthUrl();
        log.info("kakao login url: {}", kakaoAuthUrl);
        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/callback")
    public void kakaoCallback(@RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "error", required = false) String error, HttpServletResponse response) throws IOException {
        if (error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }
        kakaoAuthService.kakaoCallback(code);
        response.sendRedirect(LOGIN_URI);
    }
}