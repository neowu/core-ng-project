package core.log.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author neo
 */
public class IndexName {
    static String name(String type, LocalDate now) {
        return type + "-" + now.format(DateTimeFormatter.ISO_DATE);
    }
}
