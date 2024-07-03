package com.example.mini.global.security.filter;

import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.security.model.TokenInfo;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.auth.service.GoogleAuthService;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.security.token.TokenProcessor;
import com.example.mini.global.security.token.TokenProcessorFactory;
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
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final TokenProcessorFactory tokenProcessorFactory;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String cookieName = CookieUtil.getCookieNames(request);
		log.info("cookieName: {}", cookieName);
		String tokenType = determineTokenType(cookieName);

		if(tokenType != null){
			TokenProcessor processor = tokenProcessorFactory.getProcessor(tokenType);
			processor.processToken(request, response);
		} else {
			log.warn("No processor found for token type: {}", tokenType);
		}

		filterChain.doFilter(request, response);
	}

	private String determineTokenType(String cookieName) {
		if(cookieName == null) return null;
		if (cookieName.equals("googleAccessToken")) return "Google";
		if (cookieName.equals("kakaoAccessToken")) return "Kakao";
		if (cookieName.equals("accessToken")) return "Jwt";
		return null;
	}

}
