package com.example.mini.global.redis;

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
        throw new RuntimeException("락 처리 중 오류 발생: " + e.getMessage(), e);
      } finally {
        lock.unlock();
      }
    } else {
      throw new RuntimeException("키 " + lockKey + "에 대한 락을 획득할 수 없습니다.");
    }
  }

  @Around("@annotation(redissonQueue)")
  public Object redissonQueue(ProceedingJoinPoint joinPoint, RedissonQueue redissonQueue) throws Throwable {
    String queueName = redissonQueue.queueName();
    Object data;
    try {
      data = joinPoint.proceed();
    } catch (Exception e) {
      throw new RuntimeException("큐 처리 중 오류 발생: " + e.getMessage(), e);
    }

    RQueue<Object> queue = redissonClient.getQueue(queueName);
    queue.add(data);

    return data;
  }
}
