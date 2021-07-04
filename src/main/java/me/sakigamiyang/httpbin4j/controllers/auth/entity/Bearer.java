package me.sakigamiyang.httpbin4j.controllers.auth.entity;

import lombok.Getter;

public class Bearer {
    @Getter
    private final String token;

    private Bearer(String token) {
        this.token = token;
    }

    public static Bearer create(AuthInfo authInfo) {
        if (authInfo == null) {
            return null;
        }
        return new Bearer(authInfo.getContent());
    }
}
