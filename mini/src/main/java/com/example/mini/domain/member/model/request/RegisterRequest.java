package com.example.mini.domain.member.model.request;

import com.example.mini.global.validation.EmailValidation;
import com.example.mini.global.validation.PasswordValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	private String name;

	private String nickname;

	@EmailValidation
	private String email;

	@PasswordValidation
	private String password;

}
