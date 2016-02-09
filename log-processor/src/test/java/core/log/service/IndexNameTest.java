package core.log.service;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class IndexNameTest {
    @Test
    public void name() {
        assertEquals("action-2016-01-15", IndexName.name("action", LocalDate.of(2016, Month.JANUARY, 15)));
    }
}