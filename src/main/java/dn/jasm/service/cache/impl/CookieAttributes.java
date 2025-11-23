package dn.jasm.service.cache.impl;


public enum CookieAttributes {

    COOKIE_SECRET_KEY("cookie-key"),
    COOKIE_HTTP_ONLY_AT("httpOnly"),
    COOKIE_SECURE_AT("secure"),
    COOKIE_MAX_AGE("maxAge"),
    COOKIE_PATH("path"),
    COOKIE_SAMESITE("sameSite"),
    COOKIE_DOMAIN("domain"),
    COOKIE_PATH_SYMBOL("/"),
    COOKIE_NAME("cookieName"),
    COOKIE_ATTRIBUTES("Cookie_Attributes"),
    REDIS_COOKIE_HASH_KEY("sessionAttr:cookieAttributes");



    CookieAttributes(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }
}
