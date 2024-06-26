package com.example.mini.domain.reservation.service;

import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
public class ReservationConcurrencyServiceTest {

  @Container
  public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
      .withExposedPorts(6379);

  private static RedissonClient redissonClient;

  @Autowired
  private ReservationService reservationService;

  @BeforeAll
  public static void setUp() {
    Config config = new Config();
    config.useSingleServer()
        .setAddress("redis://" + redisContainer.getHost() + ":" + redisContainer.getMappedPort(6379));
    redissonClient = Redisson.create(config);
  }

  @Test
  public void testConcurrentReservationCreation() throws InterruptedException, ExecutionException, TimeoutException {
    int numThreads = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    CompletionService<ReservationResponse> completionService = new ExecutorCompletionService<>(executorService);

    LocalDateTime checkIn = LocalDateTime.now().plusDays(1);
    LocalDateTime checkOut = LocalDateTime.now().plusDays(3);
    int peopleNumber = 2;

    ReservationRequest request = new ReservationRequest();
    request.setCheckIn(checkIn);
    request.setCheckOut(checkOut);
    request.setPeopleNumber(peopleNumber);

    for (int i = 0; i < numThreads; i++) {
      final long memberId = i + 1;
      completionService.submit(() -> reservationService.createConfirmedReservation(memberId, request));
    }

    for (int i = 0; i < numThreads; i++) {
      Future<ReservationResponse> future = completionService.take();
      try {
        ReservationResponse response = future.get(5, TimeUnit.SECONDS);
        assertEquals(checkIn, response.getCheckIn());
      } catch (ExecutionException | TimeoutException e) {
      }
    }
  }
}