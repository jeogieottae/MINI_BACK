package com.example.mini.domain.cart.entity;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.member.entity.Member;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Table(name = "cart")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@EqualsAndHashCode(callSuper = true)
public class Cart extends BaseEntity {

	@OneToOne
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToMany(fetch = FetchType.LAZY)
	@Setter
	private List<Room> roomList = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Setter
	private List<Reservation> reservationList = new ArrayList<>();

}
