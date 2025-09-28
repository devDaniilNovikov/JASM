package dn.jasm.configuration.redis;

import dn.jasm.dto.ListDto;

import java.util.List;
import java.util.Objects;

public class RedisKeyGenerator {

    private static final String DEFAULT_PREFIX = "cache";
    private static  String prefix = null;

    public static void setPrefix(String keyPrefix){
        prefix = keyPrefix;
    }

    public static String getKey(String key){
        return getPrefix() + ":" + key;
    }

    public static String getPrefix(){
        ListDto<String> list = new ListDto<>();
        return Objects.requireNonNullElse(prefix,DEFAULT_PREFIX);
    }


}
