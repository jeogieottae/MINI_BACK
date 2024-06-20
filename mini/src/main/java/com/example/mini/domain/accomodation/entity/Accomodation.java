package com.example.mini.domain.accomodation.entity;

import com.example.mini.global.model.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Accomodation extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private int postalCode;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private Boolean parkingAvailable;

	@Column(nullable = false)
	private Boolean cookingAvailable;

	@Column(nullable = false)
	private LocalDateTime checkIn;

	@Column(nullable = false)
	private LocalDateTime checkOut;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@OneToMany(mappedBy = "accomodation")
	private List<Room> rooms = new ArrayList<>();


}
