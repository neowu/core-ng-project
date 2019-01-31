package core.framework.search.impl;

import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.mapper.MapperExtrasPlugin;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import java.util.List;

/**
 * @author neo
 */
class LocalNode extends Node {
    private static List<Class<? extends Plugin>> plugins() {
        return List.of(ReindexPlugin.class,
                Netty4Plugin.class,
                MapperExtrasPlugin.class,  // for scaled_float type
                PainlessPlugin.class,
                CommonAnalysisPlugin.class);  // for stemmer analysis
    }

    LocalNode(Settings settings) {
        super(new Environment(settings, null), plugins(), false);
    }

    @Override
    protected void registerDerivedNodeNameWithLogger(String nodeName) {
    }
}
