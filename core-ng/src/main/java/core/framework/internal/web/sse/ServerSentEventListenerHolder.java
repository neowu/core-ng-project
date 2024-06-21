package core.framework.internal.web.sse;

import core.framework.web.sse.ServerSentEventListener;

class ServerSentEventListenerHolder<T> {
    final ServerSentEventListener<T> listener;
    final ServerSentEventContextImpl<T> context;
    final ServerSentEventBuilder<T> builder;

    ServerSentEventListenerHolder(ServerSentEventListener<T> listener, Class<T> eventClass, ServerSentEventContextImpl<T> context) {
        this.listener = listener;
        this.context = context;
        this.builder = new ServerSentEventBuilder<>(eventClass);
    }
}
