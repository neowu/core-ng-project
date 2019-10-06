package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static java.time.ZonedDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;

class MonthlyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        MonthlyTrigger trigger = new MonthlyTrigger(2, LocalTime.of(3, 0));   // @2T3:00 every month

        assertThat(trigger.next(date("2017-04-02T02:00:00"))).isEqualTo(date("2017-04-02T03:00:00"));
        assertThat(trigger.next(ZonedDateTime.parse("2017-04-02T02:00:00Z"))).isEqualTo("2017-04-02T03:00:00Z");

        assertThat(trigger.next(date("2017-04-02T03:00:00"))).isEqualTo(date("2017-05-02T03:00:00"));
        assertThat(trigger.next(date("2017-04-02T03:30:00"))).isEqualTo(date("2017-05-02T03:00:00"));
    }

    @Test
    void description() {
        MonthlyTrigger trigger = new MonthlyTrigger(5, LocalTime.of(1, 0));

        assertThat(trigger.toString()).isEqualTo("monthly@5/01:00");
    }

    private ZonedDateTime date(String date) {
        return of(parse(date), US);
    }
}
