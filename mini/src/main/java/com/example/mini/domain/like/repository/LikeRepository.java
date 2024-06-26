package com.example.mini.domain.like.repository;

import com.example.mini.domain.like.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

  Optional<Like> findByMemberIdAndAccomodationId(Long memberId, Long accomodationId);

  Page<Like> findByMemberIdAndIsLiked(Long memberId, boolean isLiked, Pageable pageable);
}
