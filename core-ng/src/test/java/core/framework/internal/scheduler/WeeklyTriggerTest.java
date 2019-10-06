package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.LocalTime.of;
import static java.time.ZonedDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;

class WeeklyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/Los_Angeles");

    @Test
    void next() {
        WeeklyTrigger trigger = new WeeklyTrigger(DayOfWeek.WEDNESDAY, of(2, 0));  // @WednesdayT2:00 every week

        // 2016-1-13 is Wednesday
        assertThat(trigger.next(date("2016-01-13T01:00:00"))).isEqualTo(date("2016-01-13T02:00:00"));
        assertThat(trigger.next(ZonedDateTime.parse("2016-01-13T01:00:00Z"))).isEqualTo("2016-01-13T02:00:00Z");

        assertThat(trigger.next(date("2016-01-13T02:00:00"))).isEqualTo(date("2016-01-20T02:00:00"));
        assertThat(trigger.next(date("2016-01-13T02:30:00"))).isEqualTo(date("2016-01-20T02:00:00"));

        // 2016-1-12 is Tuesday, next should be Wednesday this week
        assertThat(trigger.next(date("2016-01-12T01:00:00"))).isEqualTo(date("2016-01-13T02:00:00"));
        // 2016-1-19 is Thursday, next should be Wednesday next week
        assertThat(trigger.next(date("2016-01-19T01:00:00"))).isEqualTo(date("2016-01-20T02:00:00"));
    }

    @Test
    void description() {
        WeeklyTrigger trigger = new WeeklyTrigger(DayOfWeek.WEDNESDAY, of(2, 0));  // @WednesdayT2:00 every week
        assertThat(trigger.toString()).isEqualTo("weekly@WEDNESDAY/02:00");
    }

    private ZonedDateTime date(String date) {
        return of(parse(date), US);
    }
}
