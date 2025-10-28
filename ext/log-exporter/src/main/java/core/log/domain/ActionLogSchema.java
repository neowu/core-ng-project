package core.log.domain;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.util.Maps;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;

import java.util.Map;

import static org.apache.avro.SchemaBuilder.array;
import static org.apache.avro.SchemaBuilder.map;

public class ActionLogSchema {
    public final Schema schema;

    public ActionLogSchema() {
        schema = SchemaBuilder.record("action")
            .fields()
            .requiredString("id")
            .name("date").type(LogicalTypes.timestampNanos().addToSchema(Schema.create(Schema.Type.LONG))).noDefault()
            .requiredString("app")
            .requiredString("host")
            .requiredString("result")
            .requiredString("action")
            .name("correlation_ids").type().optional().array().items().stringType()
            .name("client").type().optional().array().items().stringType()
            .name("ref_ids").type().optional().array().items().stringType()
            .optionalString("error_code")
            .optionalString("error_message")
            .requiredLong("elapsed")
            .name("context").type(map().values(array().items().nullable().stringType())).noDefault()
            .name("stats").type(map().values().doubleType()).noDefault()
            .name("perf_stats").type(map().values().longType()).noDefault()
            .endRecord();
    }

    public GenericData.Record record(ActionLogMessage message) {
        var record = new GenericData.Record(schema);
        record.put("id", message.id);
        record.put("date", message.date);
        record.put("app", message.app);
        record.put("host", message.host);
        record.put("result", message.result);
        record.put("action", message.action);
        record.put("correlation_ids", message.correlationIds);
        record.put("client", message.clients);
        record.put("ref_ids", message.refIds);
        record.put("error_code", message.errorCode);
        record.put("error_message", message.errorMessage);
        record.put("elapsed", message.elapsed);
        record.put("context", message.context);
        record.put("stats", message.stats);
        Map<String, Long> perfStats = Maps.newHashMapWithExpectedSize(message.performanceStats.size() * 4);
        for (Map.Entry<String, PerformanceStatMessage> entry : message.performanceStats.entrySet()) {
            String key = entry.getKey();
            PerformanceStatMessage stat = entry.getValue();
            perfStats.put(key + ".count", (long) stat.count);
            perfStats.put(key + ".total_elapsed", stat.totalElapsed);
            if (stat.readEntries != null) perfStats.put(key + ".read_entries", (long) stat.readEntries);
            if (stat.writeEntries != null) perfStats.put(key + ".write_entries", (long) stat.writeEntries);
            if (stat.readBytes != null) perfStats.put(key + ".read_bytes", stat.readBytes);
            if (stat.writeBytes != null) perfStats.put(key + ".write_bytes", stat.writeBytes);
        }
        record.put("perf_stats", perfStats);
        return record;
    }
}
