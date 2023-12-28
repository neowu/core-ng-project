package core.framework.search.impl;

import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.mapper.extras.MapperExtrasPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.LocalPluginsService;
import org.elasticsearch.reindex.ReindexPlugin;
import org.elasticsearch.transport.netty4.Netty4Plugin;

import java.util.List;

/**
 * @author neo
 */
class LocalNode extends Node {
    LocalNode(Settings settings) {
        super(new Environment(settings, null),
            key -> new LocalPluginsService(settings,
                List.of(Netty4Plugin.class,           // for http transport
                    MapperExtrasPlugin.class,         // for scaled_float type
                    PainlessPlugin.class,
                    CommonAnalysisPlugin.class,       // for stemmer analysis
                    ReindexPlugin.class)),            // for delete by query
            true);
    }
}
