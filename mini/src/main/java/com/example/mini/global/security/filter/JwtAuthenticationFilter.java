package com.example.mini.global.security.filter;

import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
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
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("블랙리스트에 등록된 토큰입니다.");
				return;
			}

			if (jwtProvider.validateToken(token)) {
				Claims claims = jwtProvider.getUserInfoFromToken(token);
				log.info("토큰 유효: 사용자 이메일={}", claims.getSubject());
				UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				log.warn("유효하지 않은 토큰: {}", token);
			}
		}

		filterChain.doFilter(request, response);
	}
}
