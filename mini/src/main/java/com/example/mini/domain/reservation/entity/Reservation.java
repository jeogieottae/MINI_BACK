package com.example.mini.domain.reservation.entity;

import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.Accomodation;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "reservation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Reservation extends BaseEntity {

	@Column(nullable = false)
	private Integer peopleNumber;

	@Column(nullable = false)
	private Integer extraCharge;

	@Column(nullable = false)
	private Integer totalPrice;

	@Column(nullable = false)
	private LocalDateTime checkIn;

	@Column(nullable = false)
	private LocalDateTime checkOut;

	@ManyToMany(fetch = FetchType.LAZY)
	private List<Cart> cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private Room room;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accomodation_id")
	private Accomodation accomodation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
}
