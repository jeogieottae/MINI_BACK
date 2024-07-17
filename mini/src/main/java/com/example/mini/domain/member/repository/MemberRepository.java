package com.example.mini.domain.member.repository;

import com.example.mini.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.mini.domain.member.entity.enums.MemberState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByNickname(String nickname);
	List<Member> findByStateAndUpdatedAtBefore(MemberState state, LocalDateTime date);
}
