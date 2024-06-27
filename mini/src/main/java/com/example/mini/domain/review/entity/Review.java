package com.example.mini.domain.review.entity;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.global.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Review extends BaseEntity {

  @Column(nullable = false, columnDefinition = "TEXT")
  private String comment;

  @Column(nullable = false)
  private int star;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "accommodation_id", nullable = false)
  private Accomodation accomodation;
}
