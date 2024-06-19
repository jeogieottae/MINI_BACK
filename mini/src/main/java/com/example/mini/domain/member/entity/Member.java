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

	@Enumerated(EnumType.STRING)
	private MemberState state;

	public void updatePassword(String password) {
		this.password = password;
	}

	public void updateNickName(String nickname) {
		this.nickname = nickname;
	}

	public void changeState(MemberState state) {
		this.state = state;
	}


	public void setEmail(String email) {
	}

	public void setOauthEmail(String email) {
	}

	public void setNickname(String s) {
	}

	public void setPassword(String kakaoOauthPassword) {
	}
}
