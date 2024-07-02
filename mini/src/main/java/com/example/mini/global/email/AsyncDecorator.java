package com.example.mini.global.email;

import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class AsyncDecorator implements TaskDecorator {
  @Override
  public Runnable decorate(Runnable runnable) {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

    return() -> {
      try{
        RequestContextHolder.setRequestAttributes(attributes);

        runnable.run();
      } finally {
        RequestContextHolder.resetRequestAttributes();
      }
    };
  }
}
