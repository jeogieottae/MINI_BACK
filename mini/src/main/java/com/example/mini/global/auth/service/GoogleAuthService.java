package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
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
public class GoogleAuthService {

    private final MemberRepository memberRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    public TokenResponse getGoogleToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URI, request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            TokenResponse body = response.getBody();

            // 토큰을 쿠키에 저장
            HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

            String googleAccessToken = body.getAccess_token();
            Integer googleAccessTokenExpiresIn = body.getExpires_in();

            CookieUtil.addCookie(httpResponse, "googleAccessToken", googleAccessToken, googleAccessTokenExpiresIn);
            log.info("AccessToken 쿠키 설정: {}", googleAccessToken);

            // 리프레시 토큰이 있는 경우에만 쿠키에 저장
            if (body.getRefresh_token() != null) {
                String googleRefreshToken = body.getRefresh_token();
                // 리프레시 토큰의 만료 시간이 없음 임의로 30일로 정함
                CookieUtil.addCookie(httpResponse, "googleRefreshToken", googleRefreshToken, 30 * 24 * 60 * 60);
                log.info("RefreshToken 쿠키 설정: {}", googleRefreshToken);
            }

            return body;
        } else {
            throw new RuntimeException("구글 토큰 받기 실패");
        }
    }

    public GoogleUserInfo getGoogleUserInfo(String googleAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + googleAccessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                USER_INFO_URI,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> attributes = response.getBody();

        return new GoogleUserInfo(attributes);
    }

    public Member saveGoogleMember(GoogleUserInfo googleUserInfo) {
        String email = googleUserInfo.getEmail();
        String name = googleUserInfo.getName();

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
        UserDetails userDetails;
        userDetails = userDetailsService.loadUserByEmail(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("SecurityContext에 인증 정보 저장 완료: {}", authentication);

        return member;
    }

    public TokenResponse getGoogleRefreshedToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URI, request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            TokenResponse body = response.getBody();

            HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

            String googleAccessToken = body.getAccess_token();
            Integer googleAccessTokenExpiresIn = body.getExpires_in();

            CookieUtil.addCookie(httpResponse, "googleAccessToken", googleAccessToken, googleAccessTokenExpiresIn);
            log.info("Refreshed AccessToken 쿠키 설정: {}", googleAccessToken);

            // 새로운 리프레시 토큰이 발급된 경우 쿠키 업데이트
            // 리프레시 토큰의 만료 시간이 없음 임의로 30일로 정함
            if (body.getRefresh_token() != null) {
                String newRefreshToken = body.getRefresh_token();
                CookieUtil.addCookie(httpResponse, "googleRefreshToken", newRefreshToken, 30 * 24 * 60 * 60);
                log.info("New RefreshToken 쿠키 설정: {}", newRefreshToken);
            }

            return body;
        } else {
            throw new RuntimeException("구글 토큰 갱신 실패");
        }
    }

    public void setMemberInactive(String accessToken) {
        GoogleUserInfo googleUserInfo = getGoogleUserInfo(accessToken);
        Member member = memberRepository.findByEmail(googleUserInfo.getEmail()).get();
        member.setState(MemberState.INACTIVE);
        memberRepository.save(member);
    }
}