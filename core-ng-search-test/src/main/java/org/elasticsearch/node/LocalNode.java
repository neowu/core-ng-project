package org.elasticsearch.node;

import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.mapper.extras.MapperExtrasPlugin;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.LocalPluginsService;
import org.elasticsearch.plugins.PluginsLoader;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.reindex.ReindexPlugin;
import org.elasticsearch.transport.netty4.Netty4Plugin;

import java.util.Collections;
import java.util.List;

/**
 * @author neo
 */
public class LocalNode extends Node {
    public LocalNode(Settings settings) {
        super(NodeConstruction.prepareConstruction(new Environment(settings, null), PluginsLoader.createPluginsLoader(Collections.emptySet(), Collections.emptySet(), Collections.emptyMap(), false), new NodeServiceProvider() {
            @Override
            PluginsService newPluginService(Environment initialEnvironment, PluginsLoader pluginsLoader) {
                return new LocalPluginsService(settings, pluginsLoader,
                    List.of(Netty4Plugin.class,           // for http transport
                        MapperExtrasPlugin.class,         // for scaled_float type
                        PainlessPlugin.class,
                        CommonAnalysisPlugin.class,       // for stemmer analysis
                        ReindexPlugin.class));            // for delete_by_query
            }
        }, true));
    }
}
