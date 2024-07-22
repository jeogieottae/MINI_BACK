package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GoogleMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private GoogleApiClient googleApiClient;

    @InjectMocks
    private GoogleMemberService googleMemberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Google 회원 정보 저장 - 새 회원")
    void testSaveNewGoogleMember() {
        // given
        GoogleUserInfo googleUserInfo = MemberEntityFixture.getGoogleUserInfo();
        Member newMember = Member.builder()
                .name(googleUserInfo.getName())
                .nickname(googleUserInfo.getGivenName())
                .email(googleUserInfo.getEmail())
                .password("OAuth password")
                .state(MemberState.ACTIVE)
                .build();

        when(memberRepository.findByEmail(googleUserInfo.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        // when
        Member savedMember = googleMemberService.saveOrUpdateGoogleMember(googleUserInfo);

        // then
        assertNotNull(savedMember);
        assertEquals(googleUserInfo.getEmail(), savedMember.getEmail());
        assertEquals(googleUserInfo.getName(), savedMember.getName());
        assertEquals(googleUserInfo.getGivenName(), savedMember.getNickname());
        assertEquals(MemberState.ACTIVE, savedMember.getState());
        verify(memberRepository).findByEmail(googleUserInfo.getEmail());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Google 회원 정보 업데이트 - 기존 회원")
    void testUpdateExistingGoogleMember() {
        // given
        GoogleUserInfo googleUserInfo = MemberEntityFixture.getGoogleUserInfo();
        Member existingMember = Member.builder()
                .name("Old Name")
                .nickname("Old Nickname")
                .email(googleUserInfo.getEmail())
                .password("OAuth password")
                .state(MemberState.INACTIVE)
                .build();

        when(memberRepository.findByEmail(googleUserInfo.getEmail())).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Member updatedMember = googleMemberService.saveOrUpdateGoogleMember(googleUserInfo);

        // then
        assertNotNull(updatedMember);
        assertEquals(googleUserInfo.getEmail(), updatedMember.getEmail());
        assertEquals(googleUserInfo.getName(), updatedMember.getName());
        assertEquals("Old Nickname", updatedMember.getNickname()); // 닉네임은 변경되지 않아야 함
        assertEquals(MemberState.ACTIVE, updatedMember.getState());
        verify(memberRepository).findByEmail(googleUserInfo.getEmail());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 비활성화 성공")
    void testSetMemberInactiveSuccess() {
        // given
        String email = "test@example.com";
        Member activeMember = Member.builder()
                .email(email)
                .state(MemberState.ACTIVE)
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(activeMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        googleMemberService.setMemberInactive(email);

        // then
        assertEquals(MemberState.INACTIVE, activeMember.getState());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).save(activeMember);
    }

    @Test
    @DisplayName("회원 비활성화 실패 - 회원 not found")
    void testSetMemberInactiveFailUserNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class,
                () -> googleMemberService.setMemberInactive(email));
        assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 비활성화 - 이미 비활성화된 회원")
    void testSetMemberInactiveAlreadyInactive() {
        // given
        String email = "inactive@example.com";
        Member inactiveMember = Member.builder()
                .email(email)
                .state(MemberState.INACTIVE)
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(inactiveMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        googleMemberService.setMemberInactive(email);

        // then
        assertEquals(MemberState.INACTIVE, inactiveMember.getState());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).save(inactiveMember);
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void testWithdrawMemberSuccess() {
        // given
        String email = "test@example.com";
        Member member = Member.builder()
                .email(email)
                .name("Test User")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        doNothing().when(memberRepository).delete(member);

        // when
        googleMemberService.withdrawMember(email);

        // then
        verify(memberRepository).findByEmail(email);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 회원 not found")
    void testWithdrawMemberFailUserNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class,
                () -> googleMemberService.withdrawMember(email));
        assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository, never()).delete(any(Member.class));
    }

    @Test
    @DisplayName("닉네임 업데이트 성공")
    void testUpdateNicknameSuccess() {
        // given
        String email = "test@example.com";
        String newNickname = "newNickname";
        Member member = Member.builder()
                .email(email)
                .nickname("oldNickname")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        googleMemberService.updateNickname(email, newNickname);

        // then
        assertEquals(newNickname, member.getNickname());
        verify(memberRepository).findByEmail(email);
    }

    @Test
    @DisplayName("닉네임 업데이트 실패 - 회원 not found")
    void testUpdateNicknameFailUserNotFound() {
        // given
        String email = "nonexistent@example.com";
        String newNickname = "newNickname";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class,
                () -> googleMemberService.updateNickname(email, newNickname));
        assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("닉네임 업데이트 - 같은 닉네임으로 변경")
    void testUpdateNicknameSameNickname() {
        // given
        String email = "test@example.com";
        String nickname = "sameNickname";
        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        googleMemberService.updateNickname(email, nickname);

        // then
        assertEquals(nickname, member.getNickname());
        verify(memberRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Google 사용자 정보 조회 성공")
    void testGetGoogleUserInfoSuccess() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("googleAccessToken", accessToken);
        GoogleUserInfo mockUserInfo = AuthServiceTestFixture.getGoogleUserInfo();
        Member mockMember = Member.builder()
                .email(mockUserInfo.getEmail())
                .name(mockUserInfo.getName())
                .nickname("testNickname")
                .build();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(accessTokenCookie);
            when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(mockUserInfo);
            when(memberRepository.findByEmail(mockUserInfo.getEmail())).thenReturn(Optional.of(mockMember));

            // when
            UserProfileResponse result = googleMemberService.getGoogleUserInfo(request);

            // then
            assertNotNull(result);
            assertEquals(mockMember.getEmail(), result.getEmail());
            assertEquals(mockMember.getName(), result.getName());
            assertEquals(mockMember.getNickname(), result.getNickname());
            verify(googleApiClient).getGoogleUserInfo(accessToken);
            verify(memberRepository).findByEmail(mockUserInfo.getEmail());
        }
    }

    @Test
    @DisplayName("Google 사용자 정보 조회 실패 - 유효하지 않은 액세스 토큰")
    void testGetGoogleUserInfoFailInvalidAccessToken() {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(null);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () -> googleMemberService.getGoogleUserInfo(request));
            assertEquals(AuthErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Google 사용자 정보 조회 실패 - 사용자 정보 조회 실패")
    void testGetGoogleUserInfoFailUserInfoRetrieval() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("googleAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(accessTokenCookie);
            when(googleApiClient.getGoogleUserInfo(accessToken)).thenThrow(new RuntimeException("Failed to get user info"));

            // when & then
            assertThrows(RuntimeException.class, () -> googleMemberService.getGoogleUserInfo(request));
            verify(googleApiClient).getGoogleUserInfo(accessToken);
        }
    }

    @Test
    @DisplayName("Google 사용자 정보 조회 실패 - 회원 정보 없음")
    void testGetGoogleUserInfoFailMemberNotFound() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("googleAccessToken", accessToken);
        GoogleUserInfo mockUserInfo = AuthServiceTestFixture.getGoogleUserInfo();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "googleAccessToken")).thenReturn(accessTokenCookie);
            when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(mockUserInfo);
            when(memberRepository.findByEmail(mockUserInfo.getEmail())).thenReturn(Optional.empty());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () -> googleMemberService.getGoogleUserInfo(request));
            assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(googleApiClient).getGoogleUserInfo(accessToken);
            verify(memberRepository).findByEmail(mockUserInfo.getEmail());
        }
    }
}
