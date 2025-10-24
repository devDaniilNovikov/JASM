package dn.jasm.service;

import java.time.Duration;

public interface RateLimiterService {

    boolean allowRequest(String clientId,
                         int limit,
                         Duration timeOut);
}
