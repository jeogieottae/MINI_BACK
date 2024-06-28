package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest= new RegisterRequest().builder()
            .name("John Doe")
            .nickname("johndoe")
            .email("example@example.com")
            .password("password")
            .build();;

    private LoginRequest loginRequest = new LoginRequest().builder()
            .email("example@example.com")
            .password("password")
            .build();

    @Test
    @DisplayName("register_성공")
    void successRegister() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);

        //when
        String result = authService.register(registerRequest);

        //then
        assertEquals("회원가입이 성공적으로 완료되었습니다.", result);
    }

    @Test
    @DisplayName("register_실패_이메일_중복")
    void failRegister(){
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        //when
        //then
        assertThrows(GlobalException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("login_성공")
    void successLogin() {
        // given
        Member member = Member.builder()
                .email("example@example.com")
                .password("encodedPassword")
                .build();
        when(memberRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(jwtProvider.createToken(anyString(), any(TokenType.class), anyBoolean()))
                .thenReturn("token");

        //when
        LoginResponse result = authService.login(loginRequest);

        //then
        assertEquals("token", result.getAccessToken());
    }

    @Test
    @DisplayName("login_실패_사용자_없음")
    void failLoginUserNotFound() {
        // given
        Member member = Member.builder()
                .email("example@example.com")
                .password("encodedPassword")
                .build();
        when(memberRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThrows(GlobalException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("login_비밀번호_불일치")
    void failLoginPasswordMismatch() {
        // given
        Member member = Member.builder()
                .email("example@example.com")
                .password("encodedPassword")
                .build();
        when(memberRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        //when
        //then
        assertThrows(GlobalException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("createAccessToken_성공")
    void successCreateAccessToken() {
        // given
        String refreshToken = "refreshToken";
        Claims claims = Mockito.mock(Claims.class);
        when(jwtProvider.getUserInfoFromToken(anyString(), any())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(tokenService.getRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtProvider.validateToken(anyString(), any())).thenReturn(true);
        when(jwtProvider.createToken(anyString(), any(), anyBoolean())).thenReturn("newAccessToken");

        // when
        String result = authService.createAccessToken(refreshToken);

        // then
        assertEquals("newAccessToken", result);
    }

    @Test
    @DisplayName("createAccessToken_토큰_불일치")
    void failCreateAccessTokenInvalidRefreshToken() {
        // given
        String refreshToken = "InvalidRefreshToken";
        Claims claims = Mockito.mock(Claims.class);
        when(jwtProvider.getUserInfoFromToken(anyString(), any())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(tokenService.getRefreshToken(anyString())).thenReturn(refreshToken);

        // when
        // then
        assertThrows(GlobalException.class, () -> authService.createAccessToken(refreshToken));
    }

    @Test
    @DisplayName("createAccessToken_유효하지_않은_토큰")
    void failCreateAccessTokenInvalidToken() {
        // given
        String refreshToken = "refreshToken";
        Claims claims = Mockito.mock(Claims.class);
        when(jwtProvider.getUserInfoFromToken(anyString(), any())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("test@example.com");
        when(tokenService.getRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtProvider.validateToken(anyString(), any())).thenReturn(false);

        // when
        // then
        assertThrows(GlobalException.class, () -> authService.createAccessToken(refreshToken));
    }

    @Test
    @DisplayName("logout_성공")
    void successLogout() {
        // given
        String accessToken = "accessToken";
        Member member = new Member();
        when(jwtProvider.getEmailFromToken(anyString(), any())).thenReturn("test@example.com");
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        authService.logout(accessToken);

        // then
        assertEquals(MemberState.INACTIVE, member.getState());
        verify(tokenService).blacklistToken(accessToken);
    }

    @Test
    @DisplayName("logout_실패_토큰_미입력")
    void failLogoutInvalidToken() {
        // given
        String accessToken = "";

        // when
        // then
        assertThrows(GlobalException.class, () -> authService.logout(accessToken));
    }

    @Test
    @DisplayName("logout_실패_사용자_없음")
    void failLogoutUserNotFound() {
        // given
        String accessToken = "accessToken";
        Member member = new Member();
        when(jwtProvider.getEmailFromToken(anyString(), any())).thenReturn("test@example.com");
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        // then
        assertThrows(GlobalException.class, () -> authService.logout(accessToken));
    }

    @Test
    @DisplayName("withdraw_성공")
    void successWithdraw() {
        // given
        String accessToken = "accessToken";
        Member member = new Member();
        when(jwtProvider.getEmailFromToken(anyString(), any())).thenReturn("test@example.com");
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        authService.withdraw(accessToken);

        // then
        verify(memberRepository).delete(member);
        verify(tokenService).blacklistToken(accessToken);
        verify(tokenService).removeToken(any());
    }

    @Test
    @DisplayName("updateNickname_성공")
    void successUpdateNickname() {
        // given
        String accessToken = "accessToken";
        String newNickname = "newNickname";
        Member member = new Member();
        when(jwtProvider.getEmailFromToken(anyString(), any())).thenReturn("test@example.com");
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        // when
        authService.updateNickname(accessToken, newNickname);

        // then
        assertEquals(newNickname, member.getNickname());
        verify(memberRepository).save(member);
    }
}
