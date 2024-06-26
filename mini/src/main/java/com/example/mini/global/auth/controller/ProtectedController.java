package com.example.mini.global.auth.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProtectedController {

	@GetMapping("/test")
	public String protectedEndpoint() {
		return "인증된 사용자만 접근 가능한 api에 접근 성공!";
	}

}