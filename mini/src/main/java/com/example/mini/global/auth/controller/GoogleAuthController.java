package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.GoogleApiClient;
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

    @GetMapping("/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleAuthService.getGoogleAuthUrl());
    }

    @GetMapping("/logout")
    public void googleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        googleAuthService.googleLogout(request, response);
        response.sendRedirect("https://api.miniteam2.store/api/protected/home");
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> googleCallback(@RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        LoginResponse loginResponse = googleAuthService.googleCallback(code);
        return ResponseEntity.ok(ApiResponse.OK(loginResponse));
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> googleRefresh(HttpServletRequest request) {
        googleAuthService.googleRefresh(request);
        return ResponseEntity.ok(ApiResponse.OK("엑세스 토큰이 재발급 되었습니다."));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(HttpServletRequest request, HttpServletResponse response) {
        googleAuthService.withdraw(request, response);
        return ResponseEntity.ok(ApiResponse.DELETE());
    }

    @PostMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> changeNickname(
        HttpServletRequest request,
        @RequestBody ChangeNicknameRequest changeNicknameRequest) {

        googleAuthService.changeNickname(request, changeNicknameRequest.getNickname());
        return ResponseEntity.ok(ApiResponse.OK("닉네임이 성공적으로 변경되었습니다."));
    }
}