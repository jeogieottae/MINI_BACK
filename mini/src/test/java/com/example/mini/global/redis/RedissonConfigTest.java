package com.example.mini.global.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
@ContextConfiguration(classes = RedissonConfig.class)
public class RedissonConfigTest {

  @Container
  public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
      .withExposedPorts(6379);

  @Autowired
  private RedissonClient redissonClient;

  @BeforeEach
  public void setup() {
    String redisHost = redisContainer.getHost();
    int redisPort = redisContainer.getMappedPort(6379);
  }

  @Test
  public void redissonClientIsNotNull() {
    assertThat(redissonClient).isNotNull();
  }

  @Test
  public void redissonClientCanPing() {
    redissonClient.getBucket("test").set("hello");
    String result = (String) redissonClient.getBucket("test").get();
    assertThat(result).isEqualTo("hello");
  }

  @Test
  public void redissonClientCanSetAndGet() {
    String key = "test";
    String value = "hello";

    redissonClient.getBucket(key).set(value);

    String result = (String) redissonClient.getBucket(key).get();
    assertThat(result).isEqualTo(value);
  }
}
