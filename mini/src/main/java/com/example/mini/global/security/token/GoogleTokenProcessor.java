package com.example.mini.global.security.token;

import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.service.GoogleAuthService;
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

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenProcessor implements TokenProcessor {
    private final GoogleAuthService googleAuthService;
    private final GoogleApiClient googleApiClient;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void processToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie googleAccessToken = CookieUtil.getCookie(request, "googleAccessToken");
        if(googleAccessToken != null){
            String token = googleAccessToken.getValue();
            long maxAge = googleAccessToken.getMaxAge();
            long currentTime = System.currentTimeMillis() / 1000;

            if(currentTime + 300 > maxAge) {
                String newToken = null;
                log.info("구글 토큰 재발급 필요");
                newToken = googleAuthService.googleRefresh(request).getAccess_token();

                log.info("구글 토큰 재발급: {}", newToken);
                processGoogleToken(newToken);
            }else{
                processGoogleToken(token);
            }
        }
    }

    private void processGoogleToken(String token){
        GoogleUserInfo userInfo = googleApiClient.getGoogleUserInfo(token);
        if(userInfo != null && userInfo.getEmail() != null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(userInfo.getEmail());
            Authentication auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

    }
}
