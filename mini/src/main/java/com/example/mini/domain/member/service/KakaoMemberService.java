package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoMemberService {

    private final MemberRepository memberRepository;
    private final KakaoApiClient kakaoApiClient;

    @Transactional
    public Member saveOrUpdateKakaoMember(KakaoUserInfo kakaoUserInfo) {
        String email = kakaoUserInfo.getEmail();
        String name = kakaoUserInfo.getNickname();

        Member member = memberRepository.findByEmail(email)
                .map(entity -> entity.update(name))
                .orElse(Member.builder()
                        .name(name)
                        .nickname(name)
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

    @Transactional(readOnly = true)
    public UserProfileResponse getKakaoUserInfo(HttpServletRequest request) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "kakaoAccessToken");
        if(accessTokenCookie == null){
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = accessTokenCookie.getValue();
        KakaoUserInfo userInfo = kakaoApiClient.getKakaoUserInfo(accessToken);
        String email = userInfo.getEmail();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .build();
    }
}
