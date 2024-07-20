package com.example.mini.domain.like.config;

import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.like.repository.LikeRepository;
import com.example.mini.global.redis.CacheService;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class LikeBatchConfiguration {

	private LikeRepository likeRepository;

	private CacheService cacheService;

	private JobRepository jobRepository;

	private PlatformTransactionManager transactionManager;

	@Bean
	public Job updateLikeCacheJob() {
		JobBuilder jobBuilder = new JobBuilder("updateLikeCacheJob", jobRepository);
		return jobBuilder
			.incrementer(new RunIdIncrementer())
			.start(updateLikeCacheStep())
			.build();
	}

	@Bean
	public Step updateLikeCacheStep() {
		StepBuilder stepBuilder = new StepBuilder("updateLikeCacheStep", jobRepository);
		return stepBuilder
			.tasklet(updateLikeCacheTasklet(), transactionManager)
			.build();
	}

	@Bean
	public Tasklet updateLikeCacheTasklet() { // Like 데이터를 데이터베이스에서 읽어와 Redis 캐시에 저장
		return (contribution, chunkContext) -> {
			List<Like> allLikes = likeRepository.findAll();
			for (Like like : allLikes) {
				cacheService.cacheLikeStatus(like.getMember().getId(), like.getAccomodation().getId(), like.isLiked());
			}
			return RepeatStatus.FINISHED;
		};
	}
}
