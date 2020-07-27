package core.framework.internal.web.websocket;

import core.framework.internal.validate.ValidationException;
import core.framework.internal.web.bean.BeanMapper;
import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;
import core.framework.web.rate.LimitRate;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class ChannelHandler {
    final ChannelListener<Object> listener;
    final LimitRate limitRate;  // only supported annotation currently
    private final BeanMapper<Object> clientMessageMapper;
    private final Class<?> serverMessageClass;
    private final BeanMapper<Object> serverMessageMapper;

    @SuppressWarnings("unchecked")
    public ChannelHandler(BeanMapper<?> clientMessageMapper, Class<?> serverMessageClass, BeanMapper<?> serverMessageMapper, ChannelListener<?> listener) {
        this.clientMessageMapper = (BeanMapper<Object>) clientMessageMapper;
        this.serverMessageClass = serverMessageClass;
        this.serverMessageMapper = (BeanMapper<Object>) serverMessageMapper;
        this.listener = (ChannelListener<Object>) listener;
        try {
            limitRate = listener.getClass().getDeclaredMethod("onMessage", Channel.class, Object.class).getDeclaredAnnotation(LimitRate.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    String toServerMessage(Object message) {
        if (message == null) throw new Error("message must not be null");
        if (!serverMessageClass.equals(message.getClass())) {
            throw new Error(Strings.format("message class does not match, expected={}, actual={}", serverMessageClass.getCanonicalName(), message.getClass().getCanonicalName()));
        }
        if (this.serverMessageMapper == null) return (String) message;
        return new String(this.serverMessageMapper.toJSON(message), UTF_8);
    }

    Object fromClientMessage(String message) {
        try {
            if (clientMessageMapper == null) return message;
            return clientMessageMapper.fromJSON(Strings.bytes(message));
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e.errorCode(), e);
        } catch (IOException e) {  // for invalid json string
            throw new BadRequestException(e.getMessage(), "INVALID_WS_MESSAGE", e);
        }
    }
}
