package com.example.mini.global.auth.oauth2.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.oauth2.model.KakaoMemberDetails;
import com.example.mini.global.auth.oauth2.model.KakaoUserInfo;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMemberDetailsService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final TokenService tokenService;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// DefaultOAuth2UserService의 기본 구현을 호출하여 OAuth2User를 로드합니다.
		OAuth2User oAuth2User = super.loadUser(userRequest);

		// 로드된 OAuth2User의 속성에서 카카오 사용자 정보를 추출합니다.
		KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

		// 추출된 사용자 정보를 로그로 출력합니다.
		log.info("Kakao User Info: {}", kakaoUserInfo);

		// 데이터베이스에서 카카오 이메일로 사용자를 조회하거나 새 사용자로 등록합니다.
		Member member = memberRepository.findByOauthEmail(kakaoUserInfo.getEmail())
			.orElseGet(() -> {
				Member newMember = Member.builder()
					.email(kakaoUserInfo.getEmail())  // email 필드도 설정
					.oauthEmail(kakaoUserInfo.getEmail())
					.name(kakaoUserInfo.getNickname())
					.password("default_password") // password 필드도 설정
					.build();
				log.info("Saving new member: {}", newMember);
				Member savedMember = memberRepository.save(newMember);
				log.info("Saved new member: {}", savedMember);
				return savedMember;
			});

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

		// 필요한 경우, 토큰을 응답에 포함하거나 프론트엔드로 전달하는 추가 로직을 구현합니다.

		return kakaoMemberDetails;
	}
}