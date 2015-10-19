package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.JSON;
import core.framework.api.util.Lists;
import core.framework.impl.log.queue.TraceLogMessage;
import core.log.domain.TraceLogDocument;
import org.elasticsearch.action.update.UpdateRequest;

import javax.inject.Inject;

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

        UpdateRequest request = new UpdateRequest()
            .script(script.toString())
            .addScriptParam("line", message.content)
            .upsert(JSON.toJSON(emptyTraceLog));
        request.scriptedUpsert(true);

        traceType.update(message.id, request);
    }
}
