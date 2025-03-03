package core.log.domain;

import core.framework.log.message.EventMessage;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;

public class EventSchema {
    public final Schema schema;

    public EventSchema() {
        schema = SchemaBuilder.record("event")
            .fields()
            .requiredString("id")
            .name("date").type().optional().type(LogicalTypes.timestampMicros().addToSchema(Schema.create(Schema.Type.LONG)))
            .requiredString("app")
            .name("received_time").type().optional().type(LogicalTypes.timestampMicros().addToSchema(Schema.create(Schema.Type.LONG)))
            .requiredString("result")
            .requiredString("action")
            .optionalString("error_code")
            .optionalString("error_message")
            .requiredLong("elapsed")
            .name("context").type().optional().type(SchemaBuilder.map().values().stringType())
            .name("stats").type().optional().map().values().doubleType()
            .name("info").type().optional().type(SchemaBuilder.map().values().stringType())
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
