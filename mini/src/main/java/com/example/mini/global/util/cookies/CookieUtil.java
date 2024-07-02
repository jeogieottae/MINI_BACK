package com.example.mini.global.util.cookies;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

	public static void addCookie(HttpServletResponse response, String name, String value, long maxAgeInSeconds, boolean isSecure) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofSeconds(maxAgeInSeconds))
			.sameSite("None")
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			return Arrays.stream(cookies)
				.filter(cookie -> cookie.getName().equals(name))
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	public static void deleteCookie(HttpServletResponse response, String name, boolean isSecure) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)
			.sameSite("None")  // 변경
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	public static String getCookieNames(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("accessToken")) {
					return "accessToken";
				} else if (cookie.getName().equals("googleAccessToken")) {
					return "googleAccessToken";
				} else if (cookie.getName().equals("kakaoAccessToken")) {
					return "kakaoAccessToken";
				}
			}
		}

		return null;
	}
}
