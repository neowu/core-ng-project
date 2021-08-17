package core.diagram.service;

import core.framework.internal.asm.CodeBuilder;
import core.log.domain.ActionDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class ActionDiagram {
    private final Map<Key, List<ActionDocument>> actions = new HashMap<>();
    private int colorIndex;

    void add(ActionDocument action) {
        List<ActionDocument> actions = this.actions.computeIfAbsent(new Key(action.app, action.action), key -> new ArrayList<>());
        actions.add(action);
    }

    public String dot() {
        var dot = new CodeBuilder().append("digraph {\n");
        dot.append("rankdir=LR;\n");
        dot.append("node [style=\"rounded, filled\", fontname=arial, fontsize=12, fontcolor=black];\n");
        dot.append("edge [arrowsize=0.5];\n");
        for (String app : apps()) {
            if (app.startsWith("_direct_")) {
                dot.append("\"{}\" [label=direct, shape=point];\n", app);
            } else {
                dot.append("\"{}\" [label=\"{}\", shape=circle];\n", app, app);
            }
        }
        messagePublishes().forEach((topic, clients) -> {
            dot.append("\"{}\" [label=\"{}\", shape=box];\n", topic, topic);
            for (String client : clients) {
                dot.append("\"{}\" -> \"{}\" [style=dashed];\n", client, topic);
            }
        });

        for (Map.Entry<Key, List<ActionDocument>> entry : actions.entrySet()) {
            Key key = entry.getKey();
            String app = key.app;
            String action = key.action;

            String color = color();
            String tooltip = tooltip(action, app, entry.getValue());
            dot.append("\"{}\" [label=\"{}\", shape=box, color=\"{}\", fillcolor=\"{}\", tooltip=\"{}\"];\n", key.id(), key.action, color, color, tooltip);

            if (action.startsWith("app:") || action.startsWith("task:") || action.contains(":task:") || action.startsWith("job:")) {
                dot.append("\"{}\" -> \"{}\" [arrowhead=none];\n", app, key.id());
            } else if (action.startsWith("api:")) {
                Set<String> clients = entry.getValue().stream().map(this::clients).flatMap(Set::stream).collect(Collectors.toSet());
                for (String client : clients) {
                    dot.append("\"{}\" -> \"{}\";\n", client, key.id());
                }
                dot.append("\"{}\" -> \"{}\";\n", key.id(), app);
            } else if (action.startsWith("topic:")) {
                String topic = action.substring(6);
                dot.append("\"{}\" -> \"{}\" [style=dashed];\n", topic, key.id());
                dot.append("\"{}\" -> \"{}\" [style=dashed];\n", key.id(), app);
            }
        }
        dot.append("}\n");
        return dot.build();
    }

    private Set<String> apps() {
        Set<String> apps = new HashSet<>();
        for (Map.Entry<Key, List<ActionDocument>> entry : actions.entrySet()) {
            Key key = entry.getKey();
            apps.add(key.app);
            if ((key.action.startsWith("api:") || key.action.startsWith("topic:")) && !key.action.contains(":task:")) {
                for (ActionDocument action : entry.getValue()) {
                    apps.addAll(clients(action));
                }
            }
        }
        return apps;
    }

    private Set<String> clients(ActionDocument action) {
        if (action.clients == null) return Set.of("_direct_" + action.app);
        return Set.copyOf(action.clients);
    }

    private Map<String, Set<String>> messagePublishes() {   // topic -> clients
        Map<String, Set<String>> publishes = new HashMap<>();
        actions.forEach((key, value) -> {
            if (key.action.startsWith("topic:") && !key.action.contains(":task:")) {
                String topic = key.action.substring(6);
                Set<String> clients = publishes.computeIfAbsent(topic, k -> new HashSet<>());
                for (ActionDocument action : value) {
                    if (action.clients == null) {
                        clients.add("_direct_" + key.app);
                    } else {
                        clients.addAll(action.clients);
                    }
                }
            }
        });
        return publishes;
    }

    String tooltip(String action, String app, List<ActionDocument> actions) {
        var builder = new CodeBuilder();
        builder.append("<table>\n")
            .append("<caption>{}</caption>\n", action)
            .append("<tr><td>app</td><td>{}</td></tr>\n", app);

        buildActionInfo(builder, actions);

        builder.append("<tr><td colspan=2 class=section>action id</td></tr>\n");
        for (ActionDocument doc : actions) {
            if ("WARN".equals(doc.result) || "ERROR".equals(doc.result)) {
                String color = "WARN".equals(doc.result) ? "OrangeRed" : "Red";
                builder.append("<tr style='color:{}'><td>{}</td><td>{}</td></tr>\n", color, doc.id, doc.errorCode);
            } else {
                builder.append("<tr><td colspan=2>{}</td></tr>\n", doc.id);
            }
        }

        builder.append("</table>");
        return builder.build();
    }

    private void buildActionInfo(CodeBuilder builder, List<ActionDocument> actions) {
        ActionDocument firstAction = actions.get(0);
        List<String> controller = firstAction.context.get("controller");
        if (controller != null) {
            builder.append("<tr><td>controller</td><td>{}</td></tr>\n", controller.get(0));
        }
        List<String> jobClass = firstAction.context.get("job_class");
        if (jobClass != null) {
            builder.append("<tr><td>job class</td><td>{}</td></tr>\n", jobClass.get(0));
        }
        List<String> handler = firstAction.context.get("handler");
        if (handler != null) {
            builder.append("<tr><td>handler</td><td>{}</td></tr>\n", handler.get(0));
        }
    }

    private String color() {
        return Colors.COLOR_PALETTE[colorIndex++ % Colors.COLOR_PALETTE.length];
    }

    static class Key {
        final String app;
        final String action;

        Key(String app, String action) {
            this.app = app;
            this.action = action;
        }

        String id() {
            return app + "_" + action;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Key key = (Key) object;
            return Objects.equals(app, key.app) && Objects.equals(action, key.action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(app, action);
        }
    }
}
