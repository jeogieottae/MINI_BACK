package com.example.mini.global.redis;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.RedissonErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedissonLockAspectTest {

  @Mock
  private RedissonClient redissonClient;

  @InjectMocks
  private RedissonLockAspect redissonLockAspect;

  @Mock
  private ProceedingJoinPoint joinPoint;

  @Mock
  private RLock mockLock;

  @Mock
  private RQueue<Object> mockQueue;

  @Test
  public void testRedissonLocSuccessful() throws Throwable {
    // Given
    RedissonLock redissonLockAnnotation = createMockRedissonLockAnnotation();
    when(redissonClient.getLock(any(String.class))).thenReturn(mockLock);
    when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
    Object expectedResult = "Test Result";
    when(joinPoint.proceed()).thenReturn(expectedResult);

    // When
    Object result = redissonLockAspect.redissonLock(joinPoint, redissonLockAnnotation);

    // Then
    assertEquals(expectedResult, result);
    verify(mockLock, times(1)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    verify(mockLock, times(1)).unlock();
  }

  @Test
  public void testRedissonLockLockNotAcquired() throws Throwable {
    // Given
    RedissonLock redissonLockAnnotation = createMockRedissonLockAnnotation();
    when(redissonClient.getLock(any(String.class))).thenReturn(mockLock);
    when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

    // When
    GlobalException exception = assertThrows(GlobalException.class, () -> redissonLockAspect.redissonLock(joinPoint, redissonLockAnnotation));

    // Then
    assertEquals(RedissonErrorCode.KEY_NOT_GAIN, exception.getErrorCode());
    verify(mockLock, times(1)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    verify(mockLock, never()).unlock();
  }

  @Test
  public void testRedissonQueueSuccessful() throws Throwable {
    // Given
    RedissonQueue redissonQueueAnnotation = createMockRedissonQueueAnnotation();
    when(redissonClient.getQueue(any(String.class))).thenReturn(mockQueue);
    Object expectedResult = "Test Result";
    when(joinPoint.proceed()).thenReturn(expectedResult);

    // When
    Object result = redissonLockAspect.redissonQueue(joinPoint, redissonQueueAnnotation);

    // Then
    assertEquals(expectedResult, result);
    verify(mockQueue, times(1)).add(any());
  }

  @Test
  public void testRedissonQueueQueueError() throws Throwable {
    // Given
    RedissonQueue redissonQueueAnnotation = createMockRedissonQueueAnnotation();
    RuntimeException testException = new RuntimeException("Test Exception");
    doThrow(testException).when(joinPoint).proceed();

    // When
    GlobalException exception = assertThrows(GlobalException.class, () -> redissonLockAspect.redissonQueue(joinPoint, redissonQueueAnnotation));

    // Then
    assertEquals(RedissonErrorCode.QUEUE_ERROR, exception.getErrorCode());
  }

  private RedissonLock createMockRedissonLockAnnotation() {
    return new RedissonLock() {
      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return RedissonLock.class;
      }

      @Override
      public String key() {
        return "testLockKey";
      }

      @Override
      public TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
      }

      @Override
      public long waitTime() {
        return 5L;
      }

      @Override
      public long leaseTime() {
        return 1L;
      }
    };
  }

  private RedissonQueue createMockRedissonQueueAnnotation() {
    return new RedissonQueue() {
      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return RedissonQueue.class;
      }

      @Override
      public String queueName() {
        return "testQueue";
      }
    };
  }
}

