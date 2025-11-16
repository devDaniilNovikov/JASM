package dn.jasm.entity.enums;

public enum  CardStatus {

    ACTIVE("ACTIVE"),
    BLOCKED("BLOCKED");


    private final String value;

    CardStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
