package com.example.mini.domain.review;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserDetailsSecurityContextFactory.class)
public @interface WithMockUserDetails {
	String username() default "test@example.com";
	long id() default 1L;
}
