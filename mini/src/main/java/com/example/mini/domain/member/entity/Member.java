package com.example.mini.domain.member.entity;

import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.global.model.entity.BaseEntity;
import com.example.mini.domain.cart.entity.Cart;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
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

	@Setter
	@Column (nullable = false)
	private String nickname;

	@OneToOne(mappedBy = "member")
	private Cart cart;

	@Setter
	@Enumerated(EnumType.STRING)
	private MemberState state;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
	private List<Like> likes;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
	private List<Review> reviews;

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
