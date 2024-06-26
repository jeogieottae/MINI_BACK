package com.example.mini.global.auth.model;

import lombok.Getter;

@Getter
public class TokenResponse {
    private String token_type;
    private String access_token;
    private String id_token;
    private Integer expires_in;
    private String refresh_token;
    private Integer refresh_token_expires_in;
}
