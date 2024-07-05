package com.example.mini.global.redis;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.RedissonErrorCode;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RedissonLockAspect {

  @Autowired
  RedissonClient redissonClient;

  @Around("@annotation(redissonLock)")
  public Object redissonLock(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
    String lockKey = generateLockKey(joinPoint, redissonLock.key());
    long waitTime = redissonLock.waitTime();
    long leaseTime = redissonLock.leaseTime();
    TimeUnit timeUnit = redissonLock.timeUnit();

    boolean isLocked = false;
    RLock lock = null;
    try {
      lock = redissonClient.getLock(lockKey);
      isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
      if (isLocked) {
        log.info("성공 : {}", lockKey);
        return joinPoint.proceed();
      } else {
        log.warn("락 획득 실패: {}", lockKey);
        throw new GlobalException(RedissonErrorCode.KEY_NOT_GAIN);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GlobalException(RedissonErrorCode.KEY_INTERRUPTED);
    } catch (RedisException e) {
      throw new GlobalException(RedissonErrorCode.REDIS_ERROR);
    } finally {
      if (isLocked) {
        try {
          lock.unlock();
          log.info("락 해제 성공: {}", lockKey);
        } catch (RedisException e) {
          log.error("락 해제 실패: {}", lockKey, e);
          throw new GlobalException(RedissonErrorCode.REDIS_ERROR);
        }
      }
    }
  }

  public String generateLockKey(ProceedingJoinPoint joinPoint, String keyExpression) {
    MethodSignature nextSignature = (MethodSignature) joinPoint.getSignature();
    nextSignature.getMethod();
    Object[] args = joinPoint.getArgs();

    EvaluationContext context = new StandardEvaluationContext();
    String[] parameterNames = nextSignature.getParameterNames();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    ExpressionParser parser = new SpelExpressionParser();
    return parser.parseExpression(keyExpression).getValue(context, String.class);

  }
}