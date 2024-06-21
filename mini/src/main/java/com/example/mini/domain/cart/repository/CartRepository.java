package com.example.mini.domain.cart.repository;

import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.member.entity.Member;
import java.util.Optional;
import org.aspectj.weaver.MemberImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByMember(Member member);

}