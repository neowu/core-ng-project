package core.framework.internal.web.sse;

import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.log.WarningContext;
import core.framework.log.IOWarning;
import core.framework.web.Request;
import core.framework.web.rate.LimitRate;
import core.framework.web.sse.Channel;
import core.framework.web.sse.ChannelListener;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;

class ChannelSupport<T> {
    final ChannelListener<T> listener;
    final ServerSentEventContextImpl<T> context;
    final ServerSentEventWriter<T> builder;
    @Nullable
    final LimitRate limitRate;
    final PerformanceWarning @Nullable [] warnings;

    ChannelSupport(ChannelListener<T> listener, Class<T> eventClass, ServerSentEventContextImpl<T> context) {
        this.listener = listener;
        this.context = context;
        builder = new ServerSentEventWriter<>(eventClass);

        try {
            Method targetMethod = listener.getClass().getMethod("onConnect", Request.class, Channel.class, String.class);
            limitRate = limitRate(targetMethod);
            warnings = WarningContext.warnings(targetMethod.getDeclaredAnnotationsByType(IOWarning.class));
        } catch (NoSuchMethodException e) {
            throw new Error("failed to get listener.onConnect method", e);
        }
    }

    @Nullable
    private LimitRate limitRate(Method targetMethod) {
        LimitRate limitRate = targetMethod.getDeclaredAnnotation(LimitRate.class);
        if (limitRate == null)
            limitRate = listener.getClass().getDeclaredAnnotation(LimitRate.class);
        return limitRate;
    }
}
