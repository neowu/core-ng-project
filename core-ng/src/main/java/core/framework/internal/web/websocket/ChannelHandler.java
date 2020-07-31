package core.framework.internal.web.websocket;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.validate.Validator;
import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;
import core.framework.web.rate.LimitRate;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;

import java.io.IOException;

/**
 * @author neo
 */
public class ChannelHandler<T, V> {
    final ChannelListener<T, V> listener;
    final LimitRate limitRate;  // only supported annotation currently

    private final JSONReader<T> clientMessageReader;
    private final Validator<T> clientMessageValidator;

    private final Class<V> serverMessageClass;
    private final JSONWriter<V> serverMessageWriter;
    private final Validator<V> serverMessageValidator;

    public ChannelHandler(Class<T> clientMessageClass, Class<V> serverMessageClass, ChannelListener<T, V> listener) {
        clientMessageReader = JSONMapper.reader(clientMessageClass);
        clientMessageValidator = Validator.of(clientMessageClass);

        this.serverMessageClass = serverMessageClass;
        serverMessageWriter = JSONMapper.writer(serverMessageClass);
        serverMessageValidator = Validator.of(serverMessageClass);

        this.listener = listener;
        try {
            limitRate = listener.getClass().getDeclaredMethod("onMessage", Channel.class, Object.class).getDeclaredAnnotation(LimitRate.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    String toServerMessage(V message) {
        if (message == null) throw new Error("message must not be null");
        if (!serverMessageClass.equals(message.getClass())) {
            throw new Error(Strings.format("message class does not match, expected={}, actual={}", serverMessageClass.getCanonicalName(), message.getClass().getCanonicalName()));
        }
        serverMessageValidator.validate(message, false);
        return serverMessageWriter.toJSONString(message);
    }

    T fromClientMessage(String message) {
        try {
            T clientMessage = clientMessageReader.fromJSON(message);
            clientMessageValidator.validate(clientMessage, false);
            return clientMessage;
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e.errorCode(), e);
        } catch (IOException e) {  // for invalid json string
            throw new BadRequestException(e.getMessage(), "INVALID_WS_MESSAGE", e);
        }
    }
}
