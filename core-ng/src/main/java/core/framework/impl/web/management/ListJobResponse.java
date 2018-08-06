package core.framework.impl.web.management;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class ListJobResponse {
    @NotNull
    @Property(name = "jobs")
    public List<JobView> jobs = new ArrayList<>();

    public static class JobView {
        @NotNull
        @Property(name = "name")
        public String name;
        @NotNull
        @Property(name = "job_class")
        public String jobClass;
        @NotNull
        @Property(name = "trigger")
        public String trigger;
    }
}
