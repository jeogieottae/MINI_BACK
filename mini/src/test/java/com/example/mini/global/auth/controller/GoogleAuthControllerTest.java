package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.service.GoogleAuthService;
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

public class GoogleAuthControllerTest {

    @InjectMocks
    private GoogleAuthController googleAuthController;

    @Mock
    private GoogleAuthService googleAuthService;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Google 로그인 리다이렉션 성공")
    void googleLoginSuccess() throws IOException {
        // Given
        String expectedAuthUrl = "https://accounts.google.com/o/oauth2/auth?...";
        when(googleAuthService.getGoogleAuthUrl()).thenReturn(expectedAuthUrl);

        // When
        googleAuthController.googleLogin(response);

        // Then
        assertEquals(expectedAuthUrl, response.getRedirectedUrl());
        verify(googleAuthService).getGoogleAuthUrl();
    }

    @Test
    @DisplayName("Google 콜백 성공")
    void googleCallbackSuccess() throws IOException {
        // Given
        String code = "valid_code";
        when(googleAuthService.googleCallback(code)).thenReturn(LoginResponse.builder().build());

        // When
        googleAuthController.googleCallback(code, null, response);

        // Then
        assertEquals("https://your-trip-pied.vercel.app/", response.getRedirectedUrl());
        verify(googleAuthService).googleCallback(code);
    }

    @Test
    @DisplayName("Google 콜백 실패 - 에러 파라미터 존재")
    void googleCallbackFailWithError() {
        // Given
        String error = "access_denied";

        // When & Then
        assertThrows(GlobalException.class, () ->
                googleAuthController.googleCallback(null, error, response)
        );
    }

    @Test
    @DisplayName("Google 콜백 실패 - 서비스 예외 발생")
    void googleCallbackFailWithServiceException() {
        // Given
        String code = "invalid_code";
        doThrow(new RuntimeException("Service error")).when(googleAuthService).googleCallback(code);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                googleAuthController.googleCallback(code, null, response)
        );
    }
}
