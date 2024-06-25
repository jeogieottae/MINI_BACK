package com.example.mini.global.auth.oauth2.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.oauth2.model.MemberDetails;
import com.example.mini.global.auth.oauth2.model.OAuthAttributes;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final TokenService tokenService;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		log.info("loadUser 메서드 실행");

		// OAuth2User 로드
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
		log.info("OAuth2User 정보: {}", oAuth2User);

		// 카카오 / 구글 로그인 판별
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// OAuth2 로그인 진행 시 키가 되는 필드 값 (PK)
		// 구글의 경우 지원, 카카오는 지원 X?
		String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUserNameAttributeName();

		// OAuth2UserService를 통해 가져온 OAuth2User의 attribute 등을 담을 클래스
		OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

		// 사용자 저장 또는 업데이트
		Member member;
		if(registrationId.equals("kakao")) {
			Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
			Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

			String email = (String) kakaoAccount.get("email");
			String name = (String) profile.get("nickname");

			member = memberRepository.findByEmail(email)
					.map(entity -> entity.update(name))
					.orElse(Member.builder()
							.name(name)
							.email(email)
							.password("OAuth password")
							.state(MemberState.ACTIVE)
							.build());
			memberRepository.save(member);
		}else if (registrationId.equals("google")){
			member = memberRepository.findByEmail(attributes.getEmail())
					.map(entity -> entity.update(attributes.getName()))
					.orElse(attributes.toEntity());
			memberRepository.save(member);
		}else{
			throw new OAuth2AuthenticationException("허용되지 않은 인증입니다.");
		}

		log.info("사용자 저장 또는 업데이트: {}", member);

		// 기본 권한을 설정
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
		log.info("기본 권한: ROLE_USER");

		// MemberDetails 객체를 생성
		MemberDetails memberDetails = new MemberDetails(String.valueOf(member.getEmail()),
			Collections.singletonList(authority),
			oAuth2User.getAttributes());
		log.info("생성된 MemberDetails 객체: {}", memberDetails);

		// Authentication 객체를 생성하여 SecurityContext에 저장
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				memberDetails, null, memberDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("Authentication 객체를 생성하여 SecurityContext에 저장: {}", authentication);

		// 토큰 생성 및 Redis에 저장
		String accessToken = userRequest.getAccessToken().getTokenValue();
		String refreshToken;
		if(registrationId.equals("kakao")) {
			refreshToken = userRequest.getAdditionalParameters().get("refresh_token") != null
					? userRequest.getAdditionalParameters().get("refresh_token").toString()
					: null;
		}else { // 구글의 경우 리프레쉬 토큰이 없고, 토큰 갱신 uri가 있다
			refreshToken = userRequest.getClientRegistration().getProviderDetails().getTokenUri().toString();
		}

		tokenService.saveRefreshToken(member.getEmail(), refreshToken);
		log.info("JWT 토큰 저장 : AccessToken={}, RefreshToken={}", accessToken, refreshToken);

		// 쿠키에 저장
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		if (response != null) {
			String tokenName = registrationId + "_token";
			CookieUtil.addCookie(response, tokenName, accessToken, TokenType.ACCESS.getExpireTime() / 1000);
			CookieUtil.addCookie(response, "refreshToken", refreshToken, TokenType.REFRESH.getExpireTime() / 1000);
			log.info("AccessToken 쿠키 설정: {}", accessToken);
			log.info("RefreshToken 쿠키 설정: {}", refreshToken);
		}

		return memberDetails;
	}

}
