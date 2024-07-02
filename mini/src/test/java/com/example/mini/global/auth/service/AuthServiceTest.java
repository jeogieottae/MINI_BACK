//package com.example.mini.global.auth.service;
//
//import com.example.mini.domain.member.entity.Member;
//import com.example.mini.domain.member.entity.enums.MemberState;
//import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
//import com.example.mini.domain.member.model.request.LoginRequest;
//import com.example.mini.domain.member.model.request.RegisterRequest;
//import com.example.mini.domain.member.model.response.LoginResponse;
//import com.example.mini.domain.member.model.response.UserProfileResponse;
//import com.example.mini.domain.member.repository.MemberRepository;
//import com.example.mini.domain.member.service.GoogleMemberService;
//import com.example.mini.domain.member.service.KakaoMemberService;
//import com.example.mini.global.api.exception.GlobalException;
//import com.example.mini.global.security.jwt.JwtProvider;
//import com.example.mini.global.security.jwt.TokenService;
//import com.example.mini.global.security.jwt.TokenType;
//import com.example.mini.global.util.cookies.CookieUtil;
//import io.jsonwebtoken.Claims;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AuthServiceTest {
//
//    @Mock
//    private MemberRepository memberRepository;
//    @Mock
//    private PasswordEncoder passwordEncoder;
//    @Mock
//    private JwtProvider jwtProvider;
//    @Mock
//    private TokenService tokenService;
//
//    @Mock
//    private HttpServletRequest request;
//
//    @Mock
//    private HttpServletResponse response;
//
//    @Mock
//    private GoogleAuthService googleAuthService;
//
//    @Mock
//    private KakaoAuthService kakaoAuthService;
//
//    @Mock
//    private GoogleMemberService googleMemberService;
//
//    @Mock
//    private KakaoMemberService kakaoMemberService;
//
//    @InjectMocks
//    private AuthService authService;
//
//    private RegisterRequest registerRequest= new RegisterRequest().builder()
//        .name("John Doe")
//        .nickname("johndoe")
//        .email("example@example.com")
//        .password("password")
//        .build();;
//
//    private LoginRequest loginRequest = new LoginRequest().builder()
//        .email("example@example.com")
//        .password("password")
//        .build();
//
//    @Test
//    @DisplayName("register_성공")
//    void successRegister() {
//        // given
//        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
//
//        //when
//        String result = authService.register(registerRequest);
//
//        //then
//        assertEquals("회원가입이 성공적으로 완료되었습니다.", result);
//    }
//
//    @Test
//    @DisplayName("register_실패_이메일_중복")
//    void failRegister(){
//        // given
//        when(memberRepository.existsByEmail(anyString())).thenReturn(true);
//
//        //when
//        //then
//        assertThrows(GlobalException.class, () -> authService.register(registerRequest));
//    }
//
//    @Test
//    @DisplayName("login_성공")
//    void successLogin() {
//        // given
//        Member member = Member.builder()
//            .email("example@example.com")
//            .password("encodedPassword")
//            .build();
//        when(memberRepository.findByEmail(anyString()))
//            .thenReturn(Optional.of(member));
//        when(passwordEncoder.matches(anyString(), anyString()))
//            .thenReturn(true);
//        when(jwtProvider.createToken(anyString(), any(TokenType.class), anyBoolean()))
//            .thenReturn("token");
//
//        //when
//        LoginResponse result = authService.login(loginRequest);
//
//        //then
//        assertEquals("token", result.getAccessToken());
//    }
//
//    @Test
//    @DisplayName("login_실패_사용자_없음")
//    void failLoginUserNotFound() {
//        // given
//        Member member = Member.builder()
//            .email("example@example.com")
//            .password("encodedPassword")
//            .build();
//        when(memberRepository.findByEmail(anyString()))
//            .thenReturn(Optional.empty());
//
//        //when
//        //then
//        assertThrows(GlobalException.class, () -> authService.login(loginRequest));
//    }
//
//    @Test
//    @DisplayName("login_비밀번호_불일치")
//    void failLoginPasswordMismatch() {
//        // given
//        Member member = Member.builder()
//            .email("example@example.com")
//            .password("encodedPassword")
//            .build();
//        when(memberRepository.findByEmail(anyString()))
//            .thenReturn(Optional.of(member));
//        when(passwordEncoder.matches(anyString(), anyString()))
//            .thenReturn(false);
//
//        //when
//        //then
//        assertThrows(GlobalException.class, () -> authService.login(loginRequest));
//    }
//
//    @Test
//    @DisplayName("refreshToken_구글")
//    void testGoogleRefreshToken() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
//
//            // when
//            authService.refreshToken(request, response);
//
//            // then
//            verify(googleAuthService).googleRefresh(request);
//            verify(kakaoAuthService, never()).kakaoRefresh(any());
//        }
//    }
//
//    @Test
//    @DisplayName("refreshToken_카카오")
//    void testKakaoRefreshToken() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
//
//            // when
//            authService.refreshToken(request, response);
//
//            // then
//            verify(kakaoAuthService).kakaoRefresh(request);
//            verify(googleAuthService, never()).googleRefresh(any());
//        }
//    }
//
//    @Test
//    @DisplayName("refreshToken_일반")
//    void testStandardRefreshToken() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");
//
//            // standardRefreshToken 메서드를 스파이로 만듭니다.
//            AuthService spyAuthService = spy(authService);
//            doNothing().when(spyAuthService).standardRefreshToken(any(), any());
//
//            // when
//            spyAuthService.refreshToken(request, response);
//
//            // then
//            verify(spyAuthService).standardRefreshToken(request, response);
//            verify(kakaoAuthService, never()).kakaoRefresh(any());
//            verify(googleAuthService, never()).googleRefresh(any());
//        }
//    }
//
//    @Test
//    @DisplayName("logout_구글")
//    void testGoogleLogout() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
//
//            // when
//            String result = authService.logout(request, response);
//
//            // then
//            verify(googleAuthService).googleLogout(request, response);
//            verify(kakaoAuthService, never()).kakaoLogout(any(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("logout_카카오")
//    void testKakaoLogout() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
//
//            // when
//            String result = authService.logout(request, response);
//
//            // then
//            verify(kakaoAuthService).kakaoLogout(request, response);
//            verify(googleAuthService, never()).googleLogout(any(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("logout_일반")
//    void testStandardLogout() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");
//            AuthService spyAuthService = spy(authService);
//            doNothing().when(spyAuthService).standardLogout(any(), any());
//
//            // when
//            String result = spyAuthService.logout(request, response);
//
//            // then
//            verify(spyAuthService).standardLogout(request, response);
//            verify(kakaoAuthService, never()).kakaoLogout(any(), any());
//            verify(googleAuthService, never()).googleLogout(any(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("updateNickname_구글")
//    void testGoogleUpdateNickname() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
//            ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("newNickname");
//
//            // when
//            authService.updateNickname(request, changeNicknameRequest);
//
//            // then
//            verify(googleAuthService).changeNickname(request, "newNickname");
//            verify(kakaoAuthService, never()).changeNickname(any(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("updateNickname_카카오")
//    void testKakaoUpdateNickname() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
//            ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("newNickname");
//
//            // when
//            authService.updateNickname(request, changeNicknameRequest);
//
//            // then
//            verify(kakaoAuthService).changeNickname(request, "newNickname");
//            verify(googleAuthService, never()).changeNickname(any(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("updateNickname_일반")
//    void testStandardUpdateNickname() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");
//            ChangeNicknameRequest changeNicknameRequest = new ChangeNicknameRequest("newNickname");
//
//            // standardUpdateNickname 메서드를 스파이로 만듭니다.
//            AuthService spyAuthService = spy(authService);
//            doNothing().when(spyAuthService).standardUpdateNickname(any(), any());
//
//            // when
//            spyAuthService.updateNickname(request, changeNicknameRequest);
//
//            // then
//            verify(spyAuthService).standardUpdateNickname(request, "newNickname");
//            verify(kakaoAuthService, never()).changeNickname(any(), any());
//            verify(googleAuthService, never()).changeNickname(any(), any());
//        }
//    }
//
//    @Test
//    @DisplayName("getUserInfo_구글")
//    void testGetGoogleUserInfo() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("googleAccessToken");
//            UserProfileResponse expectedResponse = new UserProfileResponse(); // 적절한 응답 객체 생성
//            when(googleMemberService.getGoogleUserInfo(request)).thenReturn(expectedResponse);
//
//            // when
//            UserProfileResponse result = authService.getUserInfo(request);
//
//            // then
//            verify(googleMemberService).getGoogleUserInfo(request);
//            verify(kakaoMemberService, never()).getKakaoUserInfo(any());
//            assertEquals(expectedResponse, result);
//        }
//    }
//
//    @Test
//    @DisplayName("getUserInfo_카카오")
//    void testGetKakaoUserInfo() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("kakaoAccessToken");
//            UserProfileResponse expectedResponse = new UserProfileResponse(); // 적절한 응답 객체 생성
//            when(kakaoMemberService.getKakaoUserInfo(request)).thenReturn(expectedResponse);
//
//            // when
//            UserProfileResponse result = authService.getUserInfo(request);
//
//            // then
//            verify(kakaoMemberService).getKakaoUserInfo(request);
//            verify(googleMemberService, never()).getGoogleUserInfo(any());
//            assertEquals(expectedResponse, result);
//        }
//    }
//
//    @Test
//    @DisplayName("getUserInfo_일반")
//    void testGetStandardUserInfo() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookieNames(request)).thenReturn("accessToken");
//            AuthService spyAuthService = spy(authService);
//            UserProfileResponse expectedResponse = new UserProfileResponse(); // 적절한 응답 객체 생성
//            doReturn(expectedResponse).when(spyAuthService).getStandardUserInfo(request);
//
//            // when
//            UserProfileResponse result = spyAuthService.getUserInfo(request);
//
//            // then
//            verify(spyAuthService).getStandardUserInfo(request);
//            verify(kakaoMemberService, never()).getKakaoUserInfo(any());
//            verify(googleMemberService, never()).getGoogleUserInfo(any());
//            assertEquals(expectedResponse, result);
//        }
//    }
//
//    @Test
//    @DisplayName("standardRefreshToken_성공")
//    void testStandardRefreshTokenSuccess() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            Cookie mockCookie = new Cookie("refreshToken", "validRefreshToken");
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "refreshToken")).thenReturn(mockCookie);
//
//            String newAccessToken = "newAccessToken";
//            AuthService spyAuthService = spy(authService);
//            doReturn(newAccessToken).when(spyAuthService).createAccessToken("validRefreshToken");
//            doNothing().when(spyAuthService).addAccessTokenCookie(any(), anyString());
//
//            // when
//            spyAuthService.standardRefreshToken(request, response);
//
//            // then
//            verify(spyAuthService).createAccessToken("validRefreshToken");
//            verify(spyAuthService).addAccessTokenCookie(response, newAccessToken);
//        }
//    }
//
//    @Test
//    @DisplayName("standardRefreshToken_실패_리프레시토큰없음")
//    void testStandardRefreshTokenFailNoRefreshToken() {
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            // given
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "refreshToken")).thenReturn(null);
//
//            // when & then
//            assertThrows(GlobalException.class, () -> authService.standardRefreshToken(request, response));
//        }
//    }
//
//
//    @Test
//    @DisplayName("createAccessToken_성공")
//    void successCreateAccessToken() {
//        // given
//        String refreshToken = "refreshToken";
//        Claims claims = Mockito.mock(Claims.class);
//        when(jwtProvider.getUserInfoFromToken(anyString(), any())).thenReturn(claims);
//        when(claims.getSubject()).thenReturn("test@example.com");
//        when(tokenService.getRefreshToken(anyString())).thenReturn(refreshToken);
//        when(jwtProvider.validateToken(anyString(), any())).thenReturn(true);
//        when(jwtProvider.createToken(anyString(), any(), anyBoolean())).thenReturn("newAccessToken");
//
//        // when
//        String result = authService.createAccessToken(refreshToken);
//
//        // then
//        assertEquals("newAccessToken", result);
//    }
//
//    @Test
//    @DisplayName("createAccessToken_토큰_불일치")
//    void failCreateAccessTokenInvalidRefreshToken() {
//        // given
//        String refreshToken = "InvalidRefreshToken";
//        Claims claims = Mockito.mock(Claims.class);
//        when(jwtProvider.getUserInfoFromToken(anyString(), any())).thenReturn(claims);
//        when(claims.getSubject()).thenReturn("test@example.com");
//        when(tokenService.getRefreshToken(anyString())).thenReturn(refreshToken);
//
//        // when
//        // then
//        assertThrows(GlobalException.class, () -> authService.createAccessToken(refreshToken));
//    }
//
//    @Test
//    @DisplayName("createAccessToken_유효하지_않은_토큰")
//    void failCreateAccessTokenInvalidToken() {
//        // given
//        String refreshToken = "refreshToken";
//        Claims claims = Mockito.mock(Claims.class);
//        when(jwtProvider.getUserInfoFromToken(anyString(), any())).thenReturn(claims);
//        when(claims.getSubject()).thenReturn("test@example.com");
//        when(tokenService.getRefreshToken(anyString())).thenReturn(refreshToken);
//        when(jwtProvider.validateToken(anyString(), any())).thenReturn(false);
//
//        // when
//        // then
//        assertThrows(GlobalException.class, () -> authService.createAccessToken(refreshToken));
//    }
//
//    @Test
//    @DisplayName("standardLogout_성공")
//    void successStandardLogout() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        Member mockMember = mock(Member.class);
//        Cookie mockCookie = new Cookie("accessToken", accessToken);
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
//            doNothing().when(tokenService).blacklistToken(accessToken);
//
//            // authService를 spy로 만들어 일부 메서드만 모의 처리
//            AuthService spyAuthService = spy(authService);
//            doNothing().when(spyAuthService).deleteTokenCookies(response);
//
//            // when
//            spyAuthService.standardLogout(request, response);
//
//            // then
//            verify(mockMember).setState(MemberState.INACTIVE);
//            verify(tokenService).blacklistToken(accessToken);
//            verify(spyAuthService).deleteTokenCookies(response);
//        }
//    }
//
//    @Test
//    @DisplayName("standardLogout_실패_사용자_없음")
//    void testStandardLogoutFailUserNotFound() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        Cookie mockCookie = new Cookie("accessToken", accessToken);
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThrows(GlobalException.class, () -> authService.standardLogout(request, response));
//        }
//    }
//
//    @Test
//    @DisplayName("standardWithdraw_성공")
//    void testStandardWithdrawSuccess() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        Member mockMember = mock(Member.class);
//        Cookie mockCookie = new Cookie("accessToken", accessToken);
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
//            when(tokenService.getRefreshToken(email)).thenReturn("refreshToken");
//
//            // when
//            authService.standardWithdraw(request, response);
//
//            // then
//            verify(memberRepository).delete(mockMember);
//            verify(tokenService).blacklistToken(accessToken);
//            verify(tokenService).removeToken("refreshToken");
//        }
//    }
//
//    @Test
//    @DisplayName("standardWithdraw_실패_유효하지_않은_액세스토큰")
//    void testStandardWithdrawFailInvalidAccessToken() {
//        // given
//        Cookie mockCookie = new Cookie("accessToken", "");
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//
//            // when & then
//            assertThrows(GlobalException.class, () -> authService.standardWithdraw(request, response));
//        }
//    }
//
//    @Test
//    @DisplayName("standardWithdraw_실패_사용자_없음")
//    void testStandardWithdrawFailUserNotFound() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        Cookie mockCookie = new Cookie("accessToken", accessToken);
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThrows(GlobalException.class, () -> authService.standardWithdraw(request, response));
//        }
//    }
//
//    @Test
//    @DisplayName("standardUpdateNickname_성공")
//    void testStandardUpdateNicknameSuccess() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        String newNickname = "newNickname";
//        Member mockMember = new Member();
//        mockMember.setEmail(email);
//
//        when(jwtProvider.resolveToken(request)).thenReturn(accessToken);
//        when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
//
//        // when
//        authService.standardUpdateNickname(request, newNickname);
//
//        // then
//        verify(memberRepository).save(mockMember);
//        assertEquals(newNickname, mockMember.getNickname());
//    }
//
//    @Test
//    @DisplayName("standardUpdateNickname_실패_사용자_없음")
//    void testStandardUpdateNicknameFailUserNotFound() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        String newNickname = "newNickname";
//
//        when(jwtProvider.resolveToken(request)).thenReturn(accessToken);
//        when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(GlobalException.class, () -> authService.standardUpdateNickname(request, newNickname));
//    }
//
//    @Test
//    @DisplayName("getStandardUserInfo_성공")
//    void testGetStandardUserInfoSuccess() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        Member mockMember = Member.builder()
//            .name("Test User")
//            .nickname("testuser")
//            .email(email)
//            .build();
//
//        Cookie mockCookie = new Cookie("accessToken", accessToken);
//
//        // CookieUtil.getCookie() 메서드를 모킹
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//
//            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));
//
//            // when
//            UserProfileResponse response = authService.getStandardUserInfo(request);
//
//            // then
//            assertEquals("Test User", response.getName());
//            assertEquals("testuser", response.getNickname());
//            assertEquals(email, response.getEmail());
//        }
//    }
//
//    @Test
//    @DisplayName("getStandardUserInfo_실패_사용자_없음")
//    void testGetStandardUserInfoFailUserNotFound() {
//        // given
//        String accessToken = "validAccessToken";
//        String email = "test@example.com";
//        Cookie mockCookie = new Cookie("accessToken", accessToken);
//
//        try (MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class)) {
//            mockedCookieUtil.when(() -> CookieUtil.getCookie(request, "accessToken")).thenReturn(mockCookie);
//            when(jwtProvider.getEmailFromToken(accessToken, TokenType.ACCESS)).thenReturn(email);
//            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThrows(GlobalException.class, () -> authService.getStandardUserInfo(request));
//        }
//    }
//
//}