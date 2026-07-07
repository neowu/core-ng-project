package core.framework.internal.log;

/**
 * @author neo
 */
public enum Trace {
    CURRENT,       // only trace current action
    CASCADE,    // trace all correlated actions
    NONE;

    // the value come from external client like browser / script, to be compatible with lower/upper cases, true/false values
    public static Trace parse(String value) {
        if ("false".equals(value) || "none".equals(value)) return NONE;
        if ("cascade".equals(value)) return CASCADE;
        return CURRENT;
    }
}
