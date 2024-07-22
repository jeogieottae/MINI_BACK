package com.example.mini.global.email;

import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.model.request.ReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  @Async("mailExecutor")
  public void sendReservationConfirmationEmail(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);

    log.info("이메일을 성공적으로 전송했습니다. 수신자: {}, 제목: {}", to, subject);
  }

  public void sendConfirmationEmail(
      Member member, Reservation reservation, ConfirmCartItemRequest request) {
    String to = member.getEmail();
    String subject = "예약 확정 되었습니다";
    String text = String.format("귀하의 %s에서 %s 객실 예약이 확정되었습니다.\n체크인: %s\n체크아웃: %s\n인원 수: %d명\n총 가격: %d원",
        reservation.getRoom().getAccomodation().getName(),
        reservation.getRoom().getName(),
        request.getCheckIn(),
        request.getCheckOut(),
        request.getPeopleNumber(),
        reservation.getTotalPrice());

    sendReservationConfirmationEmail(to, subject, text);
  }

  public void sendReservationConfirmationEmail(
      Member member, Reservation reservation, ReservationRequest request) {
    String to = member.getEmail();
    String subject = "예약 확정 되었습니다";
    String text = String.format("귀하의 %s에서 %s 객실 예약이 확정되었습니다.\n체크인: %s\n체크아웃: %s\n인원 수: %d명\n총 가격: %d원",
        reservation.getRoom().getAccomodation().getName(),
        reservation.getRoom().getName(),
        request.getCheckIn(),
        request.getCheckOut(),
        request.getPeopleNumber(),
        reservation.getTotalPrice());

    sendReservationConfirmationEmail(to, subject, text);
  }
}