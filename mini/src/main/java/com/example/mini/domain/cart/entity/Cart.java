package com.example.mini.domain.cart.entity;

import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

	@OneToMany(mappedBy = "cart")
	private List<CartItem> cartItem = new ArrayList<>();


}
