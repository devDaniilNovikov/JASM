package dn.jasm.entity.enums;

public enum  OrderStatus {
        NEW("new"),
        PROCESSING("processing"),
        PAID("paid"),
        SHIPPED("shipped"),
        DELIVERED("delivered"),
        CANCELLED("cancelled"),
        RETURNED("returned");

    OrderStatus(String value) {
        this.value = value;
    }

    private final String value;

    public String getValue() {
        return value;
    }
}

