package com.example.mini.global.security.filter;


import com.example.mini.global.security.token.TokenProcessor;
import com.example.mini.global.security.token.TokenProcessorFactory;
import com.example.mini.global.util.cookies.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


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