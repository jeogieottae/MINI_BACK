package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.service.KakaoAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KakaoAuthControllerTest {

    @InjectMocks
    private KakaoAuthController kakaoAuthController;

    @Mock
    private KakaoAuthService kakaoAuthService;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("카카오 로그인 리다이렉션 성공")
    void kakaoLoginSuccess() throws IOException {
        // Given
        String expectedAuthUrl = "https://kauth.kakao.com/oauth/authorize?...";
        when(kakaoAuthService.getKakaoAuthUrl()).thenReturn(expectedAuthUrl);

        // When
        kakaoAuthController.kakaoLogin(response);

        // Then
        assertEquals(expectedAuthUrl, response.getRedirectedUrl());
        verify(kakaoAuthService).getKakaoAuthUrl();
    }

    @Test
    @DisplayName("카카오 콜백 성공")
    void kakaoCallbackSuccess() throws IOException {
        // Given
        String code = "valid_code";
        when(kakaoAuthService.kakaoCallback(code)).thenReturn(LoginResponse.builder().build());

        // When
        kakaoAuthController.kakaoCallback(code, null, response);

        // Then
        assertEquals("https://your-trip-pied.vercel.app/", response.getRedirectedUrl());
        verify(kakaoAuthService).kakaoCallback(code);
    }

    @Test
    @DisplayName("카카오 콜백 실패 - 에러 파라미터 존재")
    void kakaoCallbackFailWithError() {
        // Given
        String error = "access_denied";

        // When & Then
        assertThrows(GlobalException.class, () ->
                kakaoAuthController.kakaoCallback(null, error, response)
        );
    }

    @Test
    @DisplayName("카카오 콜백 실패 - 서비스 예외 발생")
    void kakaoCallbackFailWithServiceException() {
        // Given
        String code = "invalid_code";
        doThrow(new RuntimeException("Service error")).when(kakaoAuthService).kakaoCallback(code);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                kakaoAuthController.kakaoCallback(code, null, response)
        );
    }
}