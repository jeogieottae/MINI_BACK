package com.example.mini.global.util.cookies;

import com.example.mini.global.security.jwt.TokenType;
import jakarta.servlet.http.HttpServletResponse;

public class CookieService {

	public static void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		CookieUtil.addCookie(response, "accessToken", accessToken, TokenType.ACCESS.getExpireTime() / 1000);
		CookieUtil.addCookie(response, "refreshToken", refreshToken, TokenType.REFRESH.getExpireTime() / 1000);
	}

	public static void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
		CookieUtil.addCookie(response, "accessToken", accessToken, TokenType.ACCESS.getExpireTime() / 1000);
	}

	public static void deleteTokenCookies(HttpServletResponse response) {
		CookieUtil.deleteCookie(response, "accessToken");
		CookieUtil.deleteCookie(response, "refreshToken");
	}
}
