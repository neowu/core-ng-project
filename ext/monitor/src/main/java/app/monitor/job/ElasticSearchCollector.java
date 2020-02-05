package app.monitor.job;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.stat.Stats;
import core.framework.json.JSON;
import core.framework.util.Strings;
import core.framework.util.Types;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ElasticSearchCollector implements Collector {
    private final HTTPClient httpClient;
    private final String host;

    public double highCPUUsageThreshold;
    public double highMemUsageThreshold;
    public double highHeapUsageThreshold;
    public double highDiskUsageThreshold;

    public ElasticSearchCollector(HTTPClient httpClient, String host) {
        this.httpClient = httpClient;
        this.host = host;
    }

    @Override
    public Stats collect() {
        var stats = new Stats();
        // refer to org.elasticsearch.rest.action.cat.RestNodesAction for all available fields
        Map<String, String> values = cat("nodes", "cpu", "disk.used", "disk.total", "heap.current", "heap.max", "ram.current", "ram.max");
        collectCPU(stats, values);
        collectMem(stats, values);
        collectHeap(stats, values);
        collectDisk(stats, values);

        values = cat("indices", "docs.count");
        stats.put("es_docs", get(values, "docs.count"));

        return stats;
    }

    private void collectCPU(Stats stats, Map<String, String> values) {
        double cpuUsage = get(values, "cpu") / 100d;
        stats.put("es_cpu_usage", cpuUsage);
        stats.checkHighUsage(cpuUsage, highCPUUsageThreshold, "cpu");
    }

    private void collectHeap(Stats stats, Map<String, String> values) {
        double heapUsed = get(values, "heap.current");
        stats.put("es_heap_used", heapUsed);
        double heapMax = get(values, "heap.max");
        stats.put("es_heap_max", heapMax);
        stats.checkHighUsage(heapUsed / heapMax, highHeapUsageThreshold, "heap");
    }

    private void collectDisk(Stats stats, Map<String, String> values) {
        double diskUsed = get(values, "disk.used");
        stats.put("es_disk_used", diskUsed);
        double diskMax = get(values, "disk.total");
        stats.put("es_disk_max", diskMax);
        stats.checkHighUsage(diskUsed / diskMax, highDiskUsageThreshold, "disk");
    }

    private void collectMem(Stats stats, Map<String, String> values) {
        double memUsed = get(values, "ram.current");
        stats.put("es_mem_used", memUsed);
        double memMax = get(values, "ram.max");
        stats.put("es_mem_max", memMax);
        stats.checkHighUsage(memUsed / memMax, highMemUsageThreshold, "mem");
    }

    private double get(Map<String, String> values, String field) {
        String value = values.get(field);
        if (value == null) throw new Error("can not find field, field=" + field);
        return Double.parseDouble(value);
    }

    private Map<String, String> cat(String command, String... fields) {
        var request = new HTTPRequest(HTTPMethod.GET, "http://" + host + ":9200/_cat/" + command);
        request.params.put("bytes", "b");
        request.params.put("format", "json");
        request.params.put("h", String.join(",", fields));
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.OK.code)
            throw new Error(Strings.format("failed to call es cat api, uri={}, status={}", request.requestURI(), response.statusCode));
        @SuppressWarnings("unchecked")
        List<Map<String, String>> results = (List<Map<String, String>>) JSON.fromJSON(Types.list(Types.map(String.class, String.class)), response.text());
        return results.get(0);
    }
}
