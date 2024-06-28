package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.model.KakaoUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KakaoMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoUserInfo kakaoUserInfo;

    @InjectMocks
    private KakaoMemberService kakaoMemberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("새 카카오 회원 저장 테스트")
    void testSaveNewKakaoMember() {
        // given
        String email = "test@example.com";
        String name = "Test User";
        when(kakaoUserInfo.getEmail()).thenReturn(email);
        when(kakaoUserInfo.getNickname()).thenReturn(name);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArguments()[0]);

        // when
        Member savedMember = kakaoMemberService.saveOrUpdateKakaoMember(kakaoUserInfo);

        // then
        assertNotNull(savedMember);
        assertEquals(email, savedMember.getEmail());
        assertEquals(name, savedMember.getName());
        assertEquals(MemberState.ACTIVE, savedMember.getState());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("기존 카카오 회원 업데이트 테스트")
    void testUpdateExistingKakaoMember() {
        // given
        String email = "test@example.com";
        String name = "Updated User";
        Member existingMember = new Member().builder()
                .email(email)
                .name("Old Name")
                .build();

        when(kakaoUserInfo.getEmail()).thenReturn(email);
        when(kakaoUserInfo.getNickname()).thenReturn(name);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArguments()[0]);

        // when
        Member updatedMember = kakaoMemberService.saveOrUpdateKakaoMember(kakaoUserInfo);

        // then
        assertNotNull(updatedMember);
        assertEquals(email, updatedMember.getEmail());
        assertEquals(name, updatedMember.getName());
        assertEquals(MemberState.ACTIVE, updatedMember.getState());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 비활성화 테스트")
    void testSetMemberInactive() {
        // given
        String email = "test@example.com";
        Member member = new Member();
        member.setEmail(email);
        member.setState(MemberState.ACTIVE);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArguments()[0]);

        // when
        kakaoMemberService.setMemberInactive(email);

        // then
        assertEquals(MemberState.INACTIVE, member.getState());
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void testWithdrawMember() {
        // given
        String email = "test@example.com";
        Member member = new Member();
        member.setEmail(email);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        kakaoMemberService.withdrawMember(email);

        // then
        verify(memberRepository).delete(member);
    }

    @Test
    @DisplayName("닉네임 업데이트 테스트")
    void testUpdateNickname() {
        // given
        String email = "test@example.com";
        String newNickname = "New Nickname";
        Member member = new Member();
        member.setEmail(email);
        member.setNickname("Old Nickname");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArguments()[0]);

        // when
        kakaoMemberService.updateNickname(email, newNickname);

        // then
        assertEquals(newNickname, member.getNickname());
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외 발생 테스트")
    void testUserNotFoundException() {
        // given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(GlobalException.class, () -> kakaoMemberService.setMemberInactive(email));
        assertThrows(GlobalException.class, () -> kakaoMemberService.withdrawMember(email));
        assertThrows(GlobalException.class, () -> kakaoMemberService.updateNickname(email, "New Nickname"));
    }
}