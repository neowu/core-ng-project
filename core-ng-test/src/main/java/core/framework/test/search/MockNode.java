package core.framework.test.search;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.Node;

import java.util.Collections;

/**
 * @author neo
 */
class MockNode extends Node {
    MockNode(Settings settings) {
        super(new Environment(settings), Collections.singletonList(ReindexPlugin.class));
    }
}
