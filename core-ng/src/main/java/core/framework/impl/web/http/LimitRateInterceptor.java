package core.framework.impl.web.http;

import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Response;
import core.framework.web.exception.TooManyRequestsException;
import core.framework.web.rate.LimitRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class LimitRateInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(LimitRateInterceptor.class);
    private final RateLimiter rateLimiter = new RateLimiter(1000);  // save at max 1000 group/ip combination

    @Override
    public Response intercept(Invocation invocation) throws Exception {
        LimitRate limitRate = invocation.annotation(LimitRate.class);
        if (limitRate != null) {
            String group = limitRate.value();
            String clientIP = invocation.context().request().clientIP();
            validateRate(group, clientIP);
        }
        return invocation.proceed();
    }

    void validateRate(String group, String clientIP) {
        logger.debug("acquire, group={}, clientIP={}", group, clientIP);
        boolean acquired = rateLimiter.acquire(group, clientIP);
        if (!acquired) {
            throw new TooManyRequestsException("rate exceeded");
        }
    }

    public void config(String group, int maxPermits, int fillRate, TimeUnit unit) {
        rateLimiter.config(group, maxPermits, fillRate, unit);
    }
}
