package com.example.mini.global.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns(
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

			)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
			.allowedHeaders("Authorization", "Cache-Control", "Content-Type")
			.allowCredentials(true);
	}
}
