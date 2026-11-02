package core.framework.internal.json;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * reject string which can not be stored safely, json escape allows any utf-16 code unit, e.g. "\u0000" or "\uD800"
 * NUL is rejected by postgresql and truncates c string, lone surrogate can not be encoded to utf-8 (String.getBytes(UTF_8) silently replaces it with '?')
 *
 * @author neo
 */
final class StrictStringDeserializer extends StringDeserializer {
    // return error message if value contains invalid char, to keep check cheap, only scan once
    @Nullable
    static String validate(String value) {
        int length = value.length();
        int i = 0;
        while (i < length) {
            char ch = value.charAt(i);
            if (ch == 0) return "string must not contain NUL char, index=" + i;
            if (Character.isSurrogate(ch)) {
                // valid surrogate pair must be high followed by low
                if (Character.isLowSurrogate(ch) || i + 1 >= length || !Character.isLowSurrogate(value.charAt(i + 1)))
                    return "string must not contain lone surrogate, index=" + i;
                i += 2;
            } else {
                i++;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) {
        String value = super.deserialize(parser, context);
        if (value != null) {
            String error = validate(value);
            if (error != null) context.reportInputMismatch(this, error);
        }
        return value;
    }

    static final class StrictStringKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext context) {
            String error = validate(key);
            if (error != null) throw context.weirdKeyException(String.class, key, error);
            return key;
        }
    }
}
