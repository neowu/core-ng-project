package core.diagram.service;

import core.framework.internal.asm.CodeBuilder;
import core.framework.search.SearchResponse;
import core.framework.util.ASCII;
import core.log.domain.ActionDocument;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author neo
 */
public class ArchDiagram {
    private static final List<String> COLOR_PALETTE = List.of(
        "#F94144", "#F3722C", "#F8961E", "#F9844A", "#F9C74F",
        "#90BE6D", "#43AA8B", "#4D908E", "#577590", "#277DA1"
    );
    // grey palette: "#E9ECEF", "#DEE2E6", "#CED4DA", "#ADB5BD", "#6C757D", "#495057"

    private final List<APIDependency> apiDependencies = new ArrayList<>();
    private final List<MessageSubscription> messageSubscriptions = new ArrayList<>();
    private final Map<String, Scheduler> schedulers = new HashMap<>();
    private final Map<String, String> colors = new HashMap<>();
    private final Set<String> excludeApps;

    public ArchDiagram(Set<String> excludeApps) {
        this.excludeApps = excludeApps;
    }

    public void load(SearchResponse<ActionDocument> response) {
        List<? extends Terms.Bucket> apps = ((ParsedTerms) response.aggregations.get("app")).getBuckets();
        for (Terms.Bucket appBucket : apps) {
            load(appBucket.getKeyAsString(), ((ParsedTerms) appBucket.getAggregations().get("action")).getBuckets());
        }
    }

    private void load(String app, List<? extends Terms.Bucket> actions) {
        for (Terms.Bucket actionBucket : actions) {
            String action = actionBucket.getKeyAsString();
            long totalCount = actionBucket.getDocCount();
            List<? extends Terms.Bucket> clients = ((ParsedTerms) actionBucket.getAggregations().get("client")).getBuckets();
            for (Terms.Bucket clientBucket : clients) {
                String client = clientBucket.getKeyAsString();
                long count = clientBucket.getDocCount();
                loadAction(app, action, client, count);
            }
            final long totalByClient = clients.stream().mapToLong(MultiBucketsAggregation.Bucket::getDocCount).sum();
            if (totalCount > totalByClient) {
                loadAction(app, action, "_direct_" + id(app), totalCount - totalByClient);
            }
        }
    }

    private void loadAction(String service, String action, String client, long count) {
        // ignore irrelevant actions
        if (action.startsWith("app:") || action.startsWith("task:") || action.contains(":task:")) return;

        if (action.startsWith("api:")) {
            int index = action.indexOf(':', 4);
            String method = action.substring(4, index);
            String uri = action.substring(index + 1);
            APIDependency dependency = apiDependency(service, client);
            dependency.calls.add(new APICall(ASCII.toUpperCase(method), uri, count));
        } else if (action.startsWith("topic:")) {
            String topic = action.substring(6);
            MessageSubscription subscription = messageSubscription(topic);
            long publishedCount = Math.max(subscription.publishers.getOrDefault(client, 0L), count);     // one message can be consumed by multiple consumers, use max one to count messages
            subscription.publishers.put(client, publishedCount);
            subscription.consumers.put(service, count);
        } else if (action.startsWith("job:")) {
            String job = action.substring(4);
            Scheduler scheduler = schedulers.computeIfAbsent(service, key -> new Scheduler());
            scheduler.jobs.put(job, count);
        }
    }

    public String dot() {
        var dot = new CodeBuilder().append("digraph {\n");
        dot.append("rankdir=LR;\n");
        dot.append("node [style=\"rounded, filled\", fontname=arial, fontsize=17, fontcolor=white];\n");
        dot.append("edge [arrowsize=0.5];\n");
        for (String app : apps()) {
            if (excludeApps.contains(app)) continue;
            if (app.startsWith("_direct_")) {
                dot.append("{} [label=\"direct\", shape=point];\n", app);
                continue;
            }
            String color = color(app);
            String tooltip = tooltip(app);
            dot.append("{} [label=\"{}\", shape=circle, width=3, color=\"{}\", fillcolor=\"{}\", tooltip=\"{}\"];\n", id(app), app, color, color, tooltip);
        }
        for (MessageSubscription subscription : messageSubscriptions) {
            dot.append("{} [label=\"{}\", shape=box, color=\"#6C757D\", fillcolor=\"#6C757D\", tooltip=\"{}\"];\n", id(subscription.topic), subscription.topic, tooltip(subscription));
        }
        for (APIDependency dependency : apiDependencies) {
            if (!excludeApps.contains(dependency.client) && !excludeApps.contains(dependency.service)) {
                String tooltip = tooltip(dependency);
                dot.append("{} -> {} [color=\"{}\", weight=5, penwidth=2, tooltip=\"{}\"];\n", id(dependency.client), id(dependency.service), colors.get(dependency.client), tooltip);
            }

        }
        for (MessageSubscription subscription : messageSubscriptions) {
            for (Map.Entry<String, Long> entry : subscription.publishers.entrySet()) {
                String publisher = entry.getKey();
                if (!excludeApps.contains(publisher)) {
                    dot.append("{} -> {} [color=\"#495057\", style=dashed];\n", id(publisher), id(subscription.topic));
                }
            }
            for (Map.Entry<String, Long> entry : subscription.consumers.entrySet()) {
                String consumer = entry.getKey();
                if (!excludeApps.contains(consumer)) {
                    dot.append("{} -> {} [color=\"#ADB5BD\", style=dashed];\n", id(subscription.topic), id(consumer));
                }
            }
        }
        dot.append("}\n");
        return dot.build();
    }

