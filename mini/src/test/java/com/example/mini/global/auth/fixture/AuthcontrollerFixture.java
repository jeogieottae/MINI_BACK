package com.example.mini.global.auth.fixture;

import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;

public class AuthcontrollerFixture {

    public static RegisterRequest getRegisterRequest() {
        return RegisterRequest.builder()
                .name("testName")
                .nickname("testNickname")
                .email("testEmail")
                .password("testPassword")
                .build();
    }

    public static LoginRequest getLoginRequest() {
        return LoginRequest.builder()
                .email("testEmail")
                .password("testPassword")
                .build();
    }

    public static LoginResponse getLoginResponse() {
        return LoginResponse.builder()
                .accessToken("testAccessToken")
                .refreshToken("testRefreshToken")
                .build();
    }

    public static UserProfileResponse getUserProfileResponse() {
        return UserProfileResponse.builder()
                .email("testEmail")
                .nickname("testNickname")
                .build();
    }


}
