package com.example.mini.global.security.filter;

import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.service.GoogleAuthService;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final UserDetailsServiceImpl userDetailsService;
	private final TokenService tokenService;
	private final KakaoAuthService kakaoAuthService;
	private final GoogleAuthService googleAuthService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		Cookie googleCookie = CookieUtil.getCookie(request, "googleAccessToken");
		String googleToken = googleCookie != null ? googleCookie.getValue() : null;

		Cookie kakaoCookie = CookieUtil.getCookie(request, "kakaoAccessToken");
		String kakaoToken = kakaoCookie != null ? kakaoCookie.getValue() : null;

		String token = jwtProvider.resolveToken(request);

		log.info("google 토큰: {}", googleToken);
		log.info("kakao 토큰: {}", kakaoToken);
		log.info("일반 토큰: {}", token);

		// 받은 토큰이 유효한지 확인
		if (googleToken != null) {
			log.info("google 토큰 유효성 확인: {}", googleToken);
			processToken(googleToken, "google", request);
		} else if (kakaoToken != null) {
			log.info("kakao 토큰 유효성 확인: {}", kakaoToken);
			processToken(kakaoToken, "kakao", request);
		} else if (token != null) {
			if (tokenService.isTokenBlacklisted(token)) {
				log.warn("블랙리스트에 등록된 토큰: {}", token);
				AuthErrorCode blacklistedToken = AuthErrorCode.BLACKLISTED_TOKEN;

				// JSON 응답 전송
				response.setContentType("text/plain;charset=UTF-8");
				response.setStatus(blacklistedToken.getCode().value());
				response.setContentType("application/json");
				response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.ERROR(blacklistedToken)));
				return;
			}

			// Access Token 검증
			if (jwtProvider.validateToken(token, TokenType.ACCESS)) {
				Claims claims = jwtProvider.getUserInfoFromToken(token, TokenType.ACCESS);
				log.info("토큰 유효: 사용자 이메일={}", claims.getSubject());

				UserDetails userDetails;
				userDetails = userDetailsService.loadUserByEmail(claims.getSubject());

				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				log.warn("유효하지 않은 토큰: {}", token);
				AuthErrorCode invalidToken = AuthErrorCode.INVALID_TOKEN;

				// JSON 응답 전송
				response.setContentType("text/plain;charset=UTF-8");
				response.setStatus(invalidToken.getCode().value());
				response.setContentType("application/json");
				response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.ERROR(invalidToken)));
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private void processToken(String token, String provider, HttpServletRequest request) {
		TokenInfo tokenInfo = validateAndGetTokenInfo(token, provider);
		if (tokenInfo != null && tokenInfo.isValid()) {
			UserDetails userDetails = userDetailsService.loadUserByOauthEmail(tokenInfo.getEmail());
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
	}

	private TokenInfo validateAndGetTokenInfo(String token, String provider) {
		Function<Map<String, Object>, TokenInfo> infoExtractor;

		if (provider.equals("google")) {
			GoogleUserInfo googleUserInfo = googleAuthService.getGoogleUserInfo(token);
			return new TokenInfo(googleUserInfo.getEmail() != null, googleUserInfo.getEmail());
		} else if (provider.equals("kakao")) {
			KakaoUserInfo kakaoUserInfo = kakaoAuthService.getKakaoUserInfo(token);
			return new TokenInfo(kakaoUserInfo.getEmail() != null, kakaoUserInfo.getEmail());
		} else {
			return null; // 지원하지 않는 제공자
		}
	}

	private static class TokenInfo {
		private final boolean valid;
		private final String email;

		TokenInfo(boolean valid, String email) {
			this.valid = valid;
			this.email = email;
		}

		public boolean isValid() {
			return valid;
		}

		public String getEmail() {
			return email;
		}
	}

}