package core.framework.impl.web.management;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class JobView {
    @Property(name = "name")
    public String name;
    @Property(name = "job_class")
    public String jobClass;
    @Property(name = "frequency")
    public String frequency;
}
