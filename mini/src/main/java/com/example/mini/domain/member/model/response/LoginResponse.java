package com.example.mini.domain.member.model.response;

import com.example.mini.domain.member.entity.enums.MemberState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

	private MemberState state;
	private String accessToken;
	private String refreshToken;

}
