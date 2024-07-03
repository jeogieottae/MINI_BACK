/*
package com.example.mini.global.auth.service;

import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private StandardAuthService standardAuthService;

    @Mock
    private GoogleAuthService googleAuthService;

    @Mock
    private KakaoAuthService kakaoAuthService;

    @Mock
    private GoogleMemberService googleMemberService;

    @Mock
    private KakaoMemberService kakaoMemberService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("refreshToken_구글")
    void testGoogleRefreshToken() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
            when(CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
            when(googleAuthService.googleRefresh(request)).thenReturn(mockTokenResponse);

            // when
            authService.refreshToken(request, response);

            // then
            verify(googleAuthService).googleRefresh(request);
            verify(kakaoAuthService, never()).kakaoRefresh(any());
        }
    }

    @Test
    @DisplayName("refreshToken_카카오")
    void testKakaoRefreshToken() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
            when(CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
            when(kakaoAuthService.kakaoRefresh(request)).thenReturn(mockTokenResponse);

            // when
            authService.refreshToken(request, response);

            // then
            verify(kakaoAuthService).kakaoRefresh(request);
            verify(googleAuthService, never()).googleRefresh(any());
        }
    }

    @Test
    @DisplayName("refreshToken_일반")
    void testStandardRefreshToken() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");

            // when
            authService.refreshToken(request, response);

            // then
            verify(standardAuthService).standardRefreshToken(request, response);
            verify(kakaoAuthService, never()).kakaoRefresh(any());
            verify(googleAuthService, never()).googleRefresh(any());
        }
    }

    @Test
    @DisplayName("refreshToken_실패_지원하는_로그인_방식의_토큰_없음")
    void testFailedRefreshToken() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> authService.refreshToken(request, response));
        }
    }

    @Test
    @DisplayName("logout_구글")
    void testGoogleLogout() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");

            // when
            String result = authService.logout(request, response);

            // then
            verify(googleAuthService).googleLogout(request, response);
            verify(kakaoAuthService, never()).kakaoLogout(any(), any());
        }
    }

    @Test
    @DisplayName("logout_카카오")
    void testKakaoLogout() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");

            // when
            String result = authService.logout(request, response);

            // then
            verify(kakaoAuthService).kakaoLogout(request, response);
            verify(googleAuthService, never()).googleLogout(any(), any());
        }
    }

    @Test
    @DisplayName("logout_일반")
    void testStandardLogout() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");

            // when
            String result = authService.logout(request, response);

            // then
            verify(standardAuthService).standardLogout(request, response);
            verify(kakaoAuthService, never()).kakaoLogout(any(), any());
            verify(googleAuthService, never()).googleLogout(any(), any());
        }
    }

    @Test
    @DisplayName("logout_실패_지원하는_로그인_방식의_토큰_없음")
    void testFailedLogout() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> authService.logout(request, response));
        }
    }

    @Test
    @DisplayName("withdraw_구글")
    void testGoogleWithdraw() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");

            // when
            String result = authService.withdraw(request, response);

            // then
            verify(googleAuthService).withdraw(request, response);
            verify(kakaoAuthService, never()).withdraw(any(), any());
        }
    }

    @Test
    @DisplayName("withdraw_카카오")
    void testKakaoWithdraw() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");

            // when
            String result = authService.withdraw(request, response);

            // then
            verify(kakaoAuthService).withdraw(request, response);
            verify(googleAuthService, never()).withdraw(any(), any());
        }
    }

    @Test
    @DisplayName("withdraw_일반")
    void testStandardWithdraw() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");

            // when
            String result = authService.withdraw(request, response);

            // then
            verify(standardAuthService).standardWithdraw(request, response);
            verify(kakaoAuthService, never()).withdraw(any(), any());
            verify(googleAuthService, never()).withdraw(any(), any());
        }
    }

    @Test
    @DisplayName("withdraw_실패_지원하는_로그인_방식의_토큰_없음")
    void testFailedWithdraw() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> authService.withdraw(request, response));
        }
    }

    @Test
    @DisplayName("updateNickname_구글")
    void testGoogleUpdateNickname() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");

            // when
            authService.updateNickname(request, AuthServiceTestFixture.createChangeNicknameRequest());

            // then
            verify(googleAuthService)
                    .changeNickname(request, AuthServiceTestFixture.createChangeNicknameRequest().getNickname());
            verify(kakaoAuthService, never()).withdraw(any(), any());
        }
    }

    @Test
    @DisplayName("updateNickname_카카오")
    void testKakaoUpdateNickname() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");

            // when
            authService.updateNickname(request, AuthServiceTestFixture.createChangeNicknameRequest());

            // then
            verify(kakaoAuthService)
                    .changeNickname(request, AuthServiceTestFixture.createChangeNicknameRequest().getNickname());
            verify(googleAuthService, never()).withdraw(any(), any());
        }
    }

    @Test
    @DisplayName("updateNickname_일반")
    void testStandardUpdateNickname() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");

            // when
            authService.updateNickname(request, AuthServiceTestFixture.createChangeNicknameRequest());

            // then
            verify(standardAuthService)
                    .standardUpdateNickname(request, AuthServiceTestFixture.createChangeNicknameRequest().getNickname());
            verify(kakaoAuthService, never()).withdraw(any(), any());
            verify(googleAuthService, never()).withdraw(any(), any());
        }
    }

    @Test
    @DisplayName("updateNickname_실패_지원하는_로그인_방식의_토큰_없음")
    void testFailedUpdateNickname() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class,
                    () -> authService.updateNickname(request, AuthServiceTestFixture.createChangeNicknameRequest()));
        }
    }

    @Test
    @DisplayName("getUserInfo_구글")
    void testGetGoogleUserInfo() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
            UserProfileResponse expectedResponse = new UserProfileResponse(); // 적절한 응답 객체 생성
            when(googleMemberService.getGoogleUserInfo(request)).thenReturn(expectedResponse);

            // when
            UserProfileResponse result = authService.getUserInfo(request);

            // then
            verify(googleMemberService).getGoogleUserInfo(request);
            verify(kakaoMemberService, never()).getKakaoUserInfo(any());
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    @DisplayName("getUserInfo_카카오")
    void testGetKakaoUserInfo() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
            UserProfileResponse expectedResponse = new UserProfileResponse(); // 적절한 응답 객체 생성
            when(kakaoMemberService.getKakaoUserInfo(request)).thenReturn(expectedResponse);

            // when
            UserProfileResponse result = authService.getUserInfo(request);

            // then
            verify(kakaoMemberService).getKakaoUserInfo(request);
            verify(googleMemberService, never()).getGoogleUserInfo(any());
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    @DisplayName("getUserInfo_일반")
    void testGetStandardUserInfo() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");
            UserProfileResponse expectedResponse = new UserProfileResponse();
            when(standardAuthService.getStandardUserInfo(request)).thenReturn(expectedResponse);

            // when
            UserProfileResponse result = authService.getUserInfo(request);

            // then
            verify(standardAuthService).getStandardUserInfo(request);
            verify(kakaoMemberService, never()).getKakaoUserInfo(any());
            verify(googleMemberService, never()).getGoogleUserInfo(any());
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    @DisplayName("getUserInfo_실패_지원하는_로그인_방식의_토큰_없음")
    void testFailedGetUserInfo() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> authService.getUserInfo(request));
        }
    }

    @Test
    @DisplayName("isLoggedIn_구글")
    void testGoogleIsLoggedIn() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");

            // when
            Boolean result = authService.isLoggedIn(request);

            // then
            assertEquals(true, result);
        }
    }

    @Test
    @DisplayName("isLoggedIn_카카오")
    void testKakaoIsLoggedIn() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");

            // when
            Boolean result = authService.isLoggedIn(request);

            // then
            assertEquals(true, result);
        }
    }

    @Test
    @DisplayName("isLoggedIn_일반")
    void testStandardIsLoggedIn() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");

            // when
            Boolean result = authService.isLoggedIn(request);

            // then
            assertEquals(true, result);
        }
    }

    @Test
    @DisplayName("isLoggedIn_실패_지원하는_로그인_방식의_토큰_없음")
    void testFailedIsLoggedIn() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn(null);

            // when
            Boolean result = authService.isLoggedIn(request);

            // then
            assertEquals(false, result);
        }
    }
}

*/
