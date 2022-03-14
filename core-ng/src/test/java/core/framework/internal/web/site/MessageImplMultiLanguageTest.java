package core.framework.internal.web.site;

import core.framework.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class MessageImplMultiLanguageTest {
    private MessageImpl message;

    @BeforeEach
    void createMessage() {
        message = new MessageImpl();
        List<String> properties = List.of("message-test/messages.properties", "message-test/messages_en.properties", "message-test/messages_en_US.properties");
        message.load(properties, "en", "en_US", "zh");
    }

    @Test
    void language() {
        assertThat(message.language("messages_en_US.properties")).isEqualTo("en_US");
        assertThat(message.language("messages_en.properties")).isEqualTo("en");

        assertThat(message.language("messages.properties")).isEqualTo(MessageImpl.DEFAULT_LANGUAGE);

        assertThatThrownBy(() -> message.language("invalid.message_E.properties"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("property path must match 'path/name_language.properties' pattern");
    }

    @Test
    void get() {
        assertThat(message.get("key1", "zh")).isEqualTo("value1");
        assertThat(message.get("key1", "en")).isEqualTo("value1");
        assertThat(message.get("key1", "en_US")).isEqualTo("value1");

        assertThat(message.get("key2", "zh")).isEqualTo("value2");
        assertThat(message.get("key2", "en")).isEqualTo("en_value2");
        assertThat(message.get("key2", "en_US")).isEqualTo("en_value2");

        assertThat(message.get("key3", "zh")).isEqualTo("value3");
        assertThat(message.get("key3", "en")).isEqualTo("en_value3");
        assertThat(message.get("key3", "en_US")).isEqualTo("en_US_value3");

        // use first language if language is null
        assertThat(message.get("key1")).isEqualTo("value1");
    }

    @Test
    void getWithNotDefinedLanguage() {
        assertThatThrownBy(() -> message.get("key1", "es_GT"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("language is not defined");
    }

    @Test
    void getWithNotExistedKey() {
        assertThat(message.get("notExistedKey", "zh")).isEqualTo("notExistedKey");
    }

    @Test
    void validateMessageKeys() {
        Properties properties = new Properties();
        properties.set("key4", "value4");
        message.messages.get("zh").add(properties);

        assertThatThrownBy(() -> message.validateMessageKeys())
            .isInstanceOf(Error.class)
            .hasMessageContaining("message keys are missing for language")
            .hasMessageContaining("language=en_US");
    }
}
