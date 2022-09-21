package core.framework.internal.cache;

import core.framework.internal.validate.ClassValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author neo
 */
public final class CacheClassValidator {
    private final ClassValidator validator;

    public CacheClassValidator(Class<?> cacheClass) {
        // cache class validator accepts all json types without @Property annotation checking
        validator = new ClassValidator(cacheClass);
        validator.allowedValueClasses = Set.of(String.class, Boolean.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            LocalDate.class, LocalDateTime.class, ZonedDateTime.class, Instant.class, LocalTime.class);
    }

    public void validate() {
        validator.validate();
    }
}
