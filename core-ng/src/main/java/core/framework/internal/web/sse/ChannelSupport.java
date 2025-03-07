package core.framework.internal.web.sse;

import core.framework.web.Request;
import core.framework.web.sse.Channel;
import core.framework.web.sse.ChannelListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

class ChannelSupport<T> {
    final ChannelListener<T> listener;
    final ServerSentEventContextImpl<T> context;
    final ServerSentEventBuilder<T> builder;
    final Method targetMethod;

    ChannelSupport(ChannelListener<T> listener, Class<T> eventClass, ServerSentEventContextImpl<T> context) {
        this.listener = listener;
        this.context = context;
        builder = new ServerSentEventBuilder<>(eventClass);
        try {
            this.targetMethod = listener.getClass().getMethod("onConnect", Request.class, Channel.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new Error("failed to get listener.onConnect method", e);
        }
    }

    <V extends Annotation> V annotation(Class<V> annotationClass) {
        V annotation = targetMethod.getDeclaredAnnotation(annotationClass);
        if (annotation == null)
            annotation = listener.getClass().getDeclaredAnnotation(annotationClass);
        return annotation;
    }
}
