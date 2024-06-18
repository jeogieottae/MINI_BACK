package com.example.mini.domain.member.entity;

import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.cart.entity.Cart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
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
public class Member extends BaseEntity {

	@Column(nullable = false)
	private String email;

	@Column
	private String oauthEmail;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String nickname;

	@OneToOne(mappedBy = "member")
	private Cart cart;

}
