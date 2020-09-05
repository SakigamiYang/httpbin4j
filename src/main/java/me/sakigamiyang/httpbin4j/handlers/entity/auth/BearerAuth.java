package me.sakigamiyang.httpbin4j.handlers.entity.auth;

public class BearerAuth extends Auth {
    public static final String TYPE = "bearer";

    private final String token;

    public BearerAuth(String token) {
        super(TYPE);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
