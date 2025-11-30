package dn.jasm.configuration.redis;

public enum CacheNames {

    USER_CACHE("user: #"),
    PAYMENT_CACHE("payment: #"),
    ORDER_CACHE("order: #"),
    TRANSACTION_CACHE("transaction: #"),
    CARD_CACHE("card: #"),
    SHOP_CACHE("shop: #"),
    COMMENT_CACHE("comment #"),
    ITEM_CACHE("item #"),
    SHOP_LIST("shops #");


     CacheNames(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    private final String value;

}
