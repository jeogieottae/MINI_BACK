package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StandardAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("회원가입 시도: 이메일={}, 이름={}, 닉네임={}", request.getEmail(), request.getName(), request.getNickname());

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new GlobalException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new GlobalException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .state(MemberState.INACTIVE)
                .build();

        memberRepository.save(member);

        log.info("회원가입 성공: 이메일={}", member.getEmail());
    }



    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: 이메일={}", request.getEmail());
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new GlobalException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        String accessToken = jwtProvider.createToken(member.getEmail(), TokenType.ACCESS, false);
        String refreshToken = jwtProvider.createToken(member.getEmail(), TokenType.REFRESH, false);
        tokenService.saveRefreshToken(member.getEmail(), refreshToken);

        member.setState(MemberState.ACTIVE);

        log.info("로그인 성공: 이메일={}, AccessToken={}, RefreshToken={}", member.getEmail(), accessToken, refreshToken);
        return LoginResponse.builder()
                .state(member.getState())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public String standardRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, "refreshToken");
        if (refreshTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        String newAccessToken = createAccessToken(refreshTokenCookie.getValue());
        addAccessTokenCookie(response, newAccessToken);
        log.info("재발급된 Access 토큰을 쿠키에 저장: NewAccessToken={}", newAccessToken);

        return newAccessToken;
    }

    @Transactional
    public String createAccessToken(String refreshToken) {
        Claims claims = jwtProvider.getUserInfoFromToken(refreshToken, TokenType.REFRESH);
        String email = claims.getSubject();
        String storedRefreshToken = tokenService.getRefreshToken(email);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!jwtProvider.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.createToken(email, TokenType.ACCESS, false);
        log.info("Access 토큰 재발급: 이메일={}, NewAccessToken={}", email, newAccessToken);
        return newAccessToken;
    }

    @Transactional
    public void standardLogout(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = Objects.requireNonNull(CookieUtil.getCookie(request, "accessToken")).getValue();
        String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

        member.setState(MemberState.INACTIVE);

        tokenService.blacklistToken(accessToken);

        deleteTokenCookies(response);

        log.info("로그아웃 성공: 이메일={}", email);
    }

    @Transactional
    public void standardWithdraw(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = Objects.requireNonNull(CookieUtil.getCookie(request, "accessToken")).getValue();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

        memberRepository.delete(member);
        tokenService.blacklistToken(accessToken);
        tokenService.removeToken(tokenService.getRefreshToken(email));

        deleteTokenCookies(response);

        log.info("회원 탈퇴 성공: 이메일={}", email);
    }

    @Transactional
    public void standardUpdateNickname(HttpServletRequest request, String newNickname) {
        String accessToken = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

        member.setNickname(newNickname);
        memberRepository.save(member);

        log.info("닉네임 변경 성공: 이메일={}, 새 닉네임={}", email, newNickname);
    }



    @Transactional
    public UserProfileResponse getStandardUserInfo(HttpServletRequest request) {
        String accessToken = Objects.requireNonNull(CookieUtil.getCookie(request, "accessToken")).getValue();
        String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.builder()
                .name(member.getName())
                .nickname(member.getNickname())
                .email(email)
                .build();
    }

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        CookieUtil.addCookie(response, "accessToken", accessToken, TokenType.ACCESS.getExpireTime() / 1000);
        CookieUtil.addCookie(response, "refreshToken", refreshToken, TokenType.REFRESH.getExpireTime() / 1000);
    }

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        CookieUtil.addCookie(response, "accessToken", accessToken, TokenType.ACCESS.getExpireTime() / 1000);
    }

    public void deleteTokenCookies(HttpServletResponse response) {
        CookieUtil.deleteCookie(response, "accessToken");
        CookieUtil.deleteCookie(response, "refreshToken");
    }
}
