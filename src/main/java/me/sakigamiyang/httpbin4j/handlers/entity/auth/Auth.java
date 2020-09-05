package me.sakigamiyang.httpbin4j.handlers.entity.auth;

public abstract class Auth {
    protected final String authType;

    public Auth(String authType) {
        this.authType = authType;
    }
}
