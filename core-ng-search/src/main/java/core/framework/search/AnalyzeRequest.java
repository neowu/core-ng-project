package core.framework.search;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class AnalyzeRequest {
    @Nullable
    public String index;
    public String analyzer;
    public String text;
}
