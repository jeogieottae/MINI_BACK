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
		log.info("loauUser 메서드 실행");

		// DefaultOAuth2UserService의 기본 구현을 호출하여 OAuth2User를 로드
		OAuth2User oAuth2User = super.loadUser(userRequest);
		log.info("OAuth2User 정보: {}", oAuth2User);

		// 로드된 OAuth2User의 속성에서 카카오 사용자 정보를 추출
		KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
		log.info("카카오 사용자 정보: {}", kakaoUserInfo);

		// 데이터베이스에서 카카오 이메일로 사용자를 조회하거나 없다면, 새 사용자로 등록
		Member member = memberRepository.findByEmail(kakaoUserInfo.getEmail())
			.orElseGet(() -> {
				Member newMember = Member.builder()
					.email(kakaoUserInfo.getEmail())
					.name(kakaoUserInfo.getNickname())
					.password("default_password") // TODO
					.build();
				log.info("새로운 member 객체 저장됨: {}", newMember);
				Member savedMember = memberRepository.save(newMember);
				log.info("새로운 member 객체 저장됨: {}", savedMember);
				return savedMember;
			});

		// status 변경
		member.setState(MemberState.ACTIVE);

		// 기본 권한을 설정
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
		log.info("기본 권한: ROLE_USER");

		// KakaoMemberDetails 객체를 생성
		KakaoMemberDetails kakaoMemberDetails = new KakaoMemberDetails(String.valueOf(member.getEmail()),
			Collections.singletonList(authority),
			oAuth2User.getAttributes());
		log.info("생성된 KakaoMemberDetails 객체: {}", kakaoMemberDetails);

		// Authentication 객체를 생성하여 SecurityContext에 저장
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			kakaoMemberDetails, null, kakaoMemberDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("Authentication 객체를 생성하여 SecurityContext에 저장: {}", authentication);

		// JWT 생성 및 Redis에 저장
		String accessToken = jwtProvider.createToken(member.getEmail(), TokenType.ACCESS, true);
		String refreshToken = jwtProvider.createToken(member.getEmail(), TokenType.REFRESH, true);
		tokenService.saveRefreshToken(member.getEmail(), refreshToken);
		log.info("JWT 토큰 저장 : AccessToken={}, RefreshToken={}", accessToken, refreshToken);

/*		// 토큰 정보를 KakaoMemberDetails에 추가
		kakaoMemberDetails.setAccessToken(accessToken);
		kakaoMemberDetails.setRefreshToken(refreshToken);*/

		return kakaoMemberDetails;
	}
}
