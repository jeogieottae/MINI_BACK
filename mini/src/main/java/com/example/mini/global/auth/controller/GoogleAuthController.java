package com.example.mini.global.auth.controller;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.service.GoogleAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final String LOGIN_URI = "https://your-trip-pied.vercel.app/";

    @GetMapping("/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleAuthService.getGoogleAuthUrl());
    }

    @GetMapping("/callback")
    public void googleCallback(@RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "error", required = false) String error
        , HttpServletResponse response) throws IOException {
        if (error != null) {
            throw new GlobalException(AuthErrorCode.AUTHENTICATION_FAILED);
        }

        googleAuthService.googleCallback(code);

        response.sendRedirect(LOGIN_URI);
    }

}