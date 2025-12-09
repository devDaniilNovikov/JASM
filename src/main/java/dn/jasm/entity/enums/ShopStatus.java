package dn.jasm.entity.enums;

public enum ShopStatus {

    BANNED("BANNED"),
    ACTIVE("ACTIVE"),
    NEW("NEW");

    ShopStatus(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }
}
