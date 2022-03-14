package core.framework.internal.log;

import core.framework.util.ASCII;

/**
 * @author neo
 */
public enum Trace {
    CURRENT,       // only trace current action
    CASCADE,    // trace all correlated actions
    NONE;

    // the value come from external client like browser / script, to be compatible with lower/upper cases, true/false values
    public static Trace parse(String value) {
        String valueInLowerCase = ASCII.toLowerCase(value);
        if (valueInLowerCase == null || "false".equals(valueInLowerCase) || "none".equals(valueInLowerCase)) return NONE;
        if ("cascade".equals(valueInLowerCase)) return CASCADE;
        return CURRENT;
    }
}
