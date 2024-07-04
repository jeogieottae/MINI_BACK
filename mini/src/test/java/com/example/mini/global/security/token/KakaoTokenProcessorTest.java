//package com.example.mini.global.security.token;
//
//import com.example.mini.global.auth.external.KakaoApiClient;
//import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
//import com.example.mini.global.auth.model.KakaoUserInfo;
//import com.example.mini.global.auth.model.TokenResponse;
//import com.example.mini.global.auth.service.KakaoAuthService;
//import com.example.mini.global.security.details.UserDetailsServiceImpl;
//import com.example.mini.global.util.cookies.CookieUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import jakarta.servlet.http.Cookie;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//public class KakaoTokenProcessorTest {
//
//    @InjectMocks
//    private KakaoTokenProcessor kakaoTokenProcessor;
//
//    @Mock
//    private KakaoAuthService kakaoAuthService;
//
//    @Mock
//    private KakaoApiClient kakaoApiClient;
//
//    @Mock
//    private UserDetailsServiceImpl userDetailsService;
//
//    private MockHttpServletRequest request;
//    private MockHttpServletResponse response;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        request = new MockHttpServletRequest();
//        response = new MockHttpServletResponse();
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    @DisplayName("유효한 카카오 토큰 처리")
//    void processValidKakaoToken() {
//        // Given
//        String token = "validToken";
//        Cookie cookie = new Cookie("kakaoAccessToken", token);
//        cookie.setMaxAge((int)(System.currentTimeMillis() / 1000) + 3600); // 7일
//
//        KakaoUserInfo userInfo = AuthServiceTestFixture.getKakaoUserInfo();
//        UserDetails userDetails = mock(UserDetails.class);
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(cookie);
//            when(kakaoApiClient.getKakaoUserInfo(token)).thenReturn(userInfo);
//            when(userDetailsService.loadUserByUsername(userInfo.getEmail())).thenReturn(userDetails);
//
//            // When
//            kakaoTokenProcessor.processToken(request, response);
//
//            // Then
//            verify(kakaoApiClient).getKakaoUserInfo(token);
//            verify(userDetailsService).loadUserByUsername(userInfo.getEmail());
//            verify(kakaoAuthService, never()).kakaoRefresh(any(HttpServletRequest.class));
//            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
//        }
//    }
//
//    @Test
//    @DisplayName("만료 임박한 카카오 토큰 갱신")
//    void refreshExpiringKakaoToken() {
//        // Given
//        String oldToken = "oldToken";
//        String newToken = "newToken";
//        Cookie cookie = new Cookie("kakaoAccessToken", oldToken);
//        cookie.setMaxAge(200); // 만료 임박
//        request.setCookies(cookie);
//
//        TokenResponse tokenResponse = AuthServiceTestFixture.createTokenResponse();
//        KakaoUserInfo userInfo = AuthServiceTestFixture.getKakaoUserInfo();
//        UserDetails userDetails = mock(UserDetails.class);
//
//        when(kakaoAuthService.kakaoRefresh(request)).thenReturn(tokenResponse);
//        when(kakaoApiClient.getKakaoUserInfo(newToken)).thenReturn(userInfo);
//        when(userDetailsService.loadUserByUsername(userInfo.getEmail())).thenReturn(userDetails);
//
//        // When
//        kakaoTokenProcessor.processToken(request, response);
//
//        // Then
//        verify(kakaoAuthService).kakaoRefresh(request);
//        verify(kakaoApiClient).getKakaoUserInfo(newToken);
//        verify(userDetailsService).loadUserByUsername(userInfo.getEmail());
//        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
//    }
//
//    @Test
//    @DisplayName("카카오 토큰 없음")
//    void noKakaoToken() {
//        // Given
//        // No cookie set
//
//        // When
//        kakaoTokenProcessor.processToken(request, response);
//
//        // Then
//        verifyNoInteractions(kakaoApiClient, kakaoAuthService, userDetailsService);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
//    }
//
//    @Test
//    @DisplayName("유효하지 않은 카카오 사용자 정보")
//    void invalidKakaoUserInfo() {
//        // Given
//        String token = "invalidToken";
//        Cookie cookie = new Cookie("kakaoAccessToken", token);
//        cookie.setMaxAge(3600);
//        request.setCookies(cookie);
//
//        when(kakaoApiClient.getKakaoUserInfo(token)).thenReturn(null);
//
//        // When
//        kakaoTokenProcessor.processToken(request, response);
//
//        // Then
//        verify(kakaoApiClient).getKakaoUserInfo(token);
//        verifyNoInteractions(userDetailsService);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
//    }
//}