package com.example.mini.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	@Value("${spring.jwt.access-secret}")
	private String accessSecret;
	@Value("${spring.jwt.refresh-secret}")
	private String refreshSecret;
	private Key accessKey;
	private Key refreshKey;

	@PostConstruct
	public void init() {
		byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecret);
		byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecret);
		this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
		this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
	}

	public String createToken(String email, TokenType type) {
		Date now = new Date();
		Date validity = new Date(now.getTime() + type.getExpireTime());

		Key key = type == TokenType.ACCESS ? accessKey : refreshKey;

		return Jwts.builder()
			.setSubject(email)
			.claim("type", type)
			.setIssuedAt(now)
			.setExpiration(validity)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Claims getUserInfoFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token).getBody();
	}

	public String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	public String getEmailFromToken(String token) {
		return getUserInfoFromToken(token).getSubject();
	}

}
