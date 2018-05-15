package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.search.Index;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
@Index(index = "document", type = "document")
public class TestDocument {
    @Property(name = "id")
    public String id;

    @Property(name = "string_field")
    public String stringField;

    @Property(name = "num_field")
    public Integer numField;

    @Property(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;
}
