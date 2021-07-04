package me.sakigamiyang.httpbin4j.controllers.auth.entity;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Basic {
    @Getter
    private final String user;
    @Getter
    private final String passwd;

    private Basic(String user, String passwd) {
        this.user = user;
        this.passwd = passwd;
    }

    public static Basic create(AuthInfo authInfo) {
        if (authInfo == null) {
            return null;
        }
        try {
            String[] userPasswd = new String(
                    Base64.getDecoder().decode(authInfo.getContent()),
                    StandardCharsets.UTF_8).split(":", 2);
            return new Basic(userPasswd[0].trim(), userPasswd[1].trim());
        } catch (Throwable t) {
            return null;
        }
    }
}
