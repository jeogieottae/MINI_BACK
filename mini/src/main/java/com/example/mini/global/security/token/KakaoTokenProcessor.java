package com.example.mini.global.security.token;

import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.model.KakaoUserInfo;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTokenProcessor implements TokenProcessor {
    private final KakaoAuthService kakaoAuthService;
    private final KakaoApiClient kakaoApiClient;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void processToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie kakaoAccessToken = CookieUtil.getCookie(request, "kakaoAccessToken");
        if(kakaoAccessToken != null){
            String token = kakaoAccessToken.getValue();
            long maxAge = kakaoAccessToken.getMaxAge();
            long currentTime = System.currentTimeMillis() / 1000;

            if(currentTime + 300 > maxAge) {
                String newToken = null;
                log.info("카카오 토큰 재발급 필요");
                newToken = kakaoAuthService.kakaoRefresh(request).getAccess_token();

                log.info("카카오 토큰 재발급: {}", newToken);
                processKakaoToken(newToken);
            }else{
                processKakaoToken(token);
            }
        }
    }

    private void processKakaoToken(String token){
        KakaoUserInfo userInfo = kakaoApiClient.getKakaoUserInfo(token);
        if(userInfo != null && userInfo.getEmail() != null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(userInfo.getEmail());
            Authentication auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

    }
}
