package com.example.mini.global.security.filter;

import com.example.mini.global.security.token.TokenProcessor;
import com.example.mini.global.security.token.TokenProcessorFactory;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private TokenProcessorFactory tokenProcessorFactory;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("Google 토큰 처리 성공")
    void testDoFilterInternalWithGoogleToken() throws ServletException, IOException {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
            TokenProcessor mockProcessor = mock(TokenProcessor.class);
            when(tokenProcessorFactory.getProcessor("Google")).thenReturn(mockProcessor);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(tokenProcessorFactory).getProcessor("Google");
            verify(mockProcessor).processToken(request, response);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Kakao 토큰 처리 성공")
    void testDoFilterInternalWithKakaoToken() throws ServletException, IOException {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
            TokenProcessor mockProcessor = mock(TokenProcessor.class);
            when(tokenProcessorFactory.getProcessor("Kakao")).thenReturn(mockProcessor);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(tokenProcessorFactory).getProcessor("Kakao");
            verify(mockProcessor).processToken(request, response);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("JWT 토큰 처리 성공")
    void testDoFilterInternalWithJwtToken() throws ServletException, IOException {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");
            TokenProcessor mockProcessor = mock(TokenProcessor.class);
            when(tokenProcessorFactory.getProcessor("Jwt")).thenReturn(mockProcessor);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(tokenProcessorFactory).getProcessor("Jwt");
            verify(mockProcessor).processToken(request, response);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("알 수 없는 토큰 타입")
    void testDoFilterInternalWithUnknownTokenType() throws ServletException, IOException {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("unknownToken");

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(tokenProcessorFactory, never()).getProcessor(anyString());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("쿠키가 없는 경우")
    void testDoFilterInternalWithNoCookie() throws ServletException, IOException {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            verify(tokenProcessorFactory, never()).getProcessor(anyString());
            verify(filterChain).doFilter(request, response);
        }
    }
}