package dn.jasm.entity.enums;

public enum  CardType {

    DEBIT("debit"),
    CREDIT("credit");


    private final String value;


    public String getValue() {
        return value;
    }

    CardType(String value) {
        this.value = value;
    }
}
