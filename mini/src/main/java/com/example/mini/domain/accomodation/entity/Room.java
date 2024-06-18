package com.example.mini.domain.accomodation.entity;

import com.example.mini.domain.cart.entity.CartItem;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.global.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Room extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer baseGuests;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer maxGuests;

	@Column(nullable = false)
	private Integer extraPersonCharge;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_item_id")
	private CartItem cartItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accomodation_id")
	private Accomodation accomodation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id")
	private Reservation reservation;

}
