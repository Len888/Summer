package com.swingfrog.summer.db.repository.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String type() default "";
    boolean readOnly() default false;
    boolean nonNull() default false;
    boolean unsigned() default false;
    int length() default 255;
    String comment() default "";

}
