package core.framework.internal.web.http;

import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Response;
import core.framework.web.rate.LimitRate;

/**
 * @author neo
 */
public class LimitRateInterceptor implements Interceptor {
    private final RateControl rateControl;

    public LimitRateInterceptor(RateControl rateControl) {
        this.rateControl = rateControl;
    }

    @Override
    public Response intercept(Invocation invocation) throws Exception {
        LimitRate limitRate = invocation.annotation(LimitRate.class);
        if (limitRate != null) {
            String group = limitRate.value();
            String clientIP = invocation.context().request().clientIP();
            rateControl.validateRate(group, clientIP);
        }
        return invocation.proceed();
    }
}
