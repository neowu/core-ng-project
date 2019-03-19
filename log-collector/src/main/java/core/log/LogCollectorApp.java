package core.log;

import core.framework.http.HTTPMethod;
import core.framework.internal.log.message.EventMessage;
import core.framework.internal.log.message.LogTopics;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.log.web.CollectEventRequest;
import core.log.web.EventController;

/**
 * @author neo
 */
public class LogCollectorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        kafka().publish(LogTopics.TOPIC_EVENT, EventMessage.class);

        EventController controller = bind(EventController.class);
        http().route(HTTPMethod.OPTIONS, "/event/:app", controller::options);
        http().route(HTTPMethod.PUT, "/event/:app", controller::put);
        http().bean(CollectEventRequest.class);
    }
}
