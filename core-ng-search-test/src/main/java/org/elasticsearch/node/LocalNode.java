package org.elasticsearch.node;

import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.mapper.extras.MapperExtrasPlugin;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.LocalPluginsService;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.reindex.ReindexPlugin;
import org.elasticsearch.transport.netty4.Netty4Plugin;

import java.util.List;

/**
 * @author neo
 */
public class LocalNode extends Node {
    public LocalNode(Settings settings) {
        super(NodeConstruction.prepareConstruction(new Environment(settings, null), new NodeServiceProvider() {
            @Override
            PluginsService newPluginService(Environment environment, Settings settings) {
                return new LocalPluginsService(settings,
                    List.of(Netty4Plugin.class,           // for http transport
                        MapperExtrasPlugin.class,         // for scaled_float type
                        PainlessPlugin.class,
                        CommonAnalysisPlugin.class,       // for stemmer analysis
                        ReindexPlugin.class));            // for delete_by_query
            }
        }, true));
    }
}
