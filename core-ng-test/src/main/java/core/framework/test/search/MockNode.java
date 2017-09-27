package core.framework.test.search;

import core.framework.api.util.Lists;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.painless.PainlessPlugin;

/**
 * @author neo
 */
class MockNode extends Node {
    MockNode(Settings settings) {
        super(new Environment(settings), Lists.newArrayList(ReindexPlugin.class, PainlessPlugin.class));
    }
}
