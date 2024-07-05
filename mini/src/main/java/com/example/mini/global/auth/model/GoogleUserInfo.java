package com.example.mini.global.auth.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleUserInfo {
    private final String id;
    private final String email;
    private final boolean emailVerified;
    private final String name;
    private final String givenName;
    private final String familyName;
    private final String picture;
    private final String locale;

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

}