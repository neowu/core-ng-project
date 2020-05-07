package app.monitor.kube;

import core.framework.json.JSON;

import java.util.List;

/**
 * @author neo
 */
public class PodListResponse {
    public final String body;

    public PodListResponse(String body) {
        this.body = body;
    }

    public List<PodList.Pod> pods() {
        // not using validation to reduce overhead
        return JSON.fromJSON(PodList.class, body).items;
    }
}
