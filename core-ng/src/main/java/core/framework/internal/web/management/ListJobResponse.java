package core.framework.internal.web.management;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author neo
 */
public class ListJobResponse {
    @Property(name = "jobs")
    public List<JobView> jobs;

    public static class JobView {
        @Property(name = "name")
        public String name;
        @Property(name = "job_class")
        public String jobClass;
        @Property(name = "trigger")
        public String trigger;
    }
}
