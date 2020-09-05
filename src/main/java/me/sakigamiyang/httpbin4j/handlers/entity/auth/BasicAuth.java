package me.sakigamiyang.httpbin4j.handlers.entity.auth;

public class BasicAuth extends Auth {
    public static final String TYPE = "basic";

    private final String username;
    private final String password;

    public BasicAuth(String username, String password) {
        super(TYPE);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
