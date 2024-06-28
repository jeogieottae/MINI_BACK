package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GoogleMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private GoogleMemberService googleMemberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

    // 테스트용 GoogleUserInfo 객체를 생성하는 메서드
    public static GoogleUserInfo getGoogleUserInfo() {
        return new GoogleUserInfo(getAttributes());
    }

    @Test
    @DisplayName("Google 회원 저장 또는 업데이트 테스트")
    void saveOrUpdateGoogleMemberTest() {
        GoogleUserInfo googleUserInfo = getGoogleUserInfo();
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .nickname("Test")
                .password("OAuth password")
                .build();

        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = googleMemberService.saveOrUpdateGoogleMember(googleUserInfo);

        assertNotNull(result);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 비활성화 테스트")
    void setMemberInactiveTest() {
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .nickname("Test")
                .password("OAuth password")
                .state(MemberState.ACTIVE)
                .build();

        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.of(member));

        googleMemberService.setMemberInactive("test@example.com");

        assertEquals(MemberState.INACTIVE, member.getState());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void withdrawMemberTest() {
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .nickname("Test")
                .password("OAuth password")
                .build();

        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.of(member));

        googleMemberService.withdrawMember("test@example.com");

        verify(memberRepository, times(1)).delete(member);
    }

    @Test
    @DisplayName("닉네임 업데이트 테스트")
    void updateNicknameTest() {
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .nickname("OldNickname")
                .password("OAuth password")
                .build();

        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.of(member));

        googleMemberService.updateNickname("test@example.com", "NewNickname");

        assertEquals("NewNickname", member.getNickname());
        verify(memberRepository, times(1)).save(member);
    }


}
