package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.exception.error.AuthErrorCode;
import com.example.mini.global.exception.type.GlobalException;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final TokenService tokenService;

	@Transactional
	public String register(RegisterRequest request) {
		log.info("회원가입 시도: 이메일={}, 닉네임={}", request.getEmail(), request.getName());
		String email = request.getEmail();
		String password = passwordEncoder.encode(request.getPassword());
		String name = request.getName();

		if (memberRepository.existsByEmail(email)) {
			throw new GlobalException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}

		Member member = Member.builder()
			.email(email)
			.password(password)
			.name(name)
			.state(MemberState.ACTIVE)
			.build();

		memberRepository.save(member);

		log.info("회원가입 성공: 이메일={}", member.getEmail());
		return "회원가입이 성공적으로 완료되었습니다.";
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		log.info("로그인 시도: 이메일={}", request.getEmail());
		String email = request.getEmail();
		String password = request.getPassword();
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

		if (!passwordEncoder.matches(password, member.getPassword())) {
			throw new GlobalException(AuthErrorCode.PASSWORD_MISMATCH);
		}

		String accessToken = jwtProvider.createToken(member.getEmail(), TokenType.ACCESS);
		String refreshToken = jwtProvider.createToken(member.getEmail(), TokenType.REFRESH);
		tokenService.saveRefreshToken(email, refreshToken);

		log.info("로그인 성공: 이메일={}", member.getEmail());
		return LoginResponse.builder()
			.state(member.getState())
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}


	@Transactional
	public String createAccessToken(String refreshToken) {
		Claims claims = jwtProvider.getUserInfoFromToken(refreshToken);
		String email = claims.getSubject();
		String storedRefreshToken = tokenService.getRefreshToken(email);

		if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
			throw new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		if (!jwtProvider.validateToken(refreshToken)) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		return jwtProvider.createToken(email, TokenType.ACCESS);
	}

	@Transactional
	public void logout(String accessToken) {
		if (accessToken == null || accessToken.isEmpty()) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		String email = jwtProvider.getEmailFromToken(accessToken);

		tokenService.blacklistToken(accessToken);
	}
}