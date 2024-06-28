package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoApiClient kakaoApiClient;
    private final KakaoMemberService kakaoMemberService;
    private final UserDetailsServiceImpl userDetailsService;

    public TokenResponse authenticateKakao(String code) {
        TokenResponse tokenResponse = kakaoApiClient.getKakaoToken(code);
        KakaoUserInfo userInfo = kakaoApiClient.getKakaoUserInfo(tokenResponse.getAccess_token());
        Member member = kakaoMemberService.saveOrUpdateKakaoMember(userInfo);

        setSecurityContext(member.getEmail());
        setTokenCookies(tokenResponse);

        return tokenResponse;
    }

    public TokenResponse refreshKakaoToken(String refreshToken) {
        TokenResponse tokenResponse = kakaoApiClient.getKakaoRefreshedToken(refreshToken);
        setTokenCookies(tokenResponse);
        return tokenResponse;
    }

    private void setSecurityContext(String email) {
        UserDetails userDetails = userDetailsService.loadUserByEmail(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SecurityContext에 인증 정보 저장 완료: {}", authentication);
    }

    private void setTokenCookies(TokenResponse tokenResponse) {
        HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(httpResponse, "kakaoAccessToken", tokenResponse.getAccess_token(), tokenResponse.getExpires_in());
        log.info("AccessToken 쿠키 설정: {}", tokenResponse.getAccess_token());

        if (tokenResponse.getRefresh_token() != null) {
            CookieUtil.addCookie(httpResponse, "kakaoRefreshToken", tokenResponse.getRefresh_token(), tokenResponse.getRefresh_token_expires_in());
            log.info("RefreshToken 쿠키 설정: {}", tokenResponse.getRefresh_token());
        }
    }

    public void withdrawMember(String accessToken) {
        KakaoUserInfo userInfo = kakaoApiClient.getKakaoUserInfo(accessToken);
        kakaoMemberService.withdrawMember(userInfo.getEmail());
        log.info("Kakao 회원 탈퇴 성공: 이메일={}", userInfo.getEmail());
    }

    public void updateNickname(String accessToken, String nickname) {
        KakaoUserInfo userInfo = kakaoApiClient.getKakaoUserInfo(accessToken);
        kakaoMemberService.updateNickname(userInfo.getEmail(), nickname);
        log.info("닉네임 변경 성공: 이메일={}, 새 닉네임={}", userInfo.getEmail(), nickname);
    }
}
