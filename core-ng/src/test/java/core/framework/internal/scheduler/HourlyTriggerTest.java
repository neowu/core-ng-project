package core.framework.internal.scheduler;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.LocalDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author keith
 */
class HourlyTriggerTest {
    private static final ZoneId US = ZoneId.of("America/New_York");

    @Test
    void next() {
        var trigger = new HourlyTrigger(0, 0);
        assertThat(trigger.next(date("2019-08-16T09:00:00"))).isEqualTo(date("2019-08-16T10:00:00"));

        trigger = new HourlyTrigger(30, 0);
        assertThat(trigger.next(date("2019-08-16T09:30:00"))).isEqualTo(date("2019-08-16T10:30:00"));

        trigger = new HourlyTrigger(0, 30);
        assertThat(trigger.next(date("2019-08-16T09:00:20"))).isEqualTo(date("2019-08-16T09:00:30"));
        assertThat(trigger.next(date("2019-08-16T09:00:30"))).isEqualTo(date("2019-08-16T10:00:30"));
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> new HourlyTrigger(61, 0))
            .isInstanceOf(Error.class)
            .hasMessageContaining("minute is out of range");

        assertThatThrownBy(() -> new HourlyTrigger(0, -1))
            .isInstanceOf(Error.class)
            .hasMessageContaining("second is out of range");
    }

    @Test
    void description() {
        var trigger = new HourlyTrigger(2, 30);

        assertThat(trigger.toString()).isEqualTo("hourly@2:30");
    }

    private ZonedDateTime date(String date) {
        return ZonedDateTime.of(parse(date), US);
    }
}
