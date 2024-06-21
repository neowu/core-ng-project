package core.framework.internal.web.sse;

import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.Validator;
import core.framework.util.Strings;
import core.framework.web.sse.ChannelListener;

class ChannelSupport<T> {
    final ChannelListener<T> listener;
    final ServerSentEventContextImpl<T> context;
    private final JSONWriter<T> writer;
    private final Validator<T> validator;

    ChannelSupport(ChannelListener<T> listener, Class<T> eventClass, ServerSentEventContextImpl<T> context) {
        this.listener = listener;
        this.context = context;
        writer = new JSONWriter<>(eventClass);
        validator = Validator.of(eventClass);
    }

    byte[] data(T event) {
        validator.validate(event, false);
        return writer.toJSON(event);
    }

    byte[] message(String id, byte[] data) {
        byte[] idBytes = id == null ? null : Strings.bytes(id);
        byte[] message = new byte[data.length + 7 + (idBytes == null ? 0 : idBytes.length + 4)];
        int index = 0;
        if (idBytes != null) {
            message[index++] = (byte) 'i';
            message[index++] = (byte) 'd';
            message[index++] = (byte) ':';
            System.arraycopy(idBytes, 0, message, 3, idBytes.length);
            index += idBytes.length;
            message[index++] = (byte) '\n';
        }
        message[index++] = (byte) 'd';
        message[index++] = (byte) 'a';
        message[index++] = (byte) 't';
        message[index++] = (byte) 'a';
        message[index++] = (byte) ':';
        System.arraycopy(data, 0, message, index, data.length);
        index += data.length;
        message[index++] = (byte) '\n';
        message[index] = (byte) '\n';
        return message;
    }
}
