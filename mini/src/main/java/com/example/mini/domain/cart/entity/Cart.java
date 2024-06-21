package com.example.mini.domain.cart.entity;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
