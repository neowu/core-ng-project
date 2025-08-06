package core.framework.internal.web.sse;

import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.Validator;
import org.jspecify.annotations.Nullable;

class ServerSentEventWriter<T> {
    private final JSONWriter<T> writer;
    private final Validator<T> validator;

    ServerSentEventWriter(Class<T> eventClass) {
        writer = new JSONWriter<>(eventClass);
        validator = Validator.of(eventClass);
    }

    String toMessage(@Nullable String id, T event) {
        validator.validate(event, false);
        String data = writer.toJSONString(event);

        return message(id, data);
    }

    String message(@Nullable String id, String data) {
        var builder = new StringBuilder(data.length() + 7 + (id == null ? 0 : id.length() + 4));
        if (id != null) builder.append("id: ").append(id).append('\n');
        builder.append("data: ").append(data).append("\n\n");

        return builder.toString();
    }
}
