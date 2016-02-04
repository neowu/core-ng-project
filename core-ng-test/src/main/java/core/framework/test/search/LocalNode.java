package core.framework.test.search;

import core.framework.api.util.Lists;
import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.script.groovy.GroovyPlugin;

import java.nio.file.Path;

/**
 * @author neo
 */
public class LocalNode extends Node {
    private static Settings settings(Path dataPath) {
        return Settings.settingsBuilder()
            .put("node.local", "true")          // refer to org.elasticsearch.node.NodeBuilder.local()
            .put("http.enabled", "false")       // refer to org.elasticsearch.node.Node.start()
            .put("script.inline", "on")         // refer to https://www.elastic.co/guide/en/elasticsearch/reference/2.2/breaking_20_setting_changes.html#migration-script-settings
            .put("path.home", dataPath)         // refer to org.elasticsearch.env.Environment.Environment()
            .build();
    }

    public LocalNode(Path dataPath) {
        super(InternalSettingsPreparer.prepareEnvironment(settings(dataPath), null), Version.CURRENT, Lists.newArrayList(GroovyPlugin.class));
    }
}
