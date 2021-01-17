package javax.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * help IDE (Intellij) to do nullability analysis﻿
 * <p>
 * refer to https://www.jetbrains.com/help/idea/nullable-and-notnull-annotations.html#nullable
 *
 * @author neo
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
}
