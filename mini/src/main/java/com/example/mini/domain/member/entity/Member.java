package com.example.mini.domain.member.entity;

import com.example.mini.domain.member.entity.enums.MemberRole;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.cart.entity.Cart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import lombok.*;
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

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@OneToOne(mappedBy = "member")
	private Cart cart;

	@Setter
	@Enumerated(EnumType.STRING)
	private MemberState state;

	public void setEmail(String email) {
	}


	@Override
	public String toString() {
		return "Member{" +
			"id=" + getId() +
			", email='" + email + '\'' +
			", name='" + name + '\'' +
			", state=" + state +
			'}';
	}

	public Member update(String name){
		this.name = name;
		return this;
	}


}
