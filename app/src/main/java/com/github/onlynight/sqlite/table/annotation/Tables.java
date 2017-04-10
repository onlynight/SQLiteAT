package com.github.onlynight.sqlite.table.annotation;


import java.lang.annotation.*;

/**
 * if any class use this annotation and the the class
 *
 * @author onlynight
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Tables {

    Table[] value();
}
