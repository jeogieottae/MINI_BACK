package com.example.mini.global.auth.service;

import com.example.mini.domain.member.service.KakaoMemberService;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.KakaoUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class KakaoAuthServiceTest {

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private KakaoMemberService kakaoMemberService;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    public KakaoAuthServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    public static Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> kakaoAccount = new HashMap<>();
        Map<String, Object> profile = new HashMap<>();

        profile.put("nickname", "홍길동");
        kakaoAccount.put("email", "test@example.com");
        kakaoAccount.put("profile", profile);

        attributes.put("id", 123456789L);
        attributes.put("kakao_account", kakaoAccount);

        return attributes;
    }

    public static KakaoUserInfo getKakaoUserInfo() {
        return new KakaoUserInfo(getAttributes());
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void testWithdrawMember() {
        String accessToken = "testAccessToken";
        KakaoUserInfo userInfo = getKakaoUserInfo();

        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(userInfo);

        kakaoAuthService.withdrawMember(accessToken);

        verify(kakaoApiClient).getKakaoUserInfo(accessToken);
        verify(kakaoMemberService).withdrawMember(userInfo.getEmail());
    }

    @Test
    @DisplayName("닉네임 업데이트 테스트")
    void testUpdateNickname() {
        String accessToken = "testAccessToken";
        String newNickname = "newNickname";
        KakaoUserInfo userInfo = getKakaoUserInfo();

        when(kakaoApiClient.getKakaoUserInfo(accessToken)).thenReturn(userInfo);

        kakaoAuthService.updateNickname(accessToken, newNickname);

        verify(kakaoApiClient, times(1)).getKakaoUserInfo(accessToken);
        verify(kakaoMemberService, times(1)).updateNickname(userInfo.getEmail(), newNickname);
    }
}