package dn.jasm.configuration.rabbit;

public enum RabbitHeaders {
    MESSAGE_ID("MESSAGE-ID"),
    CORRELATION_ID("CORRELATION-ID"),
    EVENT_ID("EVENT-ID"),
    EVENT_TYPE("EVENT_TYPE");


    private final String value;


    RabbitHeaders(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
