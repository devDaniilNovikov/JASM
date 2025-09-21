package dn.jasm.configuration.redis;


import dn.jasm.entity.enums.OrderStatus;

public class RedisSchema {

    public static String userKeys(){
        return RedisKeyGenerator.getKey("users");
    }

    public static String paymentKey(long userId, OrderStatus orderStatus){
        return RedisKeyGenerator.getKey("users:" + userId + ":" + orderStatus.name().toLowerCase());
    }
}
