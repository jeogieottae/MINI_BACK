package com.example.mini.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<EmailValidation, String> {

	private Pattern pattern;
	private static final String EMAIL_PATTERN =
		"^[a-zA-Z0-9.!#$%&'+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

	@Override
	public void initialize(EmailValidation constraintAnnotation) {
		pattern = Pattern.compile(EMAIL_PATTERN);
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value != null && pattern.matcher(value).matches();
	}
}
