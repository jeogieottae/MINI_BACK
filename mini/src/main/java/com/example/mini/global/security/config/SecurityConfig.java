package com.example.mini.global.security.config;


import static org.springframework.security.config.Customizer.withDefaults;
import com.example.mini.global.auth.external.GoogleApiClient;
import com.example.mini.global.auth.external.KakaoApiClient;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtProvider jwtProvider;
	private final UserDetailsServiceImpl userDetailsService;
	private final TokenService tokenService;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "^(?!/api/).*");
	}

	private String[] swagger = {
		"/swagger-ui.html",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};

	private final GoogleApiClient googleApiClientService;
	private final KakaoApiClient kakaoApiClientService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.cors(withDefaults())
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

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(Arrays.asList(
			"http://localhost:8080",
			"http://3.38.1.70:8080",
			"http://ec2-3-38-1-70.ap-northeast-2.compute.amazonaws.com:8080",
			"http://localhost:3000",
			"https://localhost:3000",
			"https://127.0.0.1:3000",
			"https://your-trip-pied.vercel.app",
			"https://api.miniteam2.store/api/auth/kakao/callback",
			"https://api.miniteam2.store/api/auth/kakao/login",
			"https://kauth.kakao.com/oauth/authorize",
			"https://kauth.kakao.com/oauth/token",
			"https://kapi.kakao.com/v2/user/me"

		));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
		config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
