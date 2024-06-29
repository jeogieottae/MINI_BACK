package com.example.mini.global.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonLock {
  String key();

  TimeUnit timeUnit() default TimeUnit.SECONDS;

  long waitTime() default 1;

  long leaseTime() default 10L;
}