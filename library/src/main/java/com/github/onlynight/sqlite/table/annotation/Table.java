package com.github.onlynight.sqlite.table.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * if any class use this annotation and the the class
 *
 * @author onlynight
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Table {

    /**
     * appoint the table name, if not appoint this value, then the tablehelper
     * will use the class name for the table name.
     *
     * @return
     */
    String value() default "";

    PrimaryKey[] primaryKeys() default {};
}
