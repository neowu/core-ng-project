package core.framework.test.search;

import core.framework.api.util.Lists;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
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
        addPainlessPlugin(plugins);
        return plugins;
    }

    private static void addPainlessPlugin(List<Class<? extends Plugin>> plugins) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Plugin> painlessPluginClass = (Class<? extends Plugin>) Class.forName("org.elasticsearch.painless.PainlessPlugin");
            plugins.add(painlessPluginClass);
        } catch (ClassNotFoundException e) {
            LOGGER.info("not found PainlessPlugin, skipped");
        }
    }

    MockNode(Settings settings) {
        super(new Environment(settings), plugins());
    }
}
