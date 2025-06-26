package core.log.domain;

import core.framework.log.message.EventMessage;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;

import static org.apache.avro.SchemaBuilder.map;

public class EventSchema {
    public final Schema schema;

    public EventSchema() {
        schema = SchemaBuilder.record("event")
            .fields()
            .requiredString("id")
            .name("date").type(LogicalTypes.timestampNanos().addToSchema(Schema.create(Schema.Type.LONG))).noDefault()
            .requiredString("app")
            .name("received_time").type(LogicalTypes.timestampNanos().addToSchema(Schema.create(Schema.Type.LONG))).noDefault()
            .requiredString("result")
            .requiredString("action")
            .optionalString("error_code")
            .optionalString("error_message")
            .requiredLong("elapsed")
            .name("context").type(map().values().stringType()).noDefault()
            .name("stats").type(map().values().doubleType()).noDefault()
            .name("info").type(map().values().stringType()).noDefault()
            .endRecord();
    }

    public GenericData.Record record(EventMessage message) {
        var record = new GenericData.Record(schema);
        record.put("id", message.id);
        record.put("date", message.date);
        record.put("app", message.app);
        record.put("received_time", message.receivedTime);
        record.put("result", message.result);
        record.put("action", message.action);
        record.put("error_code", message.errorCode);
        record.put("error_message", message.errorMessage);
        record.put("elapsed", message.elapsed);
        record.put("context", message.context);
        record.put("stats", message.stats);
        record.put("info", message.info);
        return record;
    }
}
