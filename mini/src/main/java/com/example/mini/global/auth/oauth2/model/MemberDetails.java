package com.example.mini.global.auth.oauth2.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
@Getter
@Setter
public class MemberDetails implements OAuth2User {

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
