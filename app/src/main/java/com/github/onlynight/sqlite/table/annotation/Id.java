package com.github.onlynight.sqlite.table.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * if any class which annotation by @Table which property appoint this
 * annotation, the property will become a primary key, and the primary key will
 * auto increasement.
 * 
 * @author onlynight
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Id {

}
