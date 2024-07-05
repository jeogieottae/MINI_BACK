package com.example.mini.global.security.details;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("사용자 정보 조회: username={}", username);
		Member member = memberRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("username으로 유저를 찾을 수 없습니다.: " + username));
		return new UserDetailsImpl(member);
	}

	// 일반 이메일로 정보 조회
	public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
		log.info("사용자 정보 조회: email={}", email);
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("email로 유저를 찾을 수 없습니다.: " + email));
		return new UserDetailsImpl(member);
	}

	// Oauth 이메일로 정보 조회
	public UserDetails loadUserByOauthEmail(String oauthEmail) throws UsernameNotFoundException {
		log.info("사용자 정보 조회: oauthEmail={}", oauthEmail);
		Member member = memberRepository.findByEmail(oauthEmail)
			.orElseThrow(() -> new UsernameNotFoundException("oauthEmail로 유저를 찾을 수 없습니다.: " + oauthEmail));
		return new UserDetailsImpl(member);
	}
}
