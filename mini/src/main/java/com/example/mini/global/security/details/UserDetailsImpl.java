package com.example.mini.global.security.details;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@ToString
public class UserDetailsImpl implements UserDetails {
	private final Member member;

	public UserDetailsImpl(Member member) {
		this.member = member;
	}

	public Long getMemberId() {
		return member.getId();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<SimpleGrantedAuthority> set = new HashSet<>();
		return set;
	}

	@Override
	public String getPassword() {
		return this.member.getPassword();
	}

	@Override
	public String getUsername() {
		return this.member.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.member.getState() == MemberState.ACTIVE;
	}
}