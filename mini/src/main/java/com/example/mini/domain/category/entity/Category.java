package com.example.mini.domain.category.entity;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.global.model.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "category")
	private List<Accomodation> accomodationList;



}
