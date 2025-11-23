package dn.jasm.service.cache.impl;

public enum SessionAttributes {

    SESSION_PREFIX("spring:session"),
    SESSION_ID("sessionId"),
    USER_ID("userId"),
    USERNAME("username"),
    LOGIN_TIME("loginTime"),
    SESSION_CREATION_TIME("sessionCreationTime"),
    MAX_INACTIVE_INTERVAL("MaxInActiveInterval"),
    REDIS_KEYS_PREFIX("*"),
    REDIS_COOKIE_ATTRIBUTES("cookieAttributes");


    private final String value;

    SessionAttributes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
