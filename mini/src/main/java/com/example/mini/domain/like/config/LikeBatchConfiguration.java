package com.example.mini.domain.like.config;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.like.repository.LikeRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
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
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class LikeBatchConfiguration {

	private final LikeRepository likeRepository;
	private final MemberRepository memberRepository;
	private final AccomodationRepository accomodationRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

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
	public Tasklet updateLikeCacheTasklet() {
		return (contribution, chunkContext) -> {
			// Redis의 모든 좋아요 상태를 조회하여 데이터베이스에 저장
			Set<String> keys = redisTemplate.keys("like::*");
			if (keys != null) {
				for (String key : keys) {
					String[] parts = key.split("::");
					Long memberId = Long.parseLong(parts[1]);
					Long accomodationId = Long.parseLong(parts[2]);
					Boolean isLiked = (Boolean) redisTemplate.opsForValue().get(key);

					Member member = memberRepository.findById(memberId).orElse(null);
					Accomodation accomodation = accomodationRepository.findById(accomodationId).orElse(null);

					if (member != null && accomodation != null) {
						Like like = likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId)
							.orElseGet(() -> new Like(member, accomodation, isLiked));
						like.setLiked(isLiked);
						likeRepository.save(like);
					}
				}
			}
			return RepeatStatus.FINISHED;
		};
	}
}
