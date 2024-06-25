package com.example.mini.global.auth.oauth2.util;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.oauth2.model.KakaoUserInfo;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	// oauth 로그인 성공 시 JWT 생성 후 클라이언트로 리다이렉트

	private static final String REDIRECT_URI = "http://localhost:8080/api/protected/home";

	private final JwtProvider jwtProvider;
	private final TokenService tokenService;
	private final MemberRepository memberRepository;

	@Transactional
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

		Member member = memberRepository.findByEmail(kakaoUserInfo.getEmail())
			.orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

		// JWT 생성
		String accessToken = jwtProvider.createToken(member.getEmail(), TokenType.ACCESS, true);
		String refreshToken = jwtProvider.createToken(member.getEmail(), TokenType.REFRESH, true);
		tokenService.saveRefreshToken(member.getEmail(), refreshToken);

		// 리다이렉트 URL 생성
		String redirectURI = String.format(REDIRECT_URI, accessToken, refreshToken);
		getRedirectStrategy().sendRedirect(request, response, redirectURI);
	}
}