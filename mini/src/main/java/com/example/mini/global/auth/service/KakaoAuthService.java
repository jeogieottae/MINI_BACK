package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoApiClient kakaoApiClient;
    private final KakaoMemberService kakaoMemberService;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private String kakaoLogoutRedirectUri = "http://localhost:8080/api/protected/home";

    public String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
            + "?client_id=" + kakaoClientId
            + "&redirect_uri=" + redirectUri
            + "&response_type=code";
    }

    public void kakaoLogout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = CookieUtil.getCookie(request, "kakaoAccessToken").getValue();
        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(accessToken);
        kakaoMemberService.setMemberInactive(kakaoUserInfo.getEmail());

        CookieUtil.deleteCookie(response, "kakaoAccessToken");
        CookieUtil.deleteCookie(response, "kakaoRefreshToken");
        CookieUtil.deleteCookie(response, "JSESSIONID");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public LoginResponse kakaoCallback(String code) {
        TokenResponse tokenResponse = authenticateKakao(code);
        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(tokenResponse.getAccess_token());
        Member member = kakaoMemberService.saveOrUpdateKakaoMember(kakaoUserInfo);

        return LoginResponse.builder()
            .state(member.getState())
            .accessToken(tokenResponse.getAccess_token())
            .refreshToken(tokenResponse.getRefresh_token())
            .build();
    }

    public void kakaoRefresh(HttpServletRequest request) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, "kakaoRefreshToken");
        if (refreshTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        refreshKakaoToken(refreshTokenCookie.getValue());
    }

    public void withdraw(HttpServletRequest request, HttpServletResponse response) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "kakaoAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        withdrawMember(accessTokenCookie.getValue());

        CookieUtil.deleteCookie(response, "kakaoAccessToken");
        CookieUtil.deleteCookie(response, "kakaoRefreshToken");
        CookieUtil.deleteCookie(response, "JSESSIONID");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public void changeNickname(HttpServletRequest request, String newNickname) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "kakaoAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = accessTokenCookie.getValue();
        updateNickname(accessToken, newNickname);
    }

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

    public String getKakaoLogoutRedirectUri() {
        return "https://kauth.kakao.com/oauth/logout?client_id=" + kakaoClientId
                + "&logout_redirect_uri=" + kakaoLogoutRedirectUri;
    }
}
