package com.example.mini.global.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

	@GetMapping("/test")
	public String protectedEndpoint() {
		return "This is a protected endpoint. You need a valid access token to access this.";
	}
}