package com.example.mini.global.auth.oauth2.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.oauth2.model.KakaoMemberDetails;
import com.example.mini.global.auth.oauth2.model.KakaoUserInfo;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class KakaoMemberDetailsService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final TokenService tokenService;
	private final HttpServletResponse response;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// DefaultOAuth2UserService의 기본 구현을 호출하여 OAuth2User를 로드합니다.
		OAuth2User oAuth2User = super.loadUser(userRequest);

		// 로드된 OAuth2User의 속성에서 카카오 사용자 정보를 추출합니다.
		KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

		// 데이터베이스에서 카카오 이메일로 사용자를 조회하거나 새 사용자로 등록합니다.
		Member member = memberRepository.findByOauthEmail(kakaoUserInfo.getEmail())
			.orElseGet(() ->
				memberRepository.save(
					Member.builder()
						.email(null)  // 일반 로그인용 이메일은 null로 설정
						.oauthEmail(kakaoUserInfo.getEmail())
						.name(kakaoUserInfo.getNickname())
						.build()
				)
			);

		// 기본 권한을 설정합니다.
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

		// KakaoMemberDetails 객체를 생성합니다.
		KakaoMemberDetails kakaoMemberDetails = new KakaoMemberDetails(String.valueOf(member.getOauthEmail()),
			Collections.singletonList(authority),
			oAuth2User.getAttributes());

		// Authentication 객체를 생성하여 SecurityContext에 저장합니다.
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			kakaoMemberDetails, null, kakaoMemberDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// JWT 생성 및 Redis에 저장
		String accessToken = jwtProvider.createToken(member.getOauthEmail(), TokenType.ACCESS, true); // OAuth 로그인
		String refreshToken = jwtProvider.createToken(member.getOauthEmail(), TokenType.REFRESH, true); // OAuth 로그인
		tokenService.saveRefreshToken(member.getOauthEmail(), refreshToken);

		// JWT를 응답 헤더에 포함시킵니다.
		response.setHeader("Authorization", "Bearer " + accessToken);
		response.setHeader("Refresh-Token", refreshToken);

		return kakaoMemberDetails;
	}
}
