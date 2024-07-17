package com.example.mini.global.security.token;

import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import io.jsonwebtoken.Claims;
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
import java.io.IOException;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProcessorTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    private JwtTokenProcessor jwtTokenProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenProcessor = new JwtTokenProcessor(jwtProvider, tokenService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰 처리")
    void processToken_ValidToken_SetsAuthentication() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "valid.jwt.token";
        String email = "test@example.com";

        when(jwtProvider.resolveToken(request)).thenReturn(token);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtProvider.validateToken(token, TokenType.ACCESS)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 1000000));
        when(jwtProvider.getUserInfoFromToken(token, TokenType.ACCESS)).thenReturn(claims);
        when(jwtProvider.getEmailFromToken(token, TokenType.ACCESS)).thenReturn(email);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByEmail(email)).thenReturn(userDetails);

        // When
        jwtTokenProcessor.processToken(request, response);

        // Then
        verify(jwtProvider).getEmailFromToken(token, TokenType.ACCESS);
        verify(userDetailsService).loadUserByEmail(email);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("블랙리스트 토큰 처리")
    void processToken_BlacklistedToken_HandlesBlacklistedToken() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "blacklisted.jwt.token";

        when(jwtProvider.resolveToken(request)).thenReturn(token);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(true);

        // When
        jwtTokenProcessor.processToken(request, response);

        // Then
        assertEquals(AuthErrorCode.BLACKLISTED_TOKEN.getCode().value(), response.getStatus());
        assertTrue(response.getContentAsString().contains(AuthErrorCode.BLACKLISTED_TOKEN.getInfo()));
    }

    @Test
    @DisplayName("만료 임박 토큰 리프레시")
    void processToken_TokenNearExpiration_RefreshesToken() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String oldToken = "near.expiration.token";
        String newToken = "new.refreshed.token";
        String email = "test@example.com";

        when(jwtProvider.resolveToken(request)).thenReturn(oldToken);
        when(tokenService.isTokenBlacklisted(oldToken)).thenReturn(false);
        when(jwtProvider.validateToken(oldToken, TokenType.ACCESS)).thenReturn(true);

        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 200000)); // 200 seconds left
        when(jwtProvider.getUserInfoFromToken(oldToken, TokenType.ACCESS)).thenReturn(claims);

        Cookie refreshTokenCookie = new Cookie("refreshToken", "valid.refresh.token");
        request.setCookies(refreshTokenCookie);

        when(jwtProvider.getUserInfoFromToken("valid.refresh.token", TokenType.REFRESH)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(email);
        when(tokenService.getRefreshToken(email)).thenReturn("valid.refresh.token");
        when(jwtProvider.validateToken("valid.refresh.token", TokenType.REFRESH)).thenReturn(true);
        when(jwtProvider.createToken(email, TokenType.ACCESS, false)).thenReturn(newToken);
        when(jwtProvider.getEmailFromToken(newToken, TokenType.ACCESS)).thenReturn(email);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByEmail(email)).thenReturn(userDetails);

        // When
        jwtTokenProcessor.processToken(request, response);

        // Then
        verify(jwtProvider).createToken(email, TokenType.ACCESS, false);
        verify(userDetailsService).loadUserByEmail(email);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(response.getCookie("accessToken").getValue().equals(newToken));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 처리")
    void processToken_InvalidToken_HandlesInvalidToken() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "invalid.jwt.token";

        when(jwtProvider.resolveToken(request)).thenReturn(token);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtProvider.validateToken(token, TokenType.ACCESS)).thenReturn(false);

        // When
        jwtTokenProcessor.processToken(request, response);

        // Then
        assertEquals(AuthErrorCode.INVALID_TOKEN.getCode().value(), response.getStatus());
        assertTrue(response.getContentAsString().contains(AuthErrorCode.INVALID_TOKEN.getInfo()));
    }
}