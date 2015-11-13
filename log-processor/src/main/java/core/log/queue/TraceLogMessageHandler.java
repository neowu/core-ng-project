package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.JSON;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.impl.log.queue.TraceLogMessage;
import core.log.domain.TraceLogDocument;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author neo
 */
public class TraceLogMessageHandler implements MessageHandler<TraceLogMessage> {
    @Inject
    ElasticSearchType<TraceLogDocument> traceType;

    @Override
    public void handle(TraceLogMessage message) throws Exception {
        TraceLogDocument emptyTraceLog = new TraceLogDocument();
        emptyTraceLog.date = message.date;
        emptyTraceLog.id = message.id;
        emptyTraceLog.app = message.app;
        emptyTraceLog.action = message.action;
        emptyTraceLog.result = message.result;
        emptyTraceLog.content = Lists.newArrayList();

        StringBuilder script = new StringBuilder("ctx._source.content+=line");
        if ("ERROR".equals(message.result))
            script.append("; ctx._source.result=\"ERROR\"");    // if log doc is created by WARN, ERROR should update result

        Map<String, String> params = Maps.newHashMap("line", message.content);

        UpdateRequest request = new UpdateRequest()
            .script(new Script(script.toString(), ScriptService.ScriptType.INLINE, null, params))
            .upsert(JSON.toJSON(emptyTraceLog))
            .scriptedUpsert(true);

        traceType.update(message.id, request);
    }
}
