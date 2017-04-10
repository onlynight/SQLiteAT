package com.github.onlynight.sqlite.table.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * class field and annotation. to connect the field and field annotations, you
 * should use this class.
 * 
 * @author onlynight
 * 
 */
public class ColumnAndAnnotation {

	/**
	 * field
	 */
	private Field field;

	/**
	 * the type is object. becourse annotation
	 */
	private Annotation annotation;

	public ColumnAndAnnotation(Field field, Annotation annotation) {
		super();
		this.field = field;
		this.annotation = annotation;
	}

	public Field getField() {
		return field;
	}

	public Annotation getAnnotation() {
		return annotation;
	}
}
