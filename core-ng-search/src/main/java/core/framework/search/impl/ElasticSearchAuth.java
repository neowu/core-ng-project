package core.framework.search.impl;

/**
 * @author charlie
 */
public class ElasticSearchAuth {
    public final String apiKeyId;
    public final String apiSecret;

    public ElasticSearchAuth(String apiKeyId, String apiSecret) {
        this.apiKeyId = apiKeyId;
        this.apiSecret = apiSecret;
    }
}
