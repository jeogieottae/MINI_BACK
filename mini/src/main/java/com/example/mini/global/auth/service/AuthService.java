package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	private final GoogleAuthService googleAuthService;
	private final KakaoAuthService kakaoAuthService;
	private final GoogleMemberService googleMemberService;
	private final KakaoMemberService kakaoMemberService;

	private String logoutRedirectUri = "http://localhost:8080/api/protected/home";

	@Transactional
	public String register(RegisterRequest request) {
		log.info("회원가입 시도: 이메일={}, 이름={}, 닉네임={}", request.getEmail(), request.getName(), request.getNickname());

		if (memberRepository.existsByEmail(request.getEmail())) {
			throw new GlobalException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}

		Member member = Member.builder()
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.name(request.getName())
			.nickname(request.getNickname())
			.state(MemberState.INACTIVE)
			.build();

		memberRepository.save(member);

		log.info("회원가입 성공: 이메일={}", member.getEmail());
		return "회원가입이 성공적으로 완료되었습니다.";
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		log.info("로그인 시도: 이메일={}", request.getEmail());
		Member member = memberRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

		if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
			throw new GlobalException(AuthErrorCode.PASSWORD_MISMATCH);
		}

		String accessToken = jwtProvider.createToken(member.getEmail(), TokenType.ACCESS, false);
		String refreshToken = jwtProvider.createToken(member.getEmail(), TokenType.REFRESH, false);
		tokenService.saveRefreshToken(member.getEmail(), refreshToken);

		member.setState(MemberState.ACTIVE);

		log.info("로그인 성공: 이메일={}, AccessToken={}, RefreshToken={}", member.getEmail(), accessToken, refreshToken);
		return LoginResponse.builder()
			.state(member.getState())
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	@Transactional
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		if(cookenName.equals("googleAccessToken")){
			log.info("구글 토큰 재발급");
			googleAuthService.googleRefresh(request);
		}else if(cookenName.equals("kakaoAccessToken")){
			log.info("카카오 토큰 재발급");
			kakaoAuthService.kakaoRefresh(request);
		}else if(cookenName.equals("accessToken")){
			log.info("일반 토큰 재발급");
			standardRefreshToken(request, response);
		}

	}

	@Transactional
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		if(cookenName.equals("googleAccessToken")){
			log.info("구글 로그아웃");
			googleAuthService.googleLogout(request, response);
			return logoutRedirectUri;
		}else if(cookenName.equals("kakaoAccessToken")){
			log.info("카카오 로그아웃");
			kakaoAuthService.kakaoLogout(request, response);
			return kakaoAuthService.getKakaoLogoutRedirectUri();
		}else if(cookenName.equals("accessToken")){
			log.info("일반 로그아웃");
			standardLogout(request, response);
			return logoutRedirectUri;
		}

		throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Transactional
	public String withdraw(HttpServletRequest request, HttpServletResponse response) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		if(cookenName.equals("googleAccessToken")){
			log.info("구글 회원 탈퇴");
			googleAuthService.withdraw(request, response);
			return logoutRedirectUri;
		}else if(cookenName.equals("kakaoAccessToken")){
			log.info("카카오 회원 탈퇴");
			kakaoAuthService.withdraw(request, response);
			return kakaoAuthService.getKakaoLogoutRedirectUri();
		}else if(cookenName.equals("accessToken")){
			log.info("일반 회원 탈퇴");
			standardWithdraw(request, response);
			return logoutRedirectUri;
		}

		throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Transactional
	public void updateNickname(HttpServletRequest request, ChangeNicknameRequest changeNicknameRequest) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		if(cookenName.equals("googleAccessToken")){
			log.info("구글 닉네임 변경");
			googleAuthService.changeNickname(request, changeNicknameRequest.getNickname());
		}else if(cookenName.equals("kakaoAccessToken")){
			log.info("카카오 닉네임 변경");
			kakaoAuthService.changeNickname(request, changeNicknameRequest.getNickname());
		}else if(cookenName.equals("accessToken")){
			log.info("일반 닉네임 변경");
			standardUpdateNickname(request, changeNicknameRequest.getNickname());
		}

	}

	@Transactional
	public UserProfileResponse getUserInfo(HttpServletRequest request) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		if(cookenName.equals("googleAccessToken")){
			log.info("구글 로그아웃");
			return googleMemberService.getGoogleUserInfo(request);
		}else if(cookenName.equals("kakaoAccessToken")){
			log.info("카카오 로그아웃");
			return kakaoMemberService.getKakaoUserInfo(request);
		}else if(cookenName.equals("accessToken")){
			log.info("일반 로그아웃");
			return getStandardUserInfo(request);
		}

		throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Transactional
	public void standardRefreshToken(HttpServletRequest request, HttpServletResponse response) {
		Cookie refreshTokenCookie = CookieUtil.getCookie(request, "refreshToken");
		if (refreshTokenCookie == null) {
			throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}
		String newAccessToken = createAccessToken(refreshTokenCookie.getValue());
		addAccessTokenCookie(response, newAccessToken);
		log.info("재발급된 Access 토큰을 쿠키에 저장: NewAccessToken={}", newAccessToken);
	}

	@Transactional
	public String createAccessToken(String refreshToken) {
		Claims claims = jwtProvider.getUserInfoFromToken(refreshToken, TokenType.REFRESH);
		String email = claims.getSubject();
		String storedRefreshToken = tokenService.getRefreshToken(email);

		if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
			throw new GlobalException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		if (!jwtProvider.validateToken(refreshToken, TokenType.REFRESH)) {
			throw new GlobalException(AuthErrorCode.INVALID_TOKEN);
		}

		String newAccessToken = jwtProvider.createToken(email, TokenType.ACCESS, false);
		log.info("Access 토큰 재발급: 이메일={}, NewAccessToken={}", email, newAccessToken);
		return newAccessToken;
	}

	@Transactional
	public void standardLogout(HttpServletRequest request, HttpServletResponse response) {

		String accessToken = CookieUtil.getCookie(request, "accessToken").getValue();
		String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

		member.setState(MemberState.INACTIVE);

		tokenService.blacklistToken(accessToken);

		deleteTokenCookies(response);

		log.info("로그아웃 성공: 이메일={}", email);
	}

	@Transactional
	public void standardWithdraw(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = CookieUtil.getCookie(request, "accessToken").getValue();

		if (accessToken == null || accessToken.isEmpty()) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

		memberRepository.delete(member);
		tokenService.blacklistToken(accessToken);
		tokenService.removeToken(tokenService.getRefreshToken(email));

		deleteTokenCookies(response);

		log.info("회원 탈퇴 성공: 이메일={}", email);
	}

	@Transactional
	public void standardUpdateNickname(HttpServletRequest request, String newNickname) {
		String accessToken = jwtProvider.resolveToken(request);
		String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

		member.setNickname(newNickname);
		memberRepository.save(member);

		log.info("닉네임 변경 성공: 이메일={}, 새 닉네임={}", email, newNickname);
	}



	@Transactional
	public UserProfileResponse getStandardUserInfo(HttpServletRequest request) {
		String accessToken = CookieUtil.getCookie(request, "accessToken").getValue();
		String email = jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS);
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(AuthErrorCode.USER_NOT_FOUND));

		return UserProfileResponse.builder()
				.name(member.getName())
				.nickname(member.getNickname())
				.email(email)
				.build();
	}

	// todo : 이 부분을 어떻게 할지 고민 중
	public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		CookieUtil.addCookie(response, "accessToken", accessToken, TokenType.ACCESS.getExpireTime() / 1000);
		CookieUtil.addCookie(response, "refreshToken", refreshToken, TokenType.REFRESH.getExpireTime() / 1000);
	}

	public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
		CookieUtil.addCookie(response, "accessToken", accessToken, TokenType.ACCESS.getExpireTime() / 1000);
	}

	public void deleteTokenCookies(HttpServletResponse response) {
		CookieUtil.deleteCookie(response, "accessToken");
		CookieUtil.deleteCookie(response, "refreshToken");
	}
}
