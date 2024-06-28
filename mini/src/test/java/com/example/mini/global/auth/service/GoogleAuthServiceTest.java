package com.example.mini.global.auth.service;

import com.example.mini.domain.member.service.GoogleMemberService;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private GoogleApiClient googleApiClient;

    @Mock
    private GoogleMemberService googleMemberService;

    public static Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("email", "test@example.com");
        attributes.put("email_verified", true);
        attributes.put("name", "홍길동");
        attributes.put("given_name", "길동");
        attributes.put("family_name", "홍");
        attributes.put("picture", "https://example.com/profile.jpg");
        attributes.put("locale", "ko");
        return attributes;
    }

    public static GoogleUserInfo getGoogleUserInfo() {
        return new GoogleUserInfo(getAttributes());
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void testWithdrawMember() {
        // Given
        String accessToken = "testAccessToken";
        GoogleUserInfo googleUserInfo = getGoogleUserInfo();

        when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(googleUserInfo);

        // When
        googleAuthService.withdrawMember(accessToken);

        // Then
        verify(googleApiClient).getGoogleUserInfo(accessToken);
        verify(googleMemberService).withdrawMember(googleUserInfo.getEmail());
    }

    @Test
    @DisplayName("닉네임 업데이트 테스트")
    void testUpdateNickname() {
        // Given
        String accessToken = "testAccessToken";
        String newNickname = "새로운닉네임";
        GoogleUserInfo googleUserInfo = getGoogleUserInfo();

        when(googleApiClient.getGoogleUserInfo(accessToken)).thenReturn(googleUserInfo);

        // When
        googleAuthService.updateNickname(accessToken, newNickname);

        // Then
        verify(googleApiClient).getGoogleUserInfo(accessToken);
        verify(googleMemberService).updateNickname(googleUserInfo.getEmail(), newNickname);
    }
}