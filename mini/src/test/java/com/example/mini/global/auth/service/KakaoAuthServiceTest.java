package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KakaoAuthServiceTest {

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private KakaoMemberService kakaoMemberService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Test
    @DisplayName("kakaoLogout_성공")
    void testKakaoLogoutSuccess() {
        // given
        String accessToken = "validKakaoAccessToken";
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();
        Cookie mockCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(mockCookie);
            when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
            when(request.getSession(false)).thenReturn(session);

            // when
            kakaoAuthService.kakaoLogout(request, response);

            // then
            verify(kakaoMemberService).setMemberInactive("test@example.com");
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "kakaoAccessToken"));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "kakaoRefreshToken"));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "JSESSIONID"));
            verify(session).invalidate();
        }
    }

    @Test
    @DisplayName("kakaoLogout_실패_액세스토큰_없음")
    void testKakaoLogoutFailNoAccessToken() {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(null);

            // when & then
            assertThrows(NullPointerException.class, () -> kakaoAuthService.kakaoLogout(request, response));
        }
    }

    @Test
    @DisplayName("kakaoLogout_실패_사용자정보_조회실패")
    void testKakaoLogoutFailUserInfoRetrievalFailed() {
        // given
        String accessToken = "invalidKakaoAccessToken";
        Cookie mockCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(mockCookie);
            when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenThrow(new RuntimeException("사용자 정보 조회 실패"));

            // when & then
            assertThrows(RuntimeException.class, () -> kakaoAuthService.kakaoLogout(request, response));
        }
    }

    @Test
    @DisplayName("카카오 콜백 성공")
    void testKakaoCallbackSuccess() {
        // given
        KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
        String code = "validAuthorizationCode";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();
        Member mockMember = Member.builder()
                .email("test@example.com")
                .state(MemberState.ACTIVE)
                .build();

        doReturn(mockTokenResponse).when(spyKakaoAuthService).authenticateKakao(code);
        when(kakaoApiClient.getKakaoUserInfo(mockTokenResponse.getAccess_token())).thenReturn(mockUserInfo);
        when(kakaoMemberService.saveOrUpdateKakaoMember(mockUserInfo)).thenReturn(mockMember);

        // when
        LoginResponse result = spyKakaoAuthService.kakaoCallback(code);

        // then
        assertNotNull(result);
        assertEquals(MemberState.ACTIVE, result.getState());
        assertEquals("access_token", result.getAccessToken());
        assertEquals("refresh_token", result.getRefreshToken());

        verify(spyKakaoAuthService).authenticateKakao(code);
        verify(kakaoApiClient).getKakaoUserInfo(mockTokenResponse.getAccess_token());
        verify(kakaoMemberService).saveOrUpdateKakaoMember(mockUserInfo);
    }

    @Test
    @DisplayName("카카오 콜백 실패 - 인증 실패")
    void testKakaoCallbackFailAuthentication() {
        // given
        KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
        String code = "invalidAuthorizationCode";
        doThrow(new RuntimeException("인증 실패")).when(spyKakaoAuthService).authenticateKakao(code);

        // when & then
        assertThrows(RuntimeException.class, () -> spyKakaoAuthService.kakaoCallback(code));
        verify(spyKakaoAuthService).authenticateKakao(code);
        verifyNoInteractions(kakaoApiClient);
        verifyNoInteractions(kakaoMemberService);
    }

    @Test
    @DisplayName("카카오 콜백 실패 - 사용자 정보 조회 실패")
    void testKakaoCallbackFailUserInfoRetrieval() {
        // given
        KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
        String code = "validAuthorizationCode";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
        doReturn(mockTokenResponse).when(spyKakaoAuthService).authenticateKakao(code);
        when(kakaoApiClient.getKakaoUserInfo(mockTokenResponse.getAccess_token())).thenThrow(new RuntimeException("사용자 정보 조회 실패"));

        // when & then
        assertThrows(RuntimeException.class, () -> spyKakaoAuthService.kakaoCallback(code));
        verify(spyKakaoAuthService).authenticateKakao(code);
        verify(kakaoApiClient).getKakaoUserInfo(mockTokenResponse.getAccess_token());
        verifyNoInteractions(kakaoMemberService);
    }

    @Test
    @DisplayName("카카오 리프레시 토큰 갱신 성공")
    void testKakaoRefreshSuccess() {
        // given
        String refreshToken = "validRefreshToken";
        Cookie refreshTokenCookie = new Cookie("kakaoRefreshToken", refreshToken);
        TokenResponse expectedTokenResponse = AuthServiceTestFixture.createTokenResponse();
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoRefreshToken")).thenReturn(refreshTokenCookie);
            KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
            doReturn(expectedTokenResponse).when(spyKakaoAuthService).refreshKakaoToken(refreshToken);

            // when
            TokenResponse actualTokenResponse = spyKakaoAuthService.kakaoRefresh(request);

            // then
            assertNotNull(actualTokenResponse);
            assertEquals(expectedTokenResponse.getAccess_token(), actualTokenResponse.getAccess_token());
            assertEquals(expectedTokenResponse.getRefresh_token(), actualTokenResponse.getRefresh_token());
            assertEquals(expectedTokenResponse.getExpires_in(), actualTokenResponse.getExpires_in());
            verify(spyKakaoAuthService).refreshKakaoToken(refreshToken);
        }
    }

    @Test
    @DisplayName("카카오 리프레시 토큰 갱신 실패 - 리프레시 토큰 없음")
    void testKakaoRefreshFailNoRefreshToken() {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoRefreshToken")).thenReturn(null);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () -> kakaoAuthService.kakaoRefresh(request));
            assertEquals(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("카카오 리프레시 토큰 갱신 실패 - 토큰 갱신 실패")
    void testKakaoRefreshFailTokenRefreshFailed() {
        // given
        String refreshToken = "invalidRefreshToken";
        Cookie refreshTokenCookie = new Cookie("kakaoRefreshToken", refreshToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoRefreshToken")).thenReturn(refreshTokenCookie);
            KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
            doThrow(new RuntimeException("Token refresh failed")).when(spyKakaoAuthService).refreshKakaoToken(refreshToken);

            // when & then
            assertThrows(RuntimeException.class, () -> spyKakaoAuthService.kakaoRefresh(request));
            verify(spyKakaoAuthService).refreshKakaoToken(refreshToken);
        }
    }

    @Test
    @DisplayName("카카오 회원 탈퇴 성공")
    void testWithdrawSuccess() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);
            when(request.getSession(false)).thenReturn(session);

            KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
            doNothing().when(spyKakaoAuthService).withdrawMember(accessToken);

            // when
            spyKakaoAuthService.withdraw(request, response);

            // then
            verify(spyKakaoAuthService).withdrawMember(accessToken);
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "kakaoAccessToken"));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "kakaoRefreshToken"));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "JSESSIONID"));
            verify(session).invalidate();
        }
    }

    @Test
    @DisplayName("카카오 회원 탈퇴 실패 - 유효하지 않은 액세스 토큰")
    void testWithdrawFailInvalidAccessToken() {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(null);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () -> kakaoAuthService.withdraw(request, response));
            assertEquals(AuthErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("카카오 회원 탈퇴 실패 - 회원 탈퇴 과정 중 오류 발생")
    void testWithdrawFailDuringWithdrawProcess() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);

            KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
            doThrow(new RuntimeException("회원 탈퇴 실패")).when(spyKakaoAuthService).withdrawMember(accessToken);

            // when & then
            assertThrows(RuntimeException.class, () -> spyKakaoAuthService.withdraw(request, response));
            verify(spyKakaoAuthService).withdrawMember(accessToken);
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "kakaoAccessToken"), never());
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "kakaoRefreshToken"), never());
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "JSESSIONID"), never());
            verify(session, never()).invalidate();
        }
    }

    @Test
    @DisplayName("카카오 닉네임 변경 성공")
    void testChangeNicknameSuccess() {
        // given
        String accessToken = "validAccessToken";
        String newNickname = "newNickname";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);
            KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
            doNothing().when(spyKakaoAuthService).updateNickname(accessToken, newNickname);

            // when
            spyKakaoAuthService.changeNickname(request, newNickname);

            // then
            verify(spyKakaoAuthService).updateNickname(accessToken, newNickname);
        }
    }

    @Test
    @DisplayName("카카오 닉네임 변경 실패 - 유효하지 않은 액세스 토큰")
    void testChangeNicknameFailInvalidAccessToken() {
        // given
        String newNickname = "newNickname";

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(null);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> kakaoAuthService.changeNickname(request, newNickname));
            assertEquals(AuthErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("카카오 닉네임 변경 실패 - 닉네임 업데이트 과정 중 오류 발생")
    void testChangeNicknameFailDuringUpdate() {
        // given
        String accessToken = "validAccessToken";
        String newNickname = "newNickname";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);
            KakaoAuthService spyKakaoAuthService = spy(kakaoAuthService);
            doThrow(new RuntimeException("닉네임 업데이트 실패")).when(spyKakaoAuthService).updateNickname(accessToken, newNickname);

            // when & then
            assertThrows(RuntimeException.class, () -> spyKakaoAuthService.changeNickname(request, newNickname));
            verify(spyKakaoAuthService).updateNickname(accessToken, newNickname);
        }
    }

    @Test
    @DisplayName("카카오 인증 성공")
    void testAuthenticateKakaoSuccess() {
        // given
        String code = "validAuthorizationCode";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();
        Member mockMember = Member.builder().email("test@example.com").build();
        UserDetails mockUserDetails = mock(UserDetails.class);

        when(kakaoApiClient.getToken(code)).thenReturn(mockTokenResponse);
        when(kakaoApiClient.getKakaoUserInfo(mockTokenResponse.getAccess_token())).thenReturn(mockUserInfo);
        when(kakaoMemberService.saveOrUpdateKakaoMember(mockUserInfo)).thenReturn(mockMember);
        when(userDetailsService.loadUserByEmail(mockMember.getEmail())).thenReturn(mockUserDetails);
        when(mockUserDetails.getAuthorities()).thenReturn(Collections.emptyList());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {

            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // when
            TokenResponse result = kakaoAuthService.authenticateKakao(code);

            // then
            assertNotNull(result);
            assertEquals(mockTokenResponse, result);
            verify(kakaoApiClient).getToken(code);
            verify(kakaoApiClient).getKakaoUserInfo(mockTokenResponse.getAccess_token());
            verify(kakaoMemberService).saveOrUpdateKakaoMember(mockUserInfo);
            verify(userDetailsService).loadUserByEmail(mockMember.getEmail());
            verify(securityContext).setAuthentication(any(Authentication.class));
            mockedCookieUtil.verify(() -> CookieUtil.addCookie(eq(response), eq("kakaoAccessToken"), eq("access_token"), eq(3600L)));
            mockedCookieUtil.verify(() -> CookieUtil.addCookie(eq(response), eq("kakaoRefreshToken"), eq("refresh_token"), eq(3600L)));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("카카오 인증 실패 - 토큰 획득 실패")
    void testAuthenticateKakaoFailGetToken() {
        // given
        String code = "invalidAuthorizationCode";
        when(kakaoApiClient.getToken(code)).thenThrow(new RuntimeException("Failed to get Kakao token"));

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.authenticateKakao(code));
        verify(kakaoApiClient).getToken(code);
        verifyNoInteractions(kakaoMemberService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("카카오 인증 실패 - 사용자 정보 조회 실패")
    void testAuthenticateKakaoFailGetUserInfo() {
        // given
        String code = "validAuthorizationCode";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
        when(kakaoApiClient.getToken(code)).thenReturn(mockTokenResponse);
        when(kakaoApiClient.getKakaoUserInfo(mockTokenResponse.getAccess_token())).thenThrow(new RuntimeException("Failed to get user info"));

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.authenticateKakao(code));
        verify(kakaoApiClient).getToken(code);
        verify(kakaoApiClient).getKakaoUserInfo(mockTokenResponse.getAccess_token());
        verifyNoInteractions(kakaoMemberService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("카카오 토큰 리프레시 성공")
    void testRefreshKakaoTokenSuccess() {
        // given
        String refreshToken = "validRefreshToken";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
        when(kakaoApiClient.getRefreshedToken(refreshToken)).thenReturn(mockTokenResponse);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // when
            TokenResponse result = kakaoAuthService.refreshKakaoToken(refreshToken);

            // then
            assertNotNull(result);
            assertEquals(mockTokenResponse, result);
            verify(kakaoApiClient).getRefreshedToken(refreshToken);
            mockedCookieUtil.verify(() -> CookieUtil.addCookie(eq(response), eq("kakaoAccessToken"), eq("access_token"), eq(3600L)));
            mockedCookieUtil.verify(() -> CookieUtil.addCookie(eq(response), eq("kakaoRefreshToken"), eq("refresh_token"), eq(3600L)));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("카카오 토큰 리프레시 실패 - API 호출 실패")
    void testRefreshKakaoTokenFailApiCall() {
        // given
        String refreshToken = "invalidRefreshToken";
        when(kakaoApiClient.getRefreshedToken(refreshToken)).thenThrow(new RuntimeException("Failed to refresh token"));

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.refreshKakaoToken(refreshToken));
        verify(kakaoApiClient).getRefreshedToken(refreshToken);
    }

    @Test
    @DisplayName("카카오 회원 탈퇴 성공")
    void testWithdrawMemberSuccess() {
        // given
        String accessToken = "validAccessToken";
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();

        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
        doNothing().when(kakaoMemberService).withdrawMember(mockUserInfo.getEmail());

        // when
        kakaoAuthService.withdrawMember(accessToken);

        // then
        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verify(kakaoMemberService).withdrawMember(mockUserInfo.getEmail());
    }

    @Test
    @DisplayName("카카오 회원 탈퇴 실패 - 사용자 정보 조회 실패")
    void testWithdrawMemberFailGetUserInfo() {
        // given
        String accessToken = "invalidAccessToken";
        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenThrow(new RuntimeException("Failed to get user info"));

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.withdrawMember(accessToken));
        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verifyNoInteractions(kakaoMemberService);
    }

    @Test
    @DisplayName("카카오 회원 탈퇴 실패 - 회원 탈퇴 처리 실패")
    void testWithdrawMemberFailWithdrawProcess() {
        // given
        String accessToken = "validAccessToken";
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();

        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
        doThrow(new RuntimeException("Failed to withdraw member")).when(kakaoMemberService).withdrawMember(mockUserInfo.getEmail());

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.withdrawMember(accessToken));
        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verify(kakaoMemberService).withdrawMember(mockUserInfo.getEmail());
    }

    @Test
    @DisplayName("카카오 닉네임 변경 성공")
    void testUpdateNicknameSuccess() {
        // given
        String accessToken = "validAccessToken";
        String newNickname = "newNickname";
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();

        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
        doNothing().when(kakaoMemberService).updateNickname(mockUserInfo.getEmail(), newNickname);

        // when
        kakaoAuthService.updateNickname(accessToken, newNickname);

        // then
        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verify(kakaoMemberService).updateNickname(mockUserInfo.getEmail(), newNickname);
    }

    @Test
    @DisplayName("카카오 닉네임 변경 실패 - 사용자 정보 조회 실패")
    void testUpdateNicknameFailGetUserInfo() {
        // given
        String accessToken = "invalidAccessToken";
        String newNickname = "newNickname";
        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenThrow(new RuntimeException("Failed to get user info"));

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.updateNickname(accessToken, newNickname));
        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verifyNoInteractions(kakaoMemberService);
    }

    @Test
    @DisplayName("카카오 닉네임 변경 실패 - 닉네임 업데이트 실패")
    void testUpdateNicknameFailUpdateProcess() {
        // given
        String accessToken = "validAccessToken";
        String newNickname = "newNickname";
        KakaoUserInfo mockUserInfo = AuthServiceTestFixture.getKakaoUserInfo();

        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
        doThrow(new RuntimeException("Failed to update nickname")).when(kakaoMemberService).updateNickname(mockUserInfo.getEmail(), newNickname);

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoAuthService.updateNickname(accessToken, newNickname));
        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verify(kakaoMemberService).updateNickname(mockUserInfo.getEmail(), newNickname);
    }

}