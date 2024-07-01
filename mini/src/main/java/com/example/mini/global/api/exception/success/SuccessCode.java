package com.example.mini.global.api.exception.success;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    OK(HttpStatus.OK, 200, "성공"),
    CREATED(HttpStatus.CREATED, 201, "등록 성공"),
    DELETE(HttpStatus.OK, 202, "삭제 성공"),

    /*AuthController*/
    NICKNAME_UPDATED(HttpStatus.OK, 203, "닉네임 변경 성공"),
    TOKEN_REFRESHED(HttpStatus.OK, 204, "엑세스 토큰 재발급 성공"),
    LOGOUT(HttpStatus.OK, 205, "로그아웃 성공"),
    WITHDRAW(HttpStatus.OK, 206, "탈퇴 성공"),
    REGISTER(HttpStatus.CREATED, 207, "회원가입 성공"),
    LOGIN(HttpStatus.CREATED, 208, "로그인 성공"),
    USER_INFO_RETRIEVED(HttpStatus.OK, 228, "사용자 정보 조회 성공"),


    /*AccomodationController*/
    ACCOMMODATIONS_RETRIEVED(HttpStatus.OK, 208, "숙소 목록 조회 성공"),
    CATEGORY_RETRIEVED(HttpStatus.OK, 209, "카테고리 조회 성공"),
    ACCOMMODATION_DETAILS_RETRIEVED(HttpStatus.OK, 210, "숙소 상세 정보 조회 성공"),
    ROOM_DETAILS_RETRIEVED(HttpStatus.OK, 211, "객실 상세 정보 조회 성공"),
    ACCOMMODATION_SEARCH_SUCCESS(HttpStatus.OK, 229, "숙소 검색 성공"),

    /*CartController*/
    CART_ITEMS_RETRIEVED(HttpStatus.OK, 212, "장바구니 품목 조회 성공"),
    CART_ITEM_ADDED(HttpStatus.CREATED, 213, "장바구니 품목 추가 성공"),
    CART_ITEM_CONFIRMED(HttpStatus.OK, 214, "장바구니 품목 확정 성공"),
    CART_ITEM_DELETED(HttpStatus.OK, 215, "장바구니 품목 삭제 성공"),

    /*LikeController*/
    LIKE_TOGGLED(HttpStatus.OK, 216, "좋아요 토글 성공"),
    LIKED_ACCOMMODATIONS_RETRIEVED(HttpStatus.OK, 217, "좋아요 누른 숙소 조회 성공"),

    /*ReservationController*/
    RESERVATION_CONFIRMED(HttpStatus.CREATED, 218, "예약 확정 성공"),
    RESERVATIONS_RETRIEVED(HttpStatus.OK, 219, "예약 목록 조회 성공"),
    RESERVATION_DETAIL_RETRIEVED(HttpStatus.OK, 220, "예약 상세 정보 조회 성공"),

    /*ReviewController*/
    REVIEW_ADDED(HttpStatus.CREATED, 221, "리뷰 등록 성공"),
    REVIEWS_RETRIEVED(HttpStatus.OK, 222, "리뷰 목록 조회 성공"),

    /*GoogleAuthController*/
    GOOGLE_LOGIN_SUCCESS(HttpStatus.OK, 223, "구글 로그인 성공"),

    /*KakaoAuthController*/
    KAKAO_LOGIN_SUCCESS(HttpStatus.OK, 230, "카카오 로그인 성공")

    ;

    private final HttpStatus httpStatus;
    private final int successCode;
    private final String description;
}