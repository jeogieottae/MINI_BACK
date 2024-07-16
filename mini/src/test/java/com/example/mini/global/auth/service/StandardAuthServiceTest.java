package com.example.mini.global.auth.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StandardAuthServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenService tokenService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    private StandardAuthService standardAuthService;

    @Test
    @DisplayName("register_성공")
    void successRegister() {
        // given
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false); // 닉네임 체크 추가

        //when & then
        assertDoesNotThrow(() -> standardAuthService.register(AuthServiceTestFixture.createRegisterRequest()));
    }

    @Test
    @DisplayName("register_실패_이메일_중복")
    void failRegister(){
        // given
        Member member = Member.builder()
                .email("example@example.com")
                .password("encodedPassword")
                .state(MemberState.INACTIVE)
                .build();
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));

        //when
        //then
        assertThrows(GlobalException.class, () -> standardAuthService.register(AuthServiceTestFixture.createRegisterRequest()));
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
        LoginResponse result = standardAuthService.login(AuthServiceTestFixture.createLoginRequest());

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
        assertThrows(GlobalException.class, () -> standardAuthService.login(AuthServiceTestFixture.createLoginRequest()));
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
        assertThrows(GlobalException.class, () -> standardAuthService.login(AuthServiceTestFixture.createLoginRequest()));
    }

    @Test
    @DisplayName("standardRefreshToken_성공")
    void testStandardRefreshTokenSuccess() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            Cookie mockCookie = new Cookie("refreshToken", "validRefreshToken");
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "refreshToken")).thenReturn(mockCookie);

            String newAccessToken = "newAccessToken";
            StandardAuthService spyAuthService = spy(standardAuthService);
            doReturn(newAccessToken).when(spyAuthService).createAccessToken("validRefreshToken");
            doNothing().when(spyAuthService).addAccessTokenCookie(any(), anyString());

            // when
            spyAuthService.standardRefreshToken(request, response);

            // then
            verify(spyAuthService).createAccessToken("validRefreshToken");
            verify(spyAuthService).addAccessTokenCookie(response, newAccessToken);
        }
    }

    @Test
    @DisplayName("standardRefreshToken_실패_리프레시토큰없음")
    void testStandardRefreshTokenFailNoRefreshToken() {
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            // given
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "refreshToken")).thenReturn(null);

            // when & then
            assertThrows(GlobalException.class, () -> standardAuthService.standardRefreshToken(request, response));
        }
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
        String result = standardAuthService.createAccessToken(refreshToken);

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
        assertThrows(GlobalException.class, () -> standardAuthService.createAccessToken(refreshToken));
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
        assertThrows(GlobalException.class, () -> standardAuthService.createAccessToken(refreshToken));
    }

    @Test
    @DisplayName("standardLogout_성공")
    void successStandardLogout() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Member mockMember = mock(Member.class);
        Cookie mockCookie = new Cookie("accessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
            doNothing().when(tokenService).blacklistToken(accessToken);

            // authService를 spy로 만들어 일부 메서드만 모의 처리
            StandardAuthService spyAuthService = spy(standardAuthService);
            doNothing().when(spyAuthService).deleteTokenCookies(response);

            // when
            spyAuthService.standardLogout(request, response);

            // then
            verify(mockMember).setState(MemberState.INACTIVE);
            verify(tokenService).blacklistToken(accessToken);
            verify(spyAuthService).deleteTokenCookies(response);
        }
    }

    @Test
    @DisplayName("standardLogout_실패_사용자_없음")
    void testStandardLogoutFailUserNotFound() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Cookie mockCookie = new Cookie("accessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThrows(GlobalException.class, () -> standardAuthService.standardLogout(request, response));
        }
    }


    @Test
    @DisplayName("standardWithdraw_성공")
    void testStandardWithdrawSuccess() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Member mockMember = mock(Member.class);
        Cookie mockCookie = new Cookie("accessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
            when(tokenService.getRefreshToken(email)).thenReturn("refreshToken");

            // when
            standardAuthService.standardWithdraw(request, response);

            // then
            verify(tokenService).blacklistToken(accessToken);
            verify(tokenService).removeToken("refreshToken");
        }
    }

    @Test
    @DisplayName("standardWithdraw_실패_유효하지_않은_액세스토큰")
    void testStandardWithdrawFailInvalidAccessToken() {
        // given
        Cookie mockCookie = new Cookie("accessToken", "");

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);

            // when & then
            assertThrows(GlobalException.class, () -> standardAuthService.standardWithdraw(request, response));
        }
    }

    @Test
    @DisplayName("standardWithdraw_실패_사용자_없음")
    void testStandardWithdrawFailUserNotFound() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Cookie mockCookie = new Cookie("accessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThrows(GlobalException.class, () -> standardAuthService.standardWithdraw(request, response));
        }
    }

    @Test
    @DisplayName("standardUpdateNickname_성공")
    void testStandardUpdateNicknameSuccess() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        String newNickname = "newNickname";
        Member mockMember = new Member();
        mockMember.setEmail(email);

        when(jwtProvider.resolveToken(request)).thenReturn(accessToken);
        when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));

        // when
        standardAuthService.standardUpdateNickname(request, newNickname);

        // then
        assertEquals(newNickname, mockMember.getNickname());
    }

    @Test
    @DisplayName("standardUpdateNickname_실패_사용자_없음")
    void testStandardUpdateNicknameFailUserNotFound() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        String newNickname = "newNickname";

        when(jwtProvider.resolveToken(request)).thenReturn(accessToken);
        when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(GlobalException.class, () -> standardAuthService.standardUpdateNickname(request, newNickname));
    }

    @Test
    @DisplayName("getStandardUserInfo_성공")
    void testGetStandardUserInfoSuccess() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Member mockMember = Member.builder()
                .name("Test User")
                .nickname("testuser")
                .email(email)
                .build();

        Cookie mockCookie = new Cookie("accessToken", accessToken);

        // CookieUtil.getCookie() 메서드를 모킹
        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);

            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));

            // when
            UserProfileResponse response = standardAuthService.getStandardUserInfo(request);

            // then
            assertEquals("Test User", response.getName());
            assertEquals("testuser", response.getNickname());
            assertEquals(email, response.getEmail());
        }
    }

    @Test
    @DisplayName("getStandardUserInfo_실패_사용자_없음")
    void testGetStandardUserInfoFailUserNotFound() {
        // given
        String accessToken = "validAccessToken";
        String email = "test@example.com";
        Cookie mockCookie = new Cookie("accessToken", accessToken);

        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThrows(GlobalException.class, () -> standardAuthService.getStandardUserInfo(request));
        }
    }

}
