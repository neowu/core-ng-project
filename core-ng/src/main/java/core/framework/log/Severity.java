package core.framework.log;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public enum Severity {
    @Property(name = "WARN")
    WARN,

    @Property(name = "ERROR")
    ERROR
}
