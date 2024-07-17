package com.example.mini.global.security.token;

import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class KakaoTokenProcessorTest {

    @Mock
    private KakaoAuthService kakaoAuthService;
    @Mock
    private KakaoApiClient kakaoApiClient;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    private KakaoTokenProcessor kakaoTokenProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kakaoTokenProcessor = new KakaoTokenProcessor(kakaoAuthService, kakaoApiClient, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("카카오 토큰 프로세서_토큰 리프레시 하지 않음")
    void processToken_TokenNotExpired_ProcessesExistingToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String token = "valid_token";
        long currentTime = System.currentTimeMillis() / 1000;
        long expiresIn = currentTime + 3600; // 1 hour from now

        request.setCookies(
                new Cookie("kakaoAccessToken", token),
                new Cookie("kakaoAccessTokenExpiresIn", String.valueOf(expiresIn))
        );

        KakaoUserInfo userInfo = AuthServiceTestFixture.getKakaoUserInfo();
        when(kakaoApiClient.getKakaoUserInfo(token)).thenReturn(userInfo);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        // When
        kakaoTokenProcessor.processToken(request, response);

        // Then
        verify(kakaoApiClient).getKakaoUserInfo(token);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("카카오 토큰 프로세서_토큰 리프레시")
    void processToken_TokenExpired_RefreshesToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String oldToken = "expired_token";
        String newToken = "access_token";
        long currentTime = System.currentTimeMillis() / 1000;
        long expiresIn = currentTime + 200; // 200 seconds from now (less than 5 minutes)

        request.setCookies(
                new Cookie("kakaoAccessToken", oldToken),
                new Cookie("kakaoAccessTokenExpiresIn", String.valueOf(expiresIn))
        );

        TokenResponse tokenResponse = AuthServiceTestFixture.createTokenResponse();
        when(kakaoAuthService.kakaoRefresh(request)).thenReturn(tokenResponse);

        KakaoUserInfo userInfo = AuthServiceTestFixture.getKakaoUserInfo();
        when(kakaoApiClient.getKakaoUserInfo(newToken)).thenReturn(userInfo);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        // When
        kakaoTokenProcessor.processToken(request, response);

        // Then
        verify(kakaoAuthService).kakaoRefresh(request);
        verify(kakaoApiClient).getKakaoUserInfo(newToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}