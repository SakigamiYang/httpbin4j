package me.sakigamiyang.httpbin4j.handlers.entity.auth;

import java.util.Map;

public class DigestAuth extends Auth {
    public static final String TYPE = "digest";

    private final String username;
    private final String realm;
    private final String nonce;
    private final String uri;
    private final String response;
    private final String qop;
    private final String nc;
    private final String cnonce;
    private final Map<String, String> otherItems;

    public DigestAuth(String username,
                      String realm,
                      String nonce,
                      String uri,
                      String response,
                      String qop,
                      String nc,
                      String cnonce,
                      Map<String, String> otherItems) {
        super(TYPE);
        this.username = username;
        this.realm = realm;
        this.nonce = nonce;
        this.uri = uri;
        this.response = response;
        this.qop = qop;
        this.nc = nc;
        this.cnonce = cnonce;
        this.otherItems = otherItems;
    }

    public String getUsername() {
        return username;
    }

    public String getRealm() {
        return realm;
    }

    public String getNonce() {
        return nonce;
    }

    public String getUri() {
        return uri;
    }

    public String getResponse() {
        return response;
    }

    public String getQop() {
        return qop;
    }

    public String getNc() {
        return nc;
    }

    public String getCnonce() {
        return cnonce;
    }

    public Map<String, String> getOtherItems() {
        return otherItems;
    }
}
