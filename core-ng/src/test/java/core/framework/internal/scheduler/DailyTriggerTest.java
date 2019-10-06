package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.LocalTime.of;
import static java.time.ZonedDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class DailyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        var trigger = new DailyTrigger(of(10, 0));

        assertThat(trigger.next(date("2017-04-10T09:00:00"))).isEqualTo(date("2017-04-10T10:00:00"));
        assertThat(trigger.next(ZonedDateTime.parse("2017-04-10T09:00:00Z"))).isEqualTo("2017-04-10T10:00:00Z");

        assertThat(trigger.next(date("2017-04-10T10:00:00"))).isEqualTo(date("2017-04-11T10:00:00"));
        assertThat(trigger.next(date("2017-04-10T11:00:00"))).isEqualTo(date("2017-04-11T10:00:00"));
    }

    @Test
    void nextWithDayLightSavingStart() {
        var trigger = new DailyTrigger(of(2, 30));

        // daylight saving started at 2017/03/12
        assertThat(trigger.next(date("2017-03-12T01:00:00"))).isEqualTo(date("2017-03-12T02:30:00"));
        assertThat(trigger.next(date("2017-03-12T02:30:00"))).isEqualTo(date("2017-03-13T02:30:00"));
        assertThat(trigger.next(date("2017-03-13T02:30:00"))).isEqualTo(date("2017-03-14T02:30:00"));
    }

    @Test
    void nextWithDayLightSavingEnd() {
        var trigger = new DailyTrigger(of(2, 0));

        // daylight saving ended at 2017/11/05
        assertThat(trigger.next(date("2017-11-05T00:00:00"))).isEqualTo(date("2017-11-05T02:00:00"));
        assertThat(trigger.next(date("2017-11-05T02:00:00"))).isEqualTo(date("2017-11-06T02:00:00"));
        assertThat(trigger.next(date("2017-11-05T02:30:00"))).isEqualTo(date("2017-11-06T02:00:00"));
    }

    @Test
    void description() {
        var trigger = new DailyTrigger(of(2, 30));

        assertThat(trigger.toString()).isEqualTo("daily@02:30");
    }

    private ZonedDateTime date(String date) {
        return of(parse(date), US);
    }
}
