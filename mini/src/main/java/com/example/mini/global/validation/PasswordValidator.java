package com.example.mini.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordValidation, String> {

	private static final String PASSWORD_PATTERN = "^.{8,16}$"; // 8자 이상 16자 이하

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value != null && value.matches(PASSWORD_PATTERN);
	}
}
