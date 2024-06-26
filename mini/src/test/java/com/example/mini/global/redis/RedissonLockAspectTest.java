package com.example.mini.global.redis;

import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.RedissonErrorCode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
public class RedissonLockAspectTest {

  @Container
  public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
      .withExposedPorts(6379);

  @Autowired
  private RedissonClient redissonClient;

  @Autowired
  private RedissonLockAspect redissonLockAspect;

  @Autowired
  private ReservationService reservationService;

  @BeforeEach
  void setUp() {
    String redisUrl =
        "redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort();
  }


  @Test
  void testRedissonLockSuccessfulLockAndUnlock() throws Throwable {
    // Given
    String lockKey = "testKey";
    long waitTime = 5L;
    long leaseTime = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    // When
    RLock lock = redissonClient.getLock(lockKey);
    lock.lock(); // 직접 lock을 획득하는 코드

    // Then
    assertTrue(lock.isLocked(), "잠금이 성공적으로 획득되어야 합니다");
    assertTrue(lock.isHeldByCurrentThread(), "현재 스레드에서 잠금이 보유되어야 합니다");

    lock.unlock();
    assertFalse(lock.isLocked(), "잠금이 성공적으로 해제되어야 합니다");
  }

  @Test
  void testRedissonLockFailedToAcquireLock() throws Throwable {
    // Given
    String lockKey = "testKey";
    long waitTime = 0L;
    long leaseTime = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    // When
    RLock lock = redissonClient.getLock(lockKey);
    lock.lock();

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

    doThrow(new GlobalException(RedissonErrorCode.KEY_NOT_GAIN)).when(pjp).proceed();

    // Then
    try {
      assertThrows(GlobalException.class, () -> {
        redissonLockAspect.redissonLock(pjp, new RedissonLock() {
          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return RedissonLock.class;
          }

          @Override
          public String key() {
            return lockKey;
          }

          @Override
          public TimeUnit timeUnit() {
            return timeUnit;
          }

          @Override
          public long waitTime() {
            return waitTime;
          }

          @Override
          public long leaseTime() {
            return leaseTime;
          }
        });
      });
    } finally {
      lock.unlock();
    }
  }

  @Test
  void testRedissonLockInterruptedWhileWaitingForLock() throws Throwable {
    // Given
    String lockKey = "testKey";
    long waitTime = 5L;
    long leaseTime = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    Thread currentThread = Thread.currentThread();

    // When
    Thread interruptingThread = new Thread(() -> {
      try {
        Thread.sleep(1000); // 1초 후 인터럽트
        currentThread.interrupt();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    interruptingThread.start();

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

    doThrow(new InterruptedException()).when(pjp).proceed();

    // Then
    assertThrows(GlobalException.class, () -> {
      redissonLockAspect.redissonLock(pjp, new RedissonLock() {
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
          return RedissonLock.class;
        }

        @Override
        public String key() {
          return lockKey;
        }

        @Override
        public TimeUnit timeUnit() {
          return timeUnit;
        }

        @Override
        public long waitTime() {
          return waitTime;
        }

        @Override
        public long leaseTime() {
          return leaseTime;
        }
      });
    });

    Thread.interrupted();
  }
}