package com.example.mini.global.security.config;

import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.external.KakaoApiClient;
import com.example.mini.global.auth.service.GoogleAuthService;
import com.example.mini.global.auth.service.KakaoAuthService;
import com.example.mini.global.security.details.UserDetailsServiceImpl;
import com.example.mini.global.security.filter.JwtAuthenticationFilter;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "^(?!/api/).*");
	}

	private String[] swagger = {
		"/swagger-ui.html",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};

	private final JwtProvider jwtProvider;
	private final UserDetailsServiceImpl userDetailsService;
	private final TokenService tokenService;  // 추가된 부분
	private final GoogleApiClient googleApiClientService;
	private final KakaoApiClient kakaoApiClientService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(swagger).permitAll()
				.requestMatchers("/api/protected/**").authenticated()
				.requestMatchers(("/test")).permitAll()
				.requestMatchers("/api/auth/**").permitAll()
				.anyRequest().permitAll())
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userDetailsService, tokenService, googleApiClientService, kakaoApiClientService)
					, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
