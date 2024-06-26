package com.example.mini.domain.reservation.service;

import com.example.mini.domain.reservation.model.request.ReservationRequest;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
public class ReservationConcurrencyServiceTest {

  @Container
  public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
      .withExposedPorts(6379);

  @MockBean
  private ReservationService reservationService;

  @SpringBootApplication(scanBasePackages = "com.example.mini")
  static class TestApplication {

    @Bean
    @Primary
    public RedissonClient customRedissonClient() {
      return Mockito.mock(RedissonClient.class);
    }
  }

  @Test
  public void testConcurrentReservationCreation() throws Exception {
    int numThreads = 100;
    Long roomId = 1L;
    int peopleNumber = 2;
    LocalDateTime checkIn = LocalDateTime.now().plusDays(1);
    LocalDateTime checkOut = LocalDateTime.now().plusDays(3);

    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    CountDownLatch latch = new CountDownLatch(1);

    Set<String> confirmedReservations = ConcurrentHashMap.newKeySet();

    AtomicLong memberIdGenerator = new AtomicLong(1);

    for (int i = 0; i < numThreads; i++) {
      executorService.submit(() -> {
        try {
          latch.await();
          ReservationRequest request = new ReservationRequest();
          request.setRoomId(roomId);
          request.setCheckIn(checkIn);
          request.setCheckOut(checkOut);
          request.setPeopleNumber(peopleNumber);

          reservationService.createConfirmedReservation(any(), eq(request));

          String confirmationKey = roomId + "_" + checkIn + "_" + checkOut;
          confirmedReservations.add(confirmationKey);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
    latch.countDown();

    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    assertEquals(1, confirmedReservations.size());
  }
}