package com.example.mini.global.redis;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.email.EmailService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {RedissonConfig.class, RedissonLockAspect.class, ReservationService.class})
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

  @MockBean
  private ReservationRepository reservationRepository;

  @MockBean
  private RoomRepository roomRepository;

  @MockBean
  private MemberRepository memberRepository;

  @MockBean
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    String redisUrl = "redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort();
    System.setProperty("spring.data.redis.host", redisContainer.getHost());
    System.setProperty("spring.data.redis.port", String.valueOf(redisContainer.getFirstMappedPort()));
  }

  @Test
  void testRedissonLockSuccessfulLockAndUnlock() throws Throwable {
    // Given
    String lockKey = "confirmReservation_1_2023-01-01T00:00:00_2023-01-02T00:00:00";
    long waitTime = 5L;
    long leaseTime = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    // When
    RLock lock = redissonClient.getLock(lockKey);
    boolean isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);

    // Then
    assertTrue(isLocked, "잠금을 성공적으로 획득해야 합니다");
    assertTrue(lock.isHeldByCurrentThread(), "현재 스레드에서 잠금을 보유하고 있어야 합니다");

    lock.unlock();
    assertFalse(lock.isLocked(), "잠금을 성공적으로 해제해야 합니다");
  }

  @Test
  void testRedissonLockAspectIntegration() throws Throwable {
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(1L)
        .checkIn(LocalDateTime.of(2023, 1, 1, 0, 0))
        .checkOut(LocalDateTime.of(2023, 1, 2, 0, 0))
        .peopleNumber(2)
        .build();

    Member mockMember = Member.builder()
        .id(memberId)
        .email("test@example.com")
        .build();
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));

    Accomodation accomodation = Accomodation.builder()
        .name("test Accomodation")
        .description("testtesttest")
        .postalCode("12345")
        .address("test address")
        .parkingAvailable(true)
        .cookingAvailable(true)
        .build();

    Room room = Room.builder()
        .id(request.getRoomId())
        .name("test room")
        .baseGuests(3)
        .price(10000)
        .maxGuests(5)
        .extraPersonCharge(5000)
        .accomodation(accomodation)
        .build();
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));

    // Precondition
    String lockKey = "confirmReservation_" + request.getRoomId() + "_" + request.getCheckIn() + "_" + request.getCheckOut();
    RLock lock = redissonClient.getLock(lockKey);
    assertFalse(lock.isLocked(), "테스트 전에는 잠금이 없어야 합니다");

    // When
    ReservationResponse response = null;
    boolean isLocked = false;
    try {
      isLocked = lock.tryLock(5, TimeUnit.SECONDS);
      if (isLocked) {
        response = reservationService.createConfirmedReservation(memberId, request);
      } else {
        fail("잠금을 획득하지 못했습니다.");
      }
    } finally {
      if (isLocked) {
        lock.unlock();
      }
    }

    // Then
    assertNotNull(response, "예약 응답은 null이 아니어야 합니다");
    assertFalse(lock.isLocked(), "테스트 후에 잠금이 풀려 있어야 합니다");
  }

  @Test
  void testRedissonLockFailedToAcquireLock() throws Throwable {
    // Given
    String lockKey = "confirmReservation_1_2023-01-01T00:00:00_2023-01-02T00:00:00";
    long waitTime = 0L;
    long leaseTime = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    // Precondition
    RLock lock = redissonClient.getLock(lockKey);
    lock.lock();

    // When
    try {
      assertThrows(GlobalException.class, () -> {
        reservationService.createConfirmedReservation(1L, new ReservationRequest(1L, LocalDateTime.of(2023, 1, 1, 0, 0), LocalDateTime.of(2023, 1, 2, 0, 0), 2));
      });
    } finally {
      lock.unlock();
    }
  }

  @Test
  void testRedissonLockInterruptedWhileWaitingForLock() throws Throwable {
    // Given
    String lockKey = "confirmReservation_1_2023-01-01T00:00:00_2023-01-02T00:00:00";
    long waitTime = 5L;
    long leaseTime = 1L;
    TimeUnit timeUnit = TimeUnit.SECONDS;

    Thread currentThread = Thread.currentThread();

    // When
    Thread interruptingThread = new Thread(() -> {
      try {
        Thread.sleep(1000);
        currentThread.interrupt();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    interruptingThread.start();

    try {
      assertThrows(GlobalException.class, () -> {
        reservationService.createConfirmedReservation(1L, new ReservationRequest(1L, LocalDateTime.of(2023, 1, 1, 0, 0), LocalDateTime.of(2023, 1, 2, 0, 0), 2));
      });
    } finally {
      Thread.interrupted();
    }
  }
}