package core.framework.internal.web.sse;

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

    ChannelSupport(ChannelListener<T> listener, Class<T> eventClass, ServerSentEventContextImpl<T> context) {
        this.listener = listener;
        this.context = context;
        builder = new ServerSentEventWriter<>(eventClass);
        limitRate = limitRate(listener);
    }

    @Nullable
    private LimitRate limitRate(ChannelListener<T> listener) {
        try {
            Method targetMethod = listener.getClass().getMethod("onConnect", Request.class, Channel.class, String.class);
            LimitRate limitRate = targetMethod.getDeclaredAnnotation(LimitRate.class);
            if (limitRate == null)
                limitRate = listener.getClass().getDeclaredAnnotation(LimitRate.class);
            return limitRate;
        } catch (NoSuchMethodException e) {
            throw new Error("failed to get listener.onConnect method", e);
        }
    }
}
