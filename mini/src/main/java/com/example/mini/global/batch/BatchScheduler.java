package com.example.mini.global.batch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class BatchScheduler { // 배치 작업을 주기적으로 실행하기 위한 스케줄러

	private JobLauncher jobLauncher;

	private Job updateLikeCacheJob;

	@Scheduled(cron = "0 */5 * * * *") // 매 5분마다 실행
	public void runUpdateLikeCacheJob() {
		try {
			jobLauncher.run(updateLikeCacheJob, new JobParametersBuilder().addLong("startAt", System.currentTimeMillis()).toJobParameters());
		} catch (Exception e) {
			log.error("updateLikeCacheJob을 실행하는데 실패하였습니다.", e);
		}
	}
}
