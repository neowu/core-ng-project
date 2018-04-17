package core.framework.impl.web.management;

import core.framework.api.json.Property;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;

import java.util.List;

/**
 * @author neo
 */
public class UpdateTopicRequest {
    @Property(name = "partitions")
    public Integer partitions;

    @Size(min = 1)
    @Property(name = "delete_records")
    public List<DeleteRecord> deleteRecords;

    public static class DeleteRecord {
        @NotNull
        @Min(0)
        @Property(name = "partition")
        public Integer partition;

        @NotNull
        @Min(0)
        @Property(name = "before_offset")
        public Long beforeOffset;
    }
}
