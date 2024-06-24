package com.example.mini.global.redis;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.RedissonErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RQueue;
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
    boolean isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
    if (isLocked) {
      try {
        return joinPoint.proceed();
      } catch (Exception e) {
        throw new GlobalException(RedissonErrorCode.KEY_INTERRUPTED);
      } finally {
        lock.unlock();
      }
    } else {
      throw new GlobalException(RedissonErrorCode.KEY_NOT_GAIN);
    }
  }

  @Around("@annotation(redissonQueue)")
  public Object redissonQueue(ProceedingJoinPoint joinPoint, RedissonQueue redissonQueue) throws Throwable {
    String queueName = redissonQueue.queueName();
    Object data;
    try {
      data = joinPoint.proceed();
    } catch (Exception e) {
      throw new GlobalException(RedissonErrorCode.QUEUE_ERROR);
    }

    RQueue<Object> queue = redissonClient.getQueue(queueName);
    try {
      queue.add(data);
    } catch (Exception e) {
      throw new GlobalException(RedissonErrorCode.QUEUE_DATA_NOT_ADDED);
    }

    return data;
  }
}
