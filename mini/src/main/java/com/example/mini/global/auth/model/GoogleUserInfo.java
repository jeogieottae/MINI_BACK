package com.example.mini.global.auth.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleUserInfo {
    private String id;
    private String email;
    private boolean emailVerified;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
    private String locale;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.id = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.emailVerified = (Boolean) attributes.get("email_verified");
        this.name = (String) attributes.get("name");
        this.givenName = (String) attributes.get("given_name");
        this.familyName = (String) attributes.get("family_name");
        this.picture = (String) attributes.get("picture");
        this.locale = (String) attributes.get("locale");
    }

    // 필요에 따라 추가 메서드를 구현할 수 있습니다.
    public String getFullName() {
        return this.givenName + " " + this.familyName;
    }
}