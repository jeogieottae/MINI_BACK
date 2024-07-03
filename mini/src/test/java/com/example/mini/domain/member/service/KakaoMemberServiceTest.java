package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.service.KakaoAuthService;
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
import static org.mockito.Mockito.*;

public class KakaoMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private KakaoMemberService kakaoMemberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Kakao 회원 정보 저장 - 새 회원")
    void testSaveNewKakaoMember() {
        // given
        KakaoUserInfo kakaoUserInfo = MemberEntityFixture.getKakaoUserInfo();
        Member newMember = Member.builder()
                .name(kakaoUserInfo.getNickname())
                .nickname(kakaoUserInfo.getNickname())
                .email(kakaoUserInfo.getEmail())
                .password("OAuth password")
                .state(MemberState.ACTIVE)
                .build();

        when(memberRepository.findByEmail(kakaoUserInfo.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        // when
        Member savedMember = kakaoMemberService.saveOrUpdateKakaoMember(kakaoUserInfo);

        // then
        assertNotNull(savedMember);
        assertEquals(kakaoUserInfo.getEmail(), savedMember.getEmail());
        assertEquals(kakaoUserInfo.getNickname(), savedMember.getName());
        assertEquals(kakaoUserInfo.getNickname(), savedMember.getNickname());
        assertEquals(MemberState.ACTIVE, savedMember.getState());
        verify(memberRepository).findByEmail(kakaoUserInfo.getEmail());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Kakao 회원 정보 업데이트 - 기존 회원")
    void testUpdateExistingKakaoMember() {
        // given
        KakaoUserInfo kakaoUserInfo = MemberEntityFixture.getKakaoUserInfo();
        Member existingMember = Member.builder()
                .name("Old Name")
                .nickname("Old Nickname")
                .email(kakaoUserInfo.getEmail())
                .password("OAuth password")
                .state(MemberState.INACTIVE)
                .build();

        when(memberRepository.findByEmail(kakaoUserInfo.getEmail())).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Member updatedMember = kakaoMemberService.saveOrUpdateKakaoMember(kakaoUserInfo);

        // then
        assertNotNull(updatedMember);
        assertEquals(kakaoUserInfo.getEmail(), updatedMember.getEmail());
        assertEquals(kakaoUserInfo.getNickname(), updatedMember.getName());
        assertEquals(MemberState.ACTIVE, updatedMember.getState());
        verify(memberRepository).findByEmail(kakaoUserInfo.getEmail());
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
        kakaoMemberService.setMemberInactive(email);

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
                () -> kakaoMemberService.setMemberInactive(email));
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
        kakaoMemberService.setMemberInactive(email);

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
        kakaoMemberService.withdrawMember(email);

        // then
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).delete(member);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 회원 not found")
    void testWithdrawMemberFailUserNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class,
                () -> kakaoMemberService.withdrawMember(email));
        assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository, never()).delete(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 데이터베이스 오류")
    void testWithdrawMemberFailDatabaseError() {
        // given
        String email = "test@example.com";
        Member member = Member.builder()
                .email(email)
                .name("Test User")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        doThrow(new RuntimeException("Database error")).when(memberRepository).delete(member);

        // when & then
        assertThrows(RuntimeException.class, () -> kakaoMemberService.withdrawMember(email));
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).delete(member);
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
        kakaoMemberService.updateNickname(email, newNickname);

        // then
        assertEquals(newNickname, member.getNickname());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).save(member);
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
                () -> kakaoMemberService.updateNickname(email, newNickname));
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
        kakaoMemberService.updateNickname(email, nickname);

        // then
        assertEquals(nickname, member.getNickname());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Kakao 사용자 정보 조회 성공")
    void testGetKakaoUserInfoSuccess() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);
        KakaoUserInfo mockUserInfo = MemberEntityFixture.getKakaoUserInfo();
        Member mockMember = Member.builder()
                .email(mockUserInfo.getEmail())
                .name(mockUserInfo.getNickname())
                .nickname("testNickname")
                .build();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);
            when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
            when(memberRepository.findByEmail(mockUserInfo.getEmail())).thenReturn(Optional.of(mockMember));

            // when
            UserProfileResponse result = kakaoMemberService.getKakaoUserInfo(request);

            // then
            assertNotNull(result);
            assertEquals(mockMember.getEmail(), result.getEmail());
            assertEquals(mockMember.getName(), result.getName());
            assertEquals(mockMember.getNickname(), result.getNickname());
            verify(kakaoApiClient).getKakaoUserInfo(accessToken);
            verify(memberRepository).findByEmail(mockUserInfo.getEmail());
        }
    }

    @Test
    @DisplayName("Kakao 사용자 정보 조회 실패 - 유효하지 않은 액세스 토큰")
    void testGetKakaoUserInfoFailInvalidAccessToken() {
        // given
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(null);

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () -> kakaoMemberService.getKakaoUserInfo(request));
            assertEquals(AuthErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Kakao 사용자 정보 조회 실패 - 사용자 정보 조회 실패")
    void testGetKakaoUserInfoFailUserInfoRetrieval() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);
            when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenThrow(new RuntimeException("Failed to get user info"));

            // when & then
            assertThrows(RuntimeException.class, () -> kakaoMemberService.getKakaoUserInfo(request));
            verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        }
    }

    @Test
    @DisplayName("Kakao 사용자 정보 조회 실패 - 회원 정보 없음")
    void testGetKakaoUserInfoFailMemberNotFound() {
        // given
        String accessToken = "validAccessToken";
        Cookie accessTokenCookie = new Cookie("kakaoAccessToken", accessToken);
        KakaoUserInfo mockUserInfo = MemberEntityFixture.getKakaoUserInfo();

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "kakaoAccessToken")).thenReturn(accessTokenCookie);
            when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(mockUserInfo);
            when(memberRepository.findByEmail(mockUserInfo.getEmail())).thenReturn(Optional.empty());

            // when & then
            GlobalException exception = assertThrows(GlobalException.class, () -> kakaoMemberService.getKakaoUserInfo(request));
            assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            verify(kakaoApiClient).getKakaoUserInfo(accessToken);
            verify(memberRepository).findByEmail(mockUserInfo.getEmail());
        }
    }
}