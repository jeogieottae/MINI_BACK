package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
public class GoogleAuthService {

    private final GoogleApiClient googleApiClient;
    private final GoogleMemberService googleMemberService;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public String getGoogleAuthUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
            + "?client_id=" + googleClientId
            + "&redirect_uri=" + redirectUri
            + "&response_type=code"
            + "&scope=email%20profile"
            + "&access_type=offline"
            + "&prompt=consent";
    }

    public void googleLogout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = CookieUtil.getCookie(request, "googleAccessToken").getValue();
        GoogleUserInfo googleUserInfo = googleApiClient.getGoogleUserInfo(accessToken);
        googleMemberService.setMemberInactive(googleUserInfo.getEmail());

        CookieUtil.deleteCookie(response, "googleAccessToken", true);
        CookieUtil.deleteCookie(response, "googleRefreshToken", true);
        CookieUtil.deleteCookie(response, "JSESSIONID", true);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public LoginResponse googleCallback(String code) {
        TokenResponse tokenResponse = authenticateGoogle(code);
        GoogleUserInfo googleUserInfo = googleApiClient.getGoogleUserInfo(tokenResponse.getAccess_token());
        Member member = googleMemberService.saveOrUpdateGoogleMember(googleUserInfo);

        return LoginResponse.builder()
            .state(member.getState())
            .accessToken(tokenResponse.getAccess_token())
            .refreshToken(tokenResponse.getRefresh_token())
            .build();
    }

    public TokenResponse googleRefresh(HttpServletRequest request) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, "googleRefreshToken");
        if (refreshTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        return refreshGoogleToken(refreshTokenCookie.getValue());
    }

    public void withdraw(HttpServletRequest request, HttpServletResponse response) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "googleAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        withdrawMember(accessTokenCookie.getValue());

        CookieUtil.deleteCookie(response, "googleAccessToken", true);
        CookieUtil.deleteCookie(response, "googleRefreshToken", true);
        CookieUtil.deleteCookie(response, "JSESSIONID", true);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public void changeNickname(HttpServletRequest request, String newNickname) {
        Cookie accessTokenCookie = CookieUtil.getCookie(request, "googleAccessToken");
        if (accessTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = accessTokenCookie.getValue();
        updateNickname(accessToken, newNickname);
    }

    public TokenResponse authenticateGoogle(String code) {
        TokenResponse tokenResponse = googleApiClient.getGoogleToken(code);
        GoogleUserInfo userInfo = googleApiClient.getGoogleUserInfo(tokenResponse.getAccess_token());
        Member member = googleMemberService.saveOrUpdateGoogleMember(userInfo);

        setSecurityContext(member.getEmail());
        setTokenCookies(tokenResponse);

        return tokenResponse;
    }

    public TokenResponse refreshGoogleToken(String refreshToken) {
        TokenResponse tokenResponse = googleApiClient.getGoogleRefreshedToken(refreshToken);
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
        CookieUtil.addCookie(httpResponse, "googleAccessToken", tokenResponse.getAccess_token(), tokenResponse.getExpires_in(), true);
        log.info("AccessToken 쿠키 설정: {}", tokenResponse.getAccess_token());

        if (tokenResponse.getRefresh_token() != null) {
            CookieUtil.addCookie(httpResponse, "googleRefreshToken", tokenResponse.getRefresh_token(), 30 * 24 * 60 * 60, true);
            log.info("RefreshToken 쿠키 설정: {}", tokenResponse.getRefresh_token());
        }
    }

    public void withdrawMember(String accessToken) {
        GoogleUserInfo userInfo = googleApiClient.getGoogleUserInfo(accessToken);
        googleMemberService.withdrawMember(userInfo.getEmail());
        log.info("Google 회원 탈퇴 성공: 이메일={}", userInfo.getEmail());
    }

    public void updateNickname(String accessToken, String nickname) {
        GoogleUserInfo userInfo = googleApiClient.getGoogleUserInfo(accessToken);
        googleMemberService.updateNickname(userInfo.getEmail(), nickname);
        log.info("닉네임 변경 성공: 이메일={}, 새 닉네임={}", userInfo.getEmail(), nickname);
    }
}

