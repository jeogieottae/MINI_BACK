package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.model.GoogleUserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleMemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member saveOrUpdateGoogleMember(GoogleUserInfo googleUserInfo) {
        String email = googleUserInfo.getEmail();
        String name = googleUserInfo.getName();
        String givenName = googleUserInfo.getGivenName();

        Member member = memberRepository.findByEmail(email)
                .map(entity -> entity.update(name))
                .orElse(Member.builder()
                        .name(name)
                        .nickname(givenName)
                        .email(email)
                        .password("OAuth password")
                        .build());

        member.setState(MemberState.ACTIVE);
        return memberRepository.save(member);
    }

    @Transactional
    public void setMemberInactive(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));
        member.setState(MemberState.INACTIVE);
        memberRepository.save(member);
    }

    @Transactional
    public void withdrawMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));
        memberRepository.delete(member);
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));
        member.setNickname(nickname);
        memberRepository.save(member);
    }
}
