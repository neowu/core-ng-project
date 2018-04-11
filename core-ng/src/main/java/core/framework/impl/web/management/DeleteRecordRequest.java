package core.framework.impl.web.management;

import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
import core.framework.api.web.service.QueryParam;

/**
 * @author neo
 */
public class DeleteRecordRequest {
    @NotNull
    @Min(0)
    @QueryParam(name = "partition")
    public Integer partition;

    @NotNull
    @Min(0)
    @QueryParam(name = "offset")
    public Long offset;
}
