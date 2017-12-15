package core.framework.test.search;

import core.framework.util.Lists;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
class MockNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockNode.class);

    private static List<Class<? extends Plugin>> plugins() {
        List<Class<? extends Plugin>> plugins = Lists.newArrayList();
        plugins.add(ReindexPlugin.class);
        plugins.add(Netty4Plugin.class);
        addPlugin(plugins, "org.elasticsearch.index.mapper.MapperExtrasPlugin");
        addPlugin(plugins, "org.elasticsearch.painless.PainlessPlugin");
        return plugins;
    }

    private static void addPlugin(List<Class<? extends Plugin>> plugins, String pluginClass) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Plugin> painlessPluginClass = (Class<? extends Plugin>) Class.forName(pluginClass);
            plugins.add(painlessPluginClass);
        } catch (ClassNotFoundException e) {
            LOGGER.info("not found plugin, skipped, class={}", pluginClass);
        }
    }

    MockNode(Settings settings) {
        super(new Environment(settings, null), plugins());
    }
}
