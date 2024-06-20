package com.example.mini.global.security.filter;

import com.example.mini.global.exception.error.AuthErrorCode;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.util.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
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

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final UserDetailsServiceImpl userDetailsService;
	private final TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String token = jwtProvider.resolveToken(request);

		if (token != null) {
			if (tokenService.isTokenBlacklisted(token)) {
				log.warn("블랙리스트에 등록된 토큰: {}", token);
				AuthErrorCode blacklistedToken = AuthErrorCode.BLACKLISTED_TOKEN;

				// JSON 응답 전송
				response.setContentType("text/plain;charset=UTF-8");
				response.setStatus(blacklistedToken.getHttpStatus().value());
				response.setContentType("application/json");
				response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.ERROR(blacklistedToken)));
				return;
			}

			if (jwtProvider.validateToken(token)) {
				Claims claims = jwtProvider.getUserInfoFromToken(token);
				log.info("토큰 유효: 사용자 이메일={}", claims.getSubject());
				UserDetails userDetails;
				if (claims.containsKey("oauth")) {
					userDetails = userDetailsService.loadUserByOauthEmail(claims.getSubject());
				} else {
					userDetails = userDetailsService.loadUserByEmail(claims.getSubject());
				}
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				log.warn("유효하지 않은 토큰: {}", token);
				AuthErrorCode invalidToken = AuthErrorCode.INVALID_TOKEN;

				// JSON 응답 전송
				response.setContentType("text/plain;charset=UTF-8");
				response.setStatus(invalidToken.getHttpStatus().value());
				response.setContentType("application/json");
				response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.ERROR(invalidToken)));
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}
