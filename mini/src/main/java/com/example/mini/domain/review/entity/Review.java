package com.example.mini.domain.review.entity;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.global.model.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
  @JoinColumn(name = "accomodation_id", nullable = false)
  private Accomodation accomodation;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id", nullable = false, unique = true)
  private Reservation reservation;
}
