package core.log.service;

import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.ClasspathResources;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class KibanaService {
    private final Logger logger = LoggerFactory.getLogger(KibanaService.class);

    private final HTTPClient client;
    private final String kibanaURL;
    private final String apiKey;
    private final String banner;

    public KibanaService(String kibanaURL, String apiKey, String banner, HTTPClient client) {
        this.kibanaURL = kibanaURL;
        this.apiKey = apiKey;
        this.banner = banner;
        this.client = client;
    }

    public void importObjects() {
        var watch = new StopWatch();
        try {
            logger.info("import kibana objects, banner={}", banner);
            String objects = ClasspathResources.text("kibana.json").replace("${NOTIFICATION_BANNER}", banner);
            var request = new HTTPRequest(HTTPMethod.POST, kibanaURL + "/api/saved_objects/_bulk_create?overwrite=true");
            request.headers.put("kbn-xsrf", "true");
            if (apiKey != null) {
                request.headers.put("Authorization", "ApiKey " + apiKey);
            }
            request.body(objects, ContentType.APPLICATION_JSON);
            HTTPResponse response = client.execute(request);
            if (response.statusCode == 200) {
                logger.info("kibana objects are imported, elapsed={}", watch.elapsed());
            } else {
                logger.warn("failed to import kibana objects, status={}, responseText={}", response.statusCode, response.text());
            }
        } catch (Throwable e) {
            logger.error("failed to connect to kibana", e);
        }
    }
}
