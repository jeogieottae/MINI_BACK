package com.example.mini.global.email;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
public class AsyncHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(final Throwable ex, final Method method, final Object... params) {
    log.error(ex.getMessage(), ex);
  }
}
