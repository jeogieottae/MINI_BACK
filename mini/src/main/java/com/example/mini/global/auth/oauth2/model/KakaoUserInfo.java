package com.example.mini.global.auth.oauth2.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class KakaoUserInfo {

	public static final String KAKAO_ACCOUNT = "kakao_account";
	public static final String EMAIL = "email";
	public static final String PROFILE = "profile";
	public static final String NICKNAME = "nickname";

	private Map<String, Object> attributes;

	public KakaoUserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getEmail() {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeReferencer = new TypeReference<Map<String, Object>>() {};

		Object kakaoAccount = attributes.get(KAKAO_ACCOUNT);
		Map<String, Object> account = objectMapper.convertValue(kakaoAccount, typeReferencer);

		return (String) account.get(EMAIL);
	}

	public String getNickname() {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeReferencer = new TypeReference<Map<String, Object>>() {};

		Object kakaoAccount = attributes.get(KAKAO_ACCOUNT);
		Map<String, Object> account = objectMapper.convertValue(kakaoAccount, typeReferencer);
		Map<String, Object> profile = objectMapper.convertValue(account.get(PROFILE), typeReferencer);

		return (String) profile.get(NICKNAME);
	}
}
