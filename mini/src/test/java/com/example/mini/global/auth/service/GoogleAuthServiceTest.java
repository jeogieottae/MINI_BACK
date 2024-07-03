package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

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

    public static Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("email", "test@example.com");
        attributes.put("email_verified", true);
        attributes.put("name", "홍길동");
        attributes.put("given_name", "길동");
        attributes.put("family_name", "홍");
        attributes.put("picture", "https://example.com/profile.jpg");
        attributes.put("locale", "ko");
        return attributes;
    }

    public static GoogleUserInfo getGoogleUserInfo() {
        return new GoogleUserInfo(getAttributes());
    }

    @Test
    @DisplayName("googleLogout_성공")
    void testGoogleLogoutSuccess() {
        // given
        String accessToken = "validGoogleAccessToken";
        GoogleUserInfo mockUserInfo = getGoogleUserInfo();

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

//    @Test
//    @DisplayName("googleCallback_성공")
//    void testGoogleCallbackSuccess() {
//        // given
//        String code = "validAuthorizationCode";
//        TokenResponse mockTokenResponse = TokenResponse.builder()
//                .access_token("validAccessToken")
//                .refresh_token("validRefreshToken")
//                .build();
//
//        GoogleUserInfo mockUserInfo = getGoogleUserInfo();
//
//        Member mockMember = Member.builder()
//                .email("test@example.com")
//                .state(MemberState.ACTIVE)
//                .build();
//
//        when(googleAuthService.authenticateGoogle(code)).thenReturn(mockTokenResponse);
//        when(googleApiClient.getGoogleUserInfo("validAccessToken")).thenReturn(mockUserInfo);
//        when(googleMemberService.saveOrUpdateGoogleMember(mockUserInfo)).thenReturn(mockMember);
//
//        // when
//        LoginResponse response = googleAuthService.googleCallback(code);
//
//        // then
//        assertNotNull(response);
//
//        verify(googleAuthService).authenticateGoogle(code);
//        verify(googleApiClient).getGoogleUserInfo("validAccessToken");
//        verify(googleMemberService).saveOrUpdateGoogleMember(mockUserInfo);
//    }


    @Test
    @DisplayName("회원 탈퇴 테스트")
    void testWithdrawMember() {
        // Given
        String accessToken = "testAccessToken";
        GoogleUserInfo googleUserInfo = getGoogleUserInfo();

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
        GoogleUserInfo googleUserInfo = getGoogleUserInfo();

        when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(googleUserInfo);

        // When
        googleAuthService.updateNickname(accessToken, newNickname);

        // Then
        verify(googleApiClient).getGoogleUserInfo(accessToken);
        verify(googleMemberService).updateNickname(googleUserInfo.getEmail(), newNickname);
    }
}