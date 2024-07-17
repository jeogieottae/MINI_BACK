package com.example.mini.domain.accomodation.entity;

import com.example.mini.global.model.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
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
	@JoinColumn(name = "accomodation_id")
	private Accomodation accomodation;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "room", cascade = CascadeType.ALL)
	private List<RoomImage> images = new ArrayList<>();

}