    private Map<String, Long> messagePublished(String app) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (MessageSubscription subscription : messageSubscriptions) {
            Long published = subscription.publishers.get(app);
            if (published != null) result.put(subscription.topic, published);
        }
        return result;
    }

    private Map<String, Long> messageConsumed(String app) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (MessageSubscription subscription : messageSubscriptions) {
            Long published = subscription.consumers.get(app);
            if (published != null) result.put(subscription.topic, published);
        }
        return result;
    }

    private Map<String, Long> apiCalls(String app) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (APIDependency dependency : apiDependencies) {
            if (dependency.client.equals(app)) {
                result.put(dependency.service, dependency.calls.stream().mapToLong(value -> value.count).sum());
            }
        }
        return result;
    }

    private String tooltip(MessageSubscription subscription) {
        var builder = new StringBuilder(512);
        builder.append("<table>\n<caption>").append(subscription.topic).append("</caption>\n<tr><td colspan=2 class=section>publishers</td><tr>\n");
        for (Map.Entry<String, Long> entry : subscription.publishers.entrySet()) {
            builder.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
        }
        builder.append("<tr><td colspan=2 class=section>consumers</td><tr>\n");
        for (Map.Entry<String, Long> entry : subscription.consumers.entrySet()) {
            builder.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
        }
        return builder.append("</table>").toString();
    }

    private String tooltip(String app) {
        var builder = new StringBuilder(512);
        builder.append("<table>\n<caption>").append(app).append("</caption>\n");
        Map<String, Long> calls = apiCalls(app);
        if (!calls.isEmpty()) {
            builder.append("<tr><td colspan=2 class=section>api calls</td><tr>\n");
            for (Map.Entry<String, Long> entry : calls.entrySet()) {
                builder.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
            }
        }
        Scheduler scheduler = schedulers.get(app);
        if (scheduler != null) {
            builder.append("<tr><td colspan=2 class=section>jobs</td><tr>\n");
            for (Map.Entry<String, Long> entry : scheduler.jobs.entrySet()) {
                builder.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
            }
        }
        Map<String, Long> published = messagePublished(app);
        if (!published.isEmpty()) {
            builder.append("<tr><td colspan=2 class=section>published messages</td><tr>\n");
            for (Map.Entry<String, Long> entry : published.entrySet()) {
                builder.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
            }
        }
        Map<String, Long> consumed = messageConsumed(app);
        if (!consumed.isEmpty()) {
            builder.append("<tr><td colspan=2 class=section>consumed messages</td><tr>\n");
            for (Map.Entry<String, Long> entry : consumed.entrySet()) {
                builder.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
            }
        }
        return builder.append("</table>").toString();
    }

    private String tooltip(APIDependency dependency) {
        dependency.calls.sort(Comparator.comparing(call -> call.uri));
        var builder = new StringBuilder(512);
        builder.append("<table>\n<caption>").append(dependency.client.startsWith("_direct_") ? "direct" : dependency.client)
            .append(" > ")
            .append(dependency.service).append("</caption>\n<tr><td colspan=3 class=section>api calls</td><tr>\n");
        for (APICall call : dependency.calls) {
            builder.append("<tr><td>").append(call.method).append("</td><td>").append(call.uri).append("</td><td>").append(call.count).append("</td></tr>\n");
        }
        return builder.append("</table>").toString();
    }

    private Set<String> apps() {
        Set<String> apps = new LinkedHashSet<>(apiDependencies.size() * 2);
        for (APIDependency dependency : apiDependencies) {
            apps.add(dependency.service);
            apps.add(dependency.client);
        }
        for (MessageSubscription subscription : messageSubscriptions) {
            apps.addAll(subscription.publishers.keySet());
            apps.addAll(subscription.consumers.keySet());
        }
        return apps;
    }

    private String id(String name) {
        return name.replace('-', '_').replace(':', '_');
    }

    private String color(String app) {
        String color = colors.get(app);
        if (color == null) {
            color = COLOR_PALETTE.get(colors.size() % COLOR_PALETTE.size());
            colors.put(app, color);
        }
        return color;
    }

    private APIDependency apiDependency(String service, String client) {
        for (APIDependency dependency : apiDependencies) {
            if (dependency.service.equals(service) && dependency.client.equals(client)) return dependency;
        }
        var dependency = new APIDependency(service, client);
        apiDependencies.add(dependency);
        return dependency;
    }

    private MessageSubscription messageSubscription(String topic) {
        for (MessageSubscription subscription : messageSubscriptions) {
            if (subscription.topic.equals(topic)) return subscription;
        }
        var subscription = new MessageSubscription(topic);
        messageSubscriptions.add(subscription);
        return subscription;
    }

    static class APIDependency {
        final List<APICall> calls = new ArrayList<>();
        final String service;
        final String client;

        APIDependency(String service, String client) {
            this.service = service;
            this.client = client;
        }
    }

    static class APICall {
        final String method;
        final String uri;
        final long count;

        APICall(String method, String uri, long count) {
            this.method = method;
            this.uri = uri;
            this.count = count;
        }
    }

    static class MessageSubscription {
        final Map<String, Long> publishers = new HashMap<>();   // name, count
        final Map<String, Long> consumers = new HashMap<>();    // name, count
        final String topic;

        MessageSubscription(String topic) {
            this.topic = topic;
        }
    }

    static class Scheduler {
        final Map<String, Long> jobs = new TreeMap<>();  // name, count
    }
}
