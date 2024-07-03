package com.example.mini.global.security.token;

import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.service.StandardAuthService;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.util.cookies.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProcessor implements TokenProcessor {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void processToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = jwtProvider.resolveToken(request);
        if (token != null) {
            if (tokenService.isTokenBlacklisted(token)) {
                handleBlacklistedToken(response);
                return;
            }

            if (jwtProvider.validateToken(token, TokenType.ACCESS)) {
                Claims claims = jwtProvider.getUserInfoFromToken(token, TokenType.ACCESS);
                Date expiration = claims.getExpiration();
                Date now = new Date();
                long diff = (expiration.getTime() - now.getTime()) / 1000;

                if (diff < 300) {
                    log.info("JWT 토큰 재발급 필요");
                    String newAccessToken = refreshToken(request, response);
                    log.info("JWT 토큰 재발급: {}", newAccessToken);
                    setAuthentication(jwtProvider.getEmailFromToken(newAccessToken, TokenType.ACCESS));
                } else {
                    setAuthentication(jwtProvider.getEmailFromToken(token, TokenType.ACCESS));
                }
            } else {
                handleInvalidToken(response);
            }
        }
    }

    private String refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, "refreshToken");
        if (refreshTokenCookie == null) {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        String refreshToken = refreshTokenCookie.getValue();
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
        CookieUtil.addCookie(response, "accessToken", newAccessToken, TokenType.ACCESS.getExpireTime() / 1000,true);
        log.info("Access 토큰 재발급: 이메일={}, NewAccessToken={}", email, newAccessToken);
        return newAccessToken;
    }

    private void setAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByEmail(email);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        sendErrorResponse(response, AuthErrorCode.BLACKLISTED_TOKEN);
    }

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getCode().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.ERROR(errorCode)));
    }
}