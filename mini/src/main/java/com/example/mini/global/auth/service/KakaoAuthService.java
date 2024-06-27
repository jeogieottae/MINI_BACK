package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final MemberRepository memberRepository;


    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";

    public TokenResponse getKakaoToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URI, request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {

            TokenResponse body = response.getBody();

            // 토큰을 쿠키에 저장
            HttpServletResponse HttpResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

            String kakaoAccessToken = body.getAccess_token();
            Integer kakaoAccessTokenExpiresIn = body.getExpires_in();
            String kakaoRefreshToken = body.getRefresh_token();
            Integer refreshTokenExpiresIn = body.getRefresh_token_expires_in();

            CookieUtil.addCookie(HttpResponse, "kakaoAccessToken", kakaoAccessToken, kakaoAccessTokenExpiresIn);
            CookieUtil.addCookie(HttpResponse, "kakaoRefreshToken", kakaoRefreshToken, refreshTokenExpiresIn);
            log.info("AccessToken 쿠키 설정: {}", kakaoAccessToken);
            log.info("RefreshToken 쿠키 설정: {}", kakaoRefreshToken);

            return body;
        } else {
            throw new RuntimeException("카카오 토큰 받기 실패");
        }
    }

    public KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> attributes = response.getBody();

        return new KakaoUserInfo(attributes);
    }

    public Member saveKakaoMember(KakaoUserInfo kakaoUserInfo) {

        String email = kakaoUserInfo.getEmail();
        String name = kakaoUserInfo.getNickname();

        Member member = memberRepository.findByEmail(email)
                .map(entity -> entity.update(name))
                .orElse(Member.builder()
                        .name(name)
                        .email(email)
                        .password("OAuth password")
                        .build());
        member.setState(MemberState.ACTIVE);
        memberRepository.save(member);


        // SecurityContext에 인증 정보 저장
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SecurityContext에 인증 정보 저장 완료: {}", authentication);

        return member;
    }

    public TokenResponse getKakaoRefreshedToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("refresh_token", refreshToken);
        params.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URI, request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            TokenResponse body = response.getBody();

            // 토큰을 쿠키에 저장
            HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

            String kakaoAccessToken = body.getAccess_token();
            Integer kakaoAccessTokenExpiresIn = body.getExpires_in();

            CookieUtil.addCookie(httpResponse, "kakaoAccessToken", kakaoAccessToken, kakaoAccessTokenExpiresIn);
            log.info("Refreshed AccessToken 쿠키 설정: {}", kakaoAccessToken);

            // 새로운 리프레시 토큰이 발급된 경우에만 쿠키 업데이트
            if (body.getRefresh_token() != null) {
                String kakaoRefreshToken = body.getRefresh_token();
                Integer refreshTokenExpiresIn = body.getRefresh_token_expires_in();
                CookieUtil.addCookie(httpResponse, "kakaoRefreshToken", kakaoRefreshToken, refreshTokenExpiresIn);
                log.info("Refreshed RefreshToken 쿠키 설정: {}", kakaoRefreshToken);
            }

            return body;
        } else {
            throw new RuntimeException("카카오 토큰 갱신 실패");
        }
    }

    public void setMemberInactive(String accessToken) {
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);
        Member member = memberRepository.findByEmail(kakaoUserInfo.getEmail()).get();
        member.setState(MemberState.INACTIVE);
        memberRepository.save(member);
    }
}
