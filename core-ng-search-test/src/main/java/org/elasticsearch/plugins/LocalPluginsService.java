package org.elasticsearch.plugins;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author neo
 */
public class LocalPluginsService extends PluginsService {
    private final List<LoadedPlugin> plugins;

    // refer to https://github.com/elastic/elasticsearch/blob/main/test/framework/src/main/java/org/elasticsearch/plugins/MockPluginsService.java
    // refer to https://github.com/elastic/elasticsearch/blob/main/test/framework/src/main/java/org/elasticsearch/node/MockNode.java
    // refer to https://github.com/elastic/elasticsearch/blob/main/test/framework/src/main/java/org/elasticsearch/test/ESSingleNodeTestCase.java
    public LocalPluginsService(Settings settings, Collection<Class<? extends Plugin>> pluginClasses) {
        super(settings, null, null, null);
        plugins = new ArrayList<>(pluginClasses.size());
        for (Class<? extends Plugin> pluginClass : pluginClasses) {
            Plugin plugin = loadPlugin(pluginClass, settings, null);
            var descriptor = new PluginDescriptor(
                pluginClass.getName(),
                "classpath plugin",
                "NA",
                Version.CURRENT,
                "17",
                pluginClass.getName(),
                null,
                List.of(),
                false,
                PluginType.ISOLATED,
                null,
                false);
            plugins.add(new LoadedPlugin(descriptor, plugin, Thread.currentThread().getContextClassLoader(), ModuleLayer.boot()));
        }
    }

    @Override
    protected final List<LoadedPlugin> plugins() {
        return this.plugins;
    }
}
