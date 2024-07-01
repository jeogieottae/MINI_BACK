package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleMemberService {

    private final MemberRepository memberRepository;
    private final GoogleApiClient googleApiClient;

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
        memberRepository.delete(member); //todo : db에서 아예 삭제할지, MemberState를 delete로 할지 고민
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));
        member.setNickname(nickname);
        memberRepository.save(member);
    }

    @Transactional
    public UserProfileResponse getGoogleUserInfo(HttpServletRequest request) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "googleAccessToken");
        if(accessTokenCookie == null){
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = accessTokenCookie.getValue();
        GoogleUserInfo userInfo = googleApiClient.getGoogleUserInfo(accessToken);
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
