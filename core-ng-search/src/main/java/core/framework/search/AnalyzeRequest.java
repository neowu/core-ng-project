package core.framework.search;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public class AnalyzeRequest {
    @Nullable
    public String index;
    public String analyzer;
    public String text;
}
