package com.example.mini.domain.member.service;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberCleanupService {
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시
    @Transactional
    public void deleteSleepingMembers(){
        LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(3);

        List<Member> membersToDelete = memberRepository.findByStateAndUpdatedAtBefore(MemberState.DELETED, threeYearsAgo);

        memberRepository.deleteAll(membersToDelete);
    }
}
