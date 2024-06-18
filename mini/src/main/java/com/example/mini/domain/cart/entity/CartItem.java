package com.example.mini.domain.cart.entity;


import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.global.model.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseEntity {

	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Integer personNumber;
	private String price;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	@OneToMany
	@JoinColumn(name = "room_id")
	private List<Room> roomList;

}
