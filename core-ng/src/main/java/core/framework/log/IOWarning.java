package core.framework.log;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(METHOD)
@Retention(RUNTIME)
@Repeatable(IOWarnings.class)
public @interface IOWarning {   // currently only supports WS/Controller and MessageHandler methods, and Executor task inherits from parent
    String operation();

    int maxOperations() default -1;     // total times called by one action, if exceeds, it indicates bad practice (not CD friendly), better change design or split into multiple actions

    int maxElapsedInMs() default -1;    // max elapsed of one operation

    int maxReads() default -1;          // number of rows/entries returned by one query

    int maxTotalReads() default -1;     // total entries read by one action

    int maxTotalWrites() default -1;    // total entries updated by one action
}
