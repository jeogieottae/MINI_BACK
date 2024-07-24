package com.example.mini.global.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchConfig {

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() { // 배치 작업을 위한 스레드 풀 구성
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(30);
		taskExecutor.initialize();
		return taskExecutor;
	}

	@Bean
	public SimpleAsyncTaskExecutor batchTaskExecutor() {
		return new SimpleAsyncTaskExecutor("spring_batch");
	}
}