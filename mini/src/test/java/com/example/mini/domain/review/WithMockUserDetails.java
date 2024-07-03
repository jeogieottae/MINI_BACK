package com.example.mini.domain.review;

import com.example.mini.domain.member.entity.enums.MemberState;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserDetailsSecurityContextFactory.class)
public @interface WithMockUserDetails {
	String username() default "test@example.com";
	long id() default 1L;
	String name() default "홍길동";
	String nickname() default "길동이";
	MemberState state() default MemberState.ACTIVE;
}