package core.log.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.time.LocalDate;

/**
 * @author neo
 */
public class UploadRequest {
    @NotNull
    @Property(name = "date")
    public LocalDate date = LocalDate.now();
}
