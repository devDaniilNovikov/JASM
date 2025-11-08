package dn.jasm.service.cache;

import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;

import java.util.List;
import java.util.Set;

public interface UserCacheService {

    UserResponse getValueFromCache(String id);

    UserResponseList getValuesFromCache(Set<String> strings);

    void putToCache(String key,
                    Object value);

    void putValuesToCache(List<String> keys,
                          List<Object> values);
}
