package core.framework.api.validate;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;


import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Digits can be used on numeric fields.
 *
 * @author chris
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Digits {
    int integer() default -1; // maximum number of integral digits accepted for this number

    int fraction() default -1; // maximum number of fractional digits accepted for this number

    String message() default "field out of bounds (<{integer} digits>.<{fraction} digits> expected), value={value}";
}
