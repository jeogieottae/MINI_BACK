package com.example.mini.global.auth.oauth2.util;

import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

//	private final OAuth2AuthorizedClientService clientService;

	// oauth 로그인 성공 시 클라이언트로 리다이렉트
	private static final String REDIRECT_URI = "http://localhost:8080/api/protected/test";


	@Transactional
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

//		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//		OAuth2User oAuth2User = oauthToken.getPrincipal();
//
//		String registrationId = oauthToken.getAuthorizedClientRegistrationId();
//
//		OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(registrationId, oAuth2User.getName());
//
//		OAuth2AccessToken accessToken = client.getAccessToken();
//
//		CookieUtil.addCookie(response, registrationId + "_token", accessToken.toString(), 3600);

		getRedirectStrategy().sendRedirect(request, response, REDIRECT_URI);
	}
}