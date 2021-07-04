package me.sakigamiyang.httpbin4j.controllers.auth.entity;

import lombok.Getter;

public class AuthInfo {
    private AuthInfo(String type, String content) {
        this.type = type;
        this.content = content;
    }

    @Getter
    private final String type;

    @Getter
    private final String content;

    public static AuthInfo create(String headerValue) {
        if (headerValue == null) {
            return null;
        }

        try {
            String[] tempArray = headerValue.split(" ", 2);
            return new AuthInfo(tempArray[0].toLowerCase().trim(), tempArray[1].trim());
        } catch (Throwable t) {
            return null;
        }
    }
}
