package dn.jasm.entity.enums;

public enum ShopCategory {

    OPTOVIK("OPTOVIK");


    private final String value;

    ShopCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
