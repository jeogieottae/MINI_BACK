package com.example.mini.global.security.token;

import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.auth.service.GoogleAuthService;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class GoogleTokenProcessorTest {

    @Mock
    private GoogleAuthService googleAuthService;
    @Mock
    private GoogleApiClient googleApiClient;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    private GoogleTokenProcessor googleTokenProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        googleTokenProcessor = new GoogleTokenProcessor(googleAuthService, googleApiClient, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("구글 토큰 프로세서_토큰 리프레시 하지 않음")
    void processToken_TokenNotExpired_ProcessesExistingToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String token = "valid_google_token";
        long currentTime = System.currentTimeMillis() / 1000;
        long expiresIn = currentTime + 3600; // 1 hour from now

        request.setCookies(
                new Cookie("googleAccessToken", token),
                new Cookie("googleAccessTokenExpiresIn", String.valueOf(expiresIn))
        );

        GoogleUserInfo userInfo = AuthServiceTestFixture.getGoogleUserInfo();
        when(googleApiClient.getGoogleUserInfo(token)).thenReturn(userInfo);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        // When
        googleTokenProcessor.processToken(request, response);

        // Then
        verify(googleApiClient).getGoogleUserInfo(token);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("구글 토큰 프로세서_토큰 리프레시")
    void processToken_TokenExpired_RefreshesToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String oldToken = "expired_google_token";
        String newToken = "access_token";
        long currentTime = System.currentTimeMillis() / 1000;
        long expiresIn = currentTime + 200; // 200 seconds from now (less than 5 minutes)

        request.setCookies(
                new Cookie("googleAccessToken", oldToken),
                new Cookie("googleAccessTokenExpiresIn", String.valueOf(expiresIn))
        );

        TokenResponse tokenResponse = AuthServiceTestFixture.createTokenResponse();
        when(googleAuthService.googleRefresh(request)).thenReturn(tokenResponse);

        GoogleUserInfo userInfo = AuthServiceTestFixture.getGoogleUserInfo();
        when(googleApiClient.getGoogleUserInfo(newToken)).thenReturn(userInfo);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        // When
        googleTokenProcessor.processToken(request, response);

        // Then
        verify(googleAuthService).googleRefresh(request);
        verify(googleApiClient).getGoogleUserInfo(newToken);
        verify(userDetailsService).loadUserByUsername("test@example.com");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
