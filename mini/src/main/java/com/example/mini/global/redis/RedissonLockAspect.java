package com.example.mini.global.redis;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.RedissonErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RedissonLockAspect {

  @Autowired
  private RedissonClient redissonClient;

  @Around("@annotation(redissonLock)")
  public Object redissonLock(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
    String lockKey = redissonLock.key();
    long waitTime = redissonLock.waitTime();
    long leaseTime = redissonLock.leaseTime();
    TimeUnit timeUnit = redissonLock.timeUnit();

    RLock lock = redissonClient.getLock(lockKey);
    boolean isLocked = false;
    try {
      isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
      if (isLocked) {
        return joinPoint.proceed();
      } else {
        throw new GlobalException(RedissonErrorCode.KEY_NOT_GAIN);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GlobalException(RedissonErrorCode.KEY_INTERRUPTED);
    } finally {
      if (isLocked) {
        lock.unlock();
      }
    }
  }
}
