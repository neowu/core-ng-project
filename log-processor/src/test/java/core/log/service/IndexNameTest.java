package core.log.service;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class IndexNameTest {
    @Test
    void name() {
        assertEquals("action-2016-01-15", IndexName.name("action", LocalDate.of(2016, Month.JANUARY, 15)));
    }
}
