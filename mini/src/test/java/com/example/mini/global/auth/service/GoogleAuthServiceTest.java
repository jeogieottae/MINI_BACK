package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.model.GoogleUserInfo;
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
import org.springframework.mock.web.MockHttpSession;
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
public class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private GoogleApiClient googleApiClient;

    @Mock
    private GoogleMemberService googleMemberService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("googleLogout_성공")
    void testGoogleLogoutSuccess() {
        // given
        String accessToken = "validGoogleAccessToken";
        GoogleUserInfo mockUserInfo = AuthServiceTestFixture.getGoogleUserInfo();

        Cookie mockCookie = new Cookie("googleAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(mockCookie);
            when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(mockUserInfo);
            when(request.getSession(false)).thenReturn(session);

            // when
            googleAuthService.googleLogout(request, response);

            // then
            verify(googleMemberService).setMemberInactive("test@example.com");
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "googleAccessToken",true));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "googleRefreshToken",true));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "JSESSIONID",true));
            verify(session).invalidate();
        }
    }

    @Test
    @DisplayName("구글 리프레시 토큰 갱신 성공")
    void testGoogleRefreshSuccess() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        String refreshToken = "validRefreshToken";
        Cookie refreshTokenCookie = new Cookie("googleRefreshToken", refreshToken);
        request.setCookies(refreshTokenCookie);

        TokenResponse expectedTokenResponse = AuthServiceTestFixture.createTokenResponse();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleRefreshToken")).thenReturn(refreshTokenCookie);
            mockedCookieUtil.when(() -> CookieUtil.addCookie(any(), anyString(), anyString(), anyInt())).thenAnswer(invocation -> null);

            when(googleApiClient.getRefreshedToken(refreshToken)).thenReturn(expectedTokenResponse);

            // when
            TokenResponse actualTokenResponse = googleAuthService.googleRefresh(request);

            // then
            assertNotNull(actualTokenResponse);
            assertEquals(expectedTokenResponse.getAccess_token(), actualTokenResponse.getAccess_token());
            assertEquals(expectedTokenResponse.getRefresh_token(), actualTokenResponse.getRefresh_token());
            assertEquals(expectedTokenResponse.getExpires_in(), actualTokenResponse.getExpires_in());

            verify(googleApiClient).getRefreshedToken(refreshToken);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("구글 리프레시 토큰 갱신 실패_리프레시 토큰 없음")
    void testGoogleRefreshFail() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        String refreshToken = "validRefreshToken";
        Cookie refreshTokenCookie = new Cookie("googleRefreshToken", refreshToken);
        request.setCookies(refreshTokenCookie);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleRefreshToken")).thenReturn(null);

            // when & then
            assertThrows(RuntimeException.class, () -> googleAuthService.googleRefresh(request));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdrawSuccess() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("googleAccessToken", accessToken);
        request.setCookies(accessTokenCookie);

        GoogleUserInfo mockUserInfo = AuthServiceTestFixture.getGoogleUserInfo();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(accessTokenCookie);

            when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(mockUserInfo);
            doNothing().when(googleMemberService).withdrawMember(mockUserInfo.getEmail());

            // when
            googleAuthService.withdraw(request, response);

            // then
            verify(googleApiClient).getGoogleUserInfo(accessToken);
            verify(googleMemberService).withdrawMember(mockUserInfo.getEmail());

            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "googleAccessToken"));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "googleRefreshToken"));
            mockedCookieUtil.verify(() -> CookieUtil.deleteCookie(response, "JSESSIONID"));
        }
    }

    @Test
    @DisplayName("회원 탈퇴 실패_유효하지 않은 리프레시 토큰")
    void withdrawFail() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> googleAuthService.withdraw(request, response));
        }
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void changeNicknameSuccess() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("googleAccessToken", accessToken);
        request.setCookies(accessTokenCookie);

        String newNickname = "newNickname";
        GoogleUserInfo mockUserInfo = AuthServiceTestFixture.getGoogleUserInfo();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(accessTokenCookie);

            when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(mockUserInfo);
            doNothing().when(googleMemberService).updateNickname(mockUserInfo.getEmail(), newNickname);

            // when
            googleAuthService.changeNickname(request, newNickname);

            // then
            verify(googleApiClient).getGoogleUserInfo(accessToken);
            verify(googleMemberService).updateNickname(mockUserInfo.getEmail(), newNickname);
        }
    }

    @Test
    @DisplayName("닉네임 변경 실패_유효하지 않은 액세스 토큰")
    void changeNicknameFailInvalidAccessToken() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String newNickname = "newNickname";

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> googleAuthService.changeNickname(request, newNickname));
        }
    }

    @Test
    @DisplayName("닉네임 변경 실패_사용자 정보 조회 실패")
    void changeNicknameFailToGetUserInfo() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("googleAccessToken", accessToken);
        request.setCookies(accessTokenCookie);

        String newNickname = "newNickname";

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(accessTokenCookie);

            when(googleApiClient.getGoogleUserInfo(accessToken)).thenThrow(new RuntimeException("Failed to get user info"));

            // when & then
            assertThrows(RuntimeException.class, () -> googleAuthService.changeNickname(request, newNickname));
            verify(googleApiClient).getGoogleUserInfo(accessToken);
            verify(googleMemberService, never()).updateNickname(anyString(), anyString());
        }
    }

    @Test
    @DisplayName("Google 인증 성공")
    void authenticateGoogleSuccess() {
        // given
        String code = "validAuthorizationCode";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();
        GoogleUserInfo mockUserInfo = AuthServiceTestFixture.getGoogleUserInfo();
        Member mockMember = Member.builder().email("test@example.com").build();

        when(googleApiClient.getToken(code)).thenReturn(mockTokenResponse);
        when(googleApiClient.getGoogleUserInfo(mockTokenResponse.getAccess_token())).thenReturn(mockUserInfo);
        when(googleMemberService.saveOrUpdateGoogleMember(mockUserInfo)).thenReturn(mockMember);

        UserDetails mockUserDetails = mock(UserDetails.class);
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
            TokenResponse result = googleAuthService.authenticateGoogle(code);

            // then
            assertNotNull(result);
            assertEquals(mockTokenResponse, result);

            verify(googleApiClient).getToken(code);
            verify(googleApiClient).getGoogleUserInfo(mockTokenResponse.getAccess_token());
            verify(googleMemberService).saveOrUpdateGoogleMember(mockUserInfo);
            verify(userDetailsService).loadUserByEmail(mockMember.getEmail());
            verify(securityContext).setAuthentication(any(Authentication.class));

            mockedCookieUtil.verify(() ->
                    CookieUtil.addCookie(eq(response), eq("googleAccessToken"), eq("access_token"), eq(3600L))
            );
            mockedCookieUtil.verify(() ->
                    CookieUtil.addCookie(eq(response), eq("googleRefreshToken"), eq("refresh_token"), eq(2592000L))
            );
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Google 토큰 리프레시 성공")
    void refreshGoogleTokenSuccess() {
        // given
        String refreshToken = "validRefreshToken";
        TokenResponse mockTokenResponse = AuthServiceTestFixture.createTokenResponse();

        when(googleApiClient.getRefreshedToken(refreshToken)).thenReturn(mockTokenResponse);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // when
            TokenResponse result = googleAuthService.refreshGoogleToken(refreshToken);

            // then
            assertNotNull(result);
            assertEquals(mockTokenResponse, result);

            verify(googleApiClient).getRefreshedToken(refreshToken);

            mockedCookieUtil.verify(() ->
                    CookieUtil.addCookie(eq(response), eq("googleAccessToken"), eq("access_token"), eq(3600L))
            );
            mockedCookieUtil.verify(() ->
                    CookieUtil.addCookie(eq(response), eq("googleRefreshToken"), eq("refresh_token"), eq(2592000L))
            );
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("회원 탈퇴(Member 삭제) 테스트")
    void testWithdrawMember() {
        // Given
        String accessToken = "testAccessToken";
        GoogleUserInfo googleUserInfo = AuthServiceTestFixture.getGoogleUserInfo();

        when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(googleUserInfo);

        // When
        googleAuthService.withdrawMember(accessToken);

        // Then
        verify(googleApiClient).getGoogleUserInfo(accessToken);
        verify(googleMemberService).withdrawMember(googleUserInfo.getEmail());
    }

    @Test
    @DisplayName("닉네임 업데이트 테스트")
    void testUpdateNickname() {
        // Given
        String accessToken = "testAccessToken";
        String newNickname = "새로운닉네임";
        GoogleUserInfo googleUserInfo = AuthServiceTestFixture.getGoogleUserInfo();

        when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(googleUserInfo);

        // When
        googleAuthService.updateNickname(accessToken, newNickname);

        // Then
        verify(googleApiClient).getGoogleUserInfo(accessToken);
        verify(googleMemberService).updateNickname(googleUserInfo.getEmail(), newNickname);
    }
}