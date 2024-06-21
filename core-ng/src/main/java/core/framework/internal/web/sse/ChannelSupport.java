package core.framework.internal.web.sse;

import core.framework.web.sse.ChannelListener;

class ChannelSupport<T> {
    final ChannelListener<T> listener;
    final ServerSentEventContextImpl<T> context;
    final ServerSentEventBuilder<T> builder;

    ChannelSupport(ChannelListener<T> listener, Class<T> eventClass, ServerSentEventContextImpl<T> context) {
        this.listener = listener;
        this.context = context;
        builder = new ServerSentEventBuilder<>(eventClass);
    }
}
