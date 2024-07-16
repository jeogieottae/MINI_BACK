package com.example.mini.domain.member.model.request;


import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class ChangeNicknameRequest {

    private String nickname;

}