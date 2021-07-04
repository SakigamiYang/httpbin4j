package me.sakigamiyang.httpbin4j.controllers.auth.entity;

import lombok.Getter;
import me.sakigamiyang.httpbin4j.HttpUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Digest {
    private static final String DIGEST_AUTH_INFO_KEY_USERNAME = "username";
    private static final String DIGEST_AUTH_INFO_KEY_REALM = "realm";
    private static final String DIGEST_AUTH_INFO_KEY_NONCE = "nonce";
    private static final String DIGEST_AUTH_INFO_KEY_URI = "uri";
    private static final String DIGEST_AUTH_INFO_KEY_RESPONSE = "response";
    private static final String DIGEST_AUTH_INFO_KEY_QOP = "qop";
    private static final String DIGEST_AUTH_INFO_KEY_NC = "nc";
    private static final String DIGEST_AUTH_INFO_KEY_CNONCE = "cnonce";
    private static final List<String> DIGEST_AUTH_INFO_MUST_CONTAIN_LIST =
            Arrays.asList(
                    DIGEST_AUTH_INFO_KEY_USERNAME,
                    DIGEST_AUTH_INFO_KEY_REALM,
                    DIGEST_AUTH_INFO_KEY_NONCE,
                    DIGEST_AUTH_INFO_KEY_URI,
                    DIGEST_AUTH_INFO_KEY_RESPONSE);

    @Getter
    private final String user;
    @Getter
    private final String realm;
    @Getter
    private final String nonce;
    @Getter
    private final String uri;
    @Getter
    private final String response;
    @Getter
    private final String qop;
    @Getter
    private final String nc;
    @Getter
    private final String cnonce;
    @Getter
    private final Map<String, String> headerDict;

    private Digest(
            String user,
            String realm,
            String nonce,
            String uri,
            String response,
            String qop,
            String nc,
            String cnonce,
            Map<String, String> headerDict) {
        this.user = user;
        this.realm = realm;
        this.nonce = nonce;
        this.uri = uri;
        this.response = response;
        this.qop = qop;
        this.nc = nc;
        this.cnonce = cnonce;
        this.headerDict = headerDict;
    }

    public static Digest create(AuthInfo authInfo) {
        if (authInfo == null) {
            return null;
        }
        try {
            Map<String, String> headerDict = HttpUtil.parseDictHeader(authInfo.getContent());

            for (String key : DIGEST_AUTH_INFO_MUST_CONTAIN_LIST) {
                if (!headerDict.containsKey(key)) {
                    return null;
                }
            }
            if (headerDict.containsKey(DIGEST_AUTH_INFO_KEY_QOP)) {
                if (headerDict.getOrDefault(DIGEST_AUTH_INFO_KEY_NC, "").isEmpty() ||
                        headerDict.getOrDefault(DIGEST_AUTH_INFO_KEY_CNONCE, "").isEmpty()) {
                    return null;
                }
            }

            String username = headerDict.remove(DIGEST_AUTH_INFO_KEY_USERNAME);
            String realm = headerDict.remove(DIGEST_AUTH_INFO_KEY_REALM);
            String nonce = headerDict.remove(DIGEST_AUTH_INFO_KEY_NONCE);
            String uri = headerDict.remove(DIGEST_AUTH_INFO_KEY_URI);
            String response = headerDict.remove(DIGEST_AUTH_INFO_KEY_RESPONSE);
            String qop = headerDict.remove(DIGEST_AUTH_INFO_KEY_QOP);
            String nc = headerDict.remove(DIGEST_AUTH_INFO_KEY_NC);
            String cnonce = headerDict.remove(DIGEST_AUTH_INFO_KEY_CNONCE);

            return new Digest(
                    username,
                    realm,
                    nonce,
                    uri,
                    response,
                    qop,
                    nc,
                    cnonce,
                    headerDict
            );
        } catch (Throwable t) {
            return null;
        }
    }
}
