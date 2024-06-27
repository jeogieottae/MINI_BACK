package com.example.mini.global.auth.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
@CrossOrigin(origins = {
	"http://localhost:8080",
	"http://3.38.1.70:8080",
	"http://ec2-3-38-1-70.ap-northeast-2.compute.amazonaws.com:8080",
	"http://localhost:3000",
	"https://localhost:3000",
	"https://127.0.0.1:3000"
})
public class ProtectedController {

	@GetMapping("/test")
	public String protectedEndpoint() {
		return "인증된 사용자만 접근 가능한 api에 접근 성공!";
	}

}