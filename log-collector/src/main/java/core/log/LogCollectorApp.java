package core.log;

import core.framework.http.HTTPMethod;
import core.framework.internal.log.message.EventMessage;
import core.framework.internal.log.message.LogTopics;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.framework.util.Strings;
import core.log.web.CollectEventRequest;
import core.log.web.CollectEventRequestValidator;
import core.log.web.EventController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public class LogCollectorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");
        http().httpsPort(8444);

        site().security();
        site().staticContent("/robots.txt");

        kafka().publish(LogTopics.TOPIC_EVENT, EventMessage.class);

        bind(CollectEventRequestValidator.class);

        Set<String> allowedOrigins = allowedOrigins();
        EventController controller = bind(new EventController(allowedOrigins));
        http().route(HTTPMethod.OPTIONS, "/event/:app", controller::options);
        http().route(HTTPMethod.PUT, "/event/:app", controller::put);
        http().bean(CollectEventRequest.class);
    }

    private Set<String> allowedOrigins() {
        String[] allowedOrigins = Strings.split(requiredProperty("app.allowedOrigins"), ',');
        Set<String> result = new HashSet<>();
        Collections.addAll(result, allowedOrigins);
        return result;
    }
}
