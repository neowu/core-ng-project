package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.JSON;
import core.framework.api.util.Lists;
import core.framework.impl.log.queue.TraceLogMessage;
import org.elasticsearch.action.update.UpdateRequest;

import javax.inject.Inject;

/**
 * @author neo
 */
public class TraceLogMessageHandler implements MessageHandler<TraceLogMessage> {
    @Inject
    ElasticSearchType<TraceLogMessage> traceType;

    @Override
    public void handle(TraceLogMessage message) throws Exception {
        TraceLogMessage emptyTraceLog = new TraceLogMessage();
        emptyTraceLog.date = message.date;
        emptyTraceLog.id = message.id;
        emptyTraceLog.app = message.app;
        emptyTraceLog.action = message.action;
        emptyTraceLog.result = message.result;
        emptyTraceLog.content = Lists.newArrayList();

        UpdateRequest request = new UpdateRequest()
            .script("ctx._source.content+=lines")
            .addScriptParam("lines", message.content)
            .upsert(JSON.toJSON(emptyTraceLog));
        request.scriptedUpsert(true);

        traceType.update(message.id, request);
    }
}
