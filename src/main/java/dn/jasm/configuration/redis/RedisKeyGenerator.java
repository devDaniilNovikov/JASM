package dn.jasm.configuration.redis;

import dn.jasm.dto.generics.ListDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class RedisKeyGenerator {

    private static final String DEFAULT_PREFIX = "cache";
    private static  String prefix = null;

    @Value("${spring.cache.cache-names}")
    private  List<String> cacheKeys = new ArrayList<>();

    public static void setPrefix(String keyPrefix){
        prefix = keyPrefix;
    }
    public static String getKey(String key){
        return getPrefix() + ":" + key;
    }

    public static String getPrefix(){
        return Objects.requireNonNullElse(prefix,DEFAULT_PREFIX);
    }



}
