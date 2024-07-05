package com.example.mini.global.auth.service;

import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final GoogleAuthService googleAuthService;
	private final StandardAuthService standardAuthService;
	private final KakaoAuthService kakaoAuthService;
	private final GoogleMemberService googleMemberService;
	private final KakaoMemberService kakaoMemberService;


	private String logoutRedirectUri = "https://your-trip-pied.vercel.app/";


	@Transactional
	public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		switch (cookenName) {
			case "googleAccessToken" -> {
				log.info("구글 토큰 재발급");
				return googleAuthService.googleRefresh(request).getAccess_token();
			}
			case "kakaoAccessToken" -> {
				log.info("카카오 토큰 재발급");
				return kakaoAuthService.kakaoRefresh(request).getAccess_token();
			}
			case "accessToken" -> {
				log.info("일반 토큰 재발급");
				return standardAuthService.standardRefreshToken(request, response);
			}
		}


		throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Transactional
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		switch (cookenName) {
			case "googleAccessToken" -> {
				log.info("구글 로그아웃");
				googleAuthService.googleLogout(request, response);
				return logoutRedirectUri;
			}
			case "kakaoAccessToken" -> {
				log.info("카카오 로그아웃");
				kakaoAuthService.kakaoLogout(request, response);
				return kakaoAuthService.getKakaoLogoutRedirectUri();
			}
			case "accessToken" -> {
				log.info("일반 로그아웃");
				standardAuthService.standardLogout(request, response);
				return logoutRedirectUri;
			}
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

		switch (cookenName) {
			case "googleAccessToken" -> {
				log.info("구글 회원 탈퇴");
				googleAuthService.withdraw(request, response);
				return logoutRedirectUri;
			}
			case "kakaoAccessToken" -> {
				log.info("카카오 회원 탈퇴");
				kakaoAuthService.withdraw(request, response);
				return kakaoAuthService.getKakaoLogoutRedirectUri();
			}
			case "accessToken" -> {
				log.info("일반 회원 탈퇴");
				standardAuthService.standardWithdraw(request, response);
				return logoutRedirectUri;
			}
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

		if (memberRepository.existsByNickname(changeNicknameRequest.getNickname())) { //추가
			throw new GlobalException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
		}

		switch (cookenName) {
			case "googleAccessToken" -> {
				log.info("구글 닉네임 변경");
				googleAuthService.changeNickname(request, changeNicknameRequest.getNickname());
				return;
			}
			case "kakaoAccessToken" -> {
				log.info("카카오 닉네임 변경");
				kakaoAuthService.changeNickname(request, changeNicknameRequest.getNickname());
				return;
			}
			case "accessToken" -> {
				log.info("일반 닉네임 변경");
				standardAuthService.standardUpdateNickname(request,
					changeNicknameRequest.getNickname());
				return;
			}
		}

		throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
	}

	@Transactional
	public UserProfileResponse getUserInfo(HttpServletRequest request) {
		// 로그인 방식 판단
		String cookenName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookenName);

		if (cookenName == null) {
			throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}

		switch (cookenName) {
			case "googleAccessToken" -> {
				log.info("구글 회원 정보 조회");
				return googleMemberService.getGoogleUserInfo(request);
			}
			case "kakaoAccessToken" -> {
				log.info("카카오 회원 정보 조회");
				return kakaoMemberService.getKakaoUserInfo(request);
			}
			case "accessToken" -> {
				log.info("일반 회원 정보 조회");
				return standardAuthService.getStandardUserInfo(request);
			}
		}

		throw new GlobalException(AuthErrorCode.INVALID_ACCESS_TOKEN);
	}


	@Transactional
	public Boolean isLoggedIn(HttpServletRequest request) {
		// 로그인 방식 판단
		String cookieName = CookieUtil.getCookieNames(request);
		log.info("로그인 방식: {}", cookieName);

		if (cookieName == null) {
			log.info("No login cookie found");
			return false;
		}

		switch (cookieName) {
			case "googleAccessToken":
				log.info("구글 로그인 확인");
				return true;
			case "kakaoAccessToken":
				log.info("카카오 로그인 확인");
				return true;
			case "accessToken":
				log.info("일반 로그인 확인");
				return true;
			default:
				return false;
		}
	}
}