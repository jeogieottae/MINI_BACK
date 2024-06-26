package com.example.mini.global.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

  private static final String REDIS_HOST = "redis"; // 혹은 직접 IP 주소 입력
  private static final int REDIS_PORT = 6379;
  private static final String REDIS_PASSWORD = "1234";
  private static final String REDISSON_HOST_PREFIX = "redis://";

  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
        .setAddress(REDISSON_HOST_PREFIX + REDIS_HOST + ":" + REDIS_PORT)
        .setPassword(REDIS_PASSWORD);
    return Redisson.create(config);
  }
}
