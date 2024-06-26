package com.example.mini.global.model.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Data
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@CreatedDate
	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedAt;


	@PrePersist
	public void prePersist() {
		if (this.getCreatedAt() == null) {
			this.setCreatedAt(LocalDateTime.now());
		}
		this.setUpdatedAt(null); // 최초에는 null로 설정
	}

	@PreUpdate
	public void preUpdate() {
		this.setUpdatedAt(LocalDateTime.now()); // 수정 시 현재 시간으로 설정
	}


}
