package com.example.mini.domain.reservation.entity;

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
	private String peopleNumber;

	@Column(nullable = false)
	private Integer extraCharge;

	@Column(nullable = false)
	private Integer totalPrice;

	@Column(nullable = false)
	private LocalDateTime checkIn;

	@Column(nullable = false)
	private LocalDateTime checkOut;

	@OneToMany
	@JoinColumn(name = "room_id")
	private List<Room> roomList;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accommodation_id")
	private Accomodation accomodation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
}
