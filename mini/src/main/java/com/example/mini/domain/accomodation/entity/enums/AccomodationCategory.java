package com.example.mini.domain.accomodation.entity.enums;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AccomodationErrorCode;

import java.util.Arrays;

public enum AccomodationCategory {
    SEOUL("서울"),
    BUSAN("부산"),
    DAEGU("대구"),
    INCHEON("인천"),
    DAEJEON("대전"),
    GWANGJU("광주"),
    ULSAN("울산"),
    SEJONG("세종"),
    GYEONGGI("경기"),
    GANGWON("강원"),
    CHUNGBUK("충북"),
    CHUNGNAM("충남"),
    JEONBUK("전북"),
    JEONNAM("전남"),
    GYEONGBUK("경북"),
    GYEONGNAM("경남"),
    JEJU("제주");

    private final String name;

    AccomodationCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public static AccomodationCategory fromName(String name) {
        return Arrays.stream(AccomodationCategory.values())
                .filter(category -> category.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new GlobalException(AccomodationErrorCode.INVALID_CATEGORY_CODE_REQUEST));
    }
}
