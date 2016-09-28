package core.framework.impl.kafka;

import core.framework.api.queue.Message;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
public class KafkaMessageSerializerTest {
    @Test
    public void serialize() {
//        KafkaMessageSerializer serializer = new KafkaMessageSerializer(Maps.newHashMap(TestMessage.class, "test_message"));
//        KafkaMessage message = new KafkaMessage();
//        message.headers = Maps.newHashMap("client", "test");
//        TestMessage testMessage = new TestMessage();
//        testMessage.stringField = "value";
//        message.body = testMessage;
//        byte[] data = serializer.serialize(null, message);
//
//        KafkaMessageDeserializer deserializer = new KafkaMessageDeserializer(Maps.newHashMap("test_message", TestMessage.class));
//        KafkaMessage decodedMessage = deserializer.deserialize(null, data);
//        assertEquals("test_message", decodedMessage.headers.get("type"));
//        assertEquals("test", decodedMessage.headers.get("client"));
//        assertEquals("value", ((TestMessage) decodedMessage.body).stringField);
    }

    @Message(name = "test_message")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestMessage {
        @XmlElement(name = "string_field")
        public String stringField;
    }
}