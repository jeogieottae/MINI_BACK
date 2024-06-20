package com.example.mini.global.auth.oauth2.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class KakaoMemberDetails implements OAuth2User {


	// 인증 객체인 Authentication 객체안에 사용자 정보를 담기 위한 클래스
	private final String email;
	private final List<? extends GrantedAuthority> authorities;
	private final Map<String, Object> attributes;

	@Override
	public String getName() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}