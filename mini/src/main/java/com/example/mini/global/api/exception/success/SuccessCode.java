package com.example.mini.global.api.exception.success;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    OK(HttpStatus.OK,  "성공"),
    CREATED(HttpStatus.CREATED,  "등록 성공"),
    DELETE(HttpStatus.OK,  "삭제 성공"),

    /*AuthController*/
    NICKNAME_UPDATED(HttpStatus.OK,  "닉네임 변경 성공"),
    TOKEN_REFRESHED(HttpStatus.OK,  "엑세스 토큰 재발급 성공"),
    LOGOUT(HttpStatus.OK,  "로그아웃 성공"),
    WITHDRAW(HttpStatus.OK,  "탈퇴 성공"),
    REGISTER(HttpStatus.CREATED,  "회원가입 성공"),
    LOGIN(HttpStatus.CREATED,  "로그인 성공"),
    USER_INFO_RETRIEVED(HttpStatus.OK,  "사용자 정보 조회 성공"),


    /*AccomodationController*/
    ACCOMMODATIONS_RETRIEVED(HttpStatus.OK,  "숙소 목록 조회 성공"),
    CATEGORY_RETRIEVED(HttpStatus.OK,  "카테고리 조회 성공"),
    ACCOMMODATION_DETAILS_RETRIEVED(HttpStatus.OK,  "숙소 상세 정보 조회 성공"),
    ROOM_DETAILS_RETRIEVED(HttpStatus.OK,  "객실 상세 정보 조회 성공"),
    ACCOMMODATION_SEARCH_SUCCESS(HttpStatus.OK,  "숙소 검색 성공"),

    /*CartController*/
    CART_ITEMS_RETRIEVED(HttpStatus.OK,  "장바구니 품목 조회 성공"),
    CART_ITEM_ADDED(HttpStatus.CREATED,  "장바구니 품목 추가 성공"),
    CART_ITEM_CONFIRMED(HttpStatus.OK,  "장바구니 품목 확정 성공"),
    CART_ITEM_DELETED(HttpStatus.OK, "장바구니 품목 삭제 성공"),

    /*LikeController*/
    LIKE_TOGGLED(HttpStatus.OK,  "좋아요 토글 성공"),
    LIKED_ACCOMMODATIONS_RETRIEVED(HttpStatus.OK,  "좋아요 누른 숙소 조회 성공"),

    /*ReservationController*/
    RESERVATION_CONFIRMED(HttpStatus.CREATED,  "예약 확정 성공"),
    RESERVATIONS_RETRIEVED(HttpStatus.OK,  "예약 목록 조회 성공"),
    RESERVATION_DETAIL_RETRIEVED(HttpStatus.OK,  "예약 상세 정보 조회 성공"),

    /*ReviewController*/
    REVIEW_ADDED(HttpStatus.CREATED,  "리뷰 등록 성공"),
    REVIEWS_RETRIEVED(HttpStatus.OK,  "리뷰 목록 조회 성공"),

    /*GoogleAuthController*/
    GOOGLE_LOGIN_SUCCESS(HttpStatus.OK,  "구글 로그인 성공"),

    /*KakaoAuthController*/
    KAKAO_LOGIN_SUCCESS(HttpStatus.OK,  "카카오 로그인 성공")

    ;

    private final HttpStatus httpStatus;
    private final String description;
}