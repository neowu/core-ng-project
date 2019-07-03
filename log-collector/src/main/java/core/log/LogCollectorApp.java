package core.log;

import core.framework.http.HTTPMethod;
import core.framework.internal.log.message.EventMessage;
import core.framework.internal.log.message.LogTopics;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.framework.util.Sets;
import core.framework.util.Strings;
import core.log.web.CollectEventRequest;
import core.log.web.CollectEventRequestValidator;
import core.log.web.EventController;

import java.util.Set;

/**
 * @author neo
 */
public class LogCollectorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");
        http().maxForwardedIPs(3);      // loose x-forwarded-for ip config, there are cdn/proxy before system, and in event collector, preventing fake client ip is less important

        site().security();
        site().staticContent("/robots.txt");

        kafka().publish(LogTopics.TOPIC_EVENT, EventMessage.class);

        bind(CollectEventRequestValidator.class);

        Set<String> allowedOrigins = allowedOrigins(requiredProperty("app.allowedOrigins"));
        EventController controller = bind(new EventController(allowedOrigins));
        http().route(HTTPMethod.OPTIONS, "/event/:app", controller::options);
        http().route(HTTPMethod.PUT, "/event/:app", controller::put);
        http().bean(CollectEventRequest.class);
    }

    Set<String> allowedOrigins(String value) {
        String[] origins = Strings.split(value, ',');
        Set<String> result = Sets.newHashSetWithExpectedSize(origins.length);
        for (String origin : origins) {
            result.add(origin.strip());
        }
        return result;
    }
}
