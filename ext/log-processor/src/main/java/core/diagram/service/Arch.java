package core.diagram.service;

import core.framework.internal.asm.CodeBuilder;
import core.framework.search.SearchResponse;
import core.framework.util.ASCII;
import core.log.domain.ActionDocument;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class Arch {
    private static final List<String> COLOR_PALETTE = List.of(
            "#F94144", "#F3722C", "#F8961E", "#F9844A", "#F9C74F",
            "#90BE6D", "#43AA8B", "#4D908E", "#577590", "#277DA1"
    );
    public final List<APIDependency> apiDependencies = new ArrayList<>();
    public final List<MessageSubscription> messageSubscriptions = new ArrayList<>();
    private final Map<String, String> colors = new HashMap<>();
    private int index = 0;

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
            APIDependency dependency = getOrCreateAPIDependency(service, client);
            dependency.apis.put(ASCII.toUpperCase(method) + " " + uri, count);
        } else if (action.startsWith("topic:")) {
            String topic = action.substring(6);
            MessageSubscription subscription = getOrCreateMessageSubscription(topic);
            long publishedCount = Math.max(subscription.publishers.getOrDefault(client, 0L), count);     // one message can be consumed by multiple consumers, use max one to count messages
            subscription.publishers.put(client, publishedCount);
            subscription.consumers.put(service, count);
        }
    }

    private APIDependency getOrCreateAPIDependency(String service, String client) {
        for (APIDependency dependency : apiDependencies) {
            if (dependency.service.equals(service) && dependency.client.equals(client)) return dependency;
        }
        var dependency = new APIDependency(service, client);
        apiDependencies.add(dependency);
        return dependency;
    }

    private MessageSubscription getOrCreateMessageSubscription(String topic) {
        for (MessageSubscription subscription : messageSubscriptions) {
            if (subscription.topic.equals(topic)) return subscription;
        }
        var subscription = new MessageSubscription(topic);
        messageSubscriptions.add(subscription);
        return subscription;
    }

    public Diagram diagram() {
        var diagram = new Diagram();
        var dot = new CodeBuilder().append("digraph {\n");
        dot.append("rankdir=LR;\n");
        dot.append("node [style=rounded, fontname=arial, fontsize=20];\n");
        dot.append("edge [arrowsize=0.5];\n");
        for (String app : apps()) {
            if (app.startsWith("_direct_")) {
                dot.append("{} [label=\"direct\", shape=point];\n", app);
                continue;
            }
            String color = color(app);
            dot.append("{} [label=\"{}\", shape=circle, width=3, style=filled, color=\"{}\", fillcolor=\"{}\", fontcolor=white];\n", id(app), app, color, color);
        }
        for (MessageSubscription subscription : messageSubscriptions) {
            dot.append("{} [shape=box, label=\"{}\"];\n", id(subscription.topic), subscription.topic);
        }
        for (APIDependency dependency : apiDependencies) {
            String edgeId = "edge_" + (index++);
            dot.append("{} -> {} [id=\"{}\", color=\"{}\", weight=5, penwidth=2];\n", id(dependency.client), id(dependency.service), edgeId, colors.get(dependency.client));
            diagram.notes.add(note(edgeId, tooltip(dependency)));
        }
        for (MessageSubscription subscription : messageSubscriptions) {
            for (Map.Entry<String, Long> entry : subscription.publishers.entrySet()) {
                dot.append("{} -> {} [style=dashed];\n", id(entry.getKey()), id(subscription.topic));
            }
            for (Map.Entry<String, Long> entry : subscription.consumers.entrySet()) {
                dot.append("{} -> {} [style=dashed];\n", id(subscription.topic), id(entry.getKey()));
            }
        }
        dot.append("}\n");

        diagram.dot = dot.build();
        return diagram;
    }

    private String tooltip(APIDependency dependency) {
        var builder = new StringBuilder(512);
        builder.append("<table><caption>").append(dependency.client.startsWith("_direct_") ? "direct" : dependency.client).append(" > ")
                .append(dependency.service)
                .append("</caption>");
        for (Map.Entry<String, Long> entry : dependency.apis.entrySet()) {
            builder.append("<tr><td>")
                    .append(entry.getKey())
                    .append("</td><td>")
                    .append(entry.getValue())
                    .append("</td></tr>");
        }
        return builder.append("</table>").toString();
    }

    private Diagram.Note note(String id, String html) {
        var note = new Diagram.Note();
        note.id = id;
        note.html = html;
        return note;
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
        return name.replace('-', '_');
    }

    private String color(String app) {
        String color = colors.get(app);
        if (color == null) {
            color = COLOR_PALETTE.get(colors.size() % COLOR_PALETTE.size());
            colors.put(app, color);
        }
        return color;
    }

    public static class APIDependency {
        public final Map<String, Long> apis = new HashMap<>();         // name, count
        public final String service;
        public final String client;

        public APIDependency(String service, String client) {
            this.service = service;
            this.client = client;
        }
    }

    public static class MessageSubscription {
        public final Map<String, Long> publishers = new HashMap<>();   // name, count
        public final Map<String, Long> consumers = new HashMap<>();    // name, count
        public final String topic;

        public MessageSubscription(String topic) {
            this.topic = topic;
        }
    }
}
