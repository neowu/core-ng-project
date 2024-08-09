package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.search.Index;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
@Index(name = "aggregation_document")
public class TestAggregationDocument {
    @Property(name = "date")
    public ZonedDateTime date;

    @Property(name = "key_1")
    public String key1;

    @Property(name = "key_2")
    public String key2;

    @NotNull
    @Property(name = "value")
    public Integer value;
}
