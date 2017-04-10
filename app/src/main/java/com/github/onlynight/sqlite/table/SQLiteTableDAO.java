package com.github.onlynight.sqlite.table;

import com.github.onlynight.sqlite.table.annotation.Column;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * abstract dao
 * 
 * @author onlynight
 * 
 * @param <T>
 */
public abstract class SQLiteTableDAO<T> {

	/**
	 * create if table is exist SQL string
	 * 
	 * @return SQL
	 */
	public abstract boolean isTableExist();

	/**
	 * create a insert SQL string
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract void insert(T entity);

	/**
	 * create a insert SQL string with multi column
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract void insert(List<T> entities);

	/**
	 * create a delete SQL string, the entity must have auto increment primary
	 * key or composite primary key. if not the delete will not execute.
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract void delete(T entity);

	/**
	 * create a delete SQL string, delete multi columns
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract void delete(List<T> entities);

	/**
	 * create a update SQL string the entity must have auto increment primary
	 * key or composite primary key. if not the update will not execute.
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract void update(T entity);

	/**
	 * create a update SQL string, update multi columns
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract void update(List<T> entities);
	
	/**
	 * create a select SQL string the entity must have auto increment primary
	 * key or composite primary key. if not the select will not execute.
	 * 
	 * @param entity
	 *            model
	 * @return SQL string
	 */
	public abstract List<T> select(T entity);

	/**
	 * create a search SQL string
	 * 
	 * @param column
	 *            column name
	 * @param value
	 *            column value
	 * @return SQL string
	 */
	public abstract List<T> select(String column, String value);

	/**
	 * create a search SQL string, the columns number must the same as the
	 * values number,or the select will error.
	 * 
	 * @param columns
	 *            column names
	 * @param values
	 *            column names
	 * @return SQL string
	 */
	public abstract List<T> select(String[] columns, String[] values);

	/**
	 * create a search SQL string to search all
	 * 
	 * @return SQL string
	 */
	public abstract List<T> selectAll();
	
	public abstract long count();

	/**
	 * execute SQL string
	 * 
	 * @param sql
	 * @return
	 */
	public abstract void execute(String sql);

	/**
	 * execute query SQL string
	 * 
	 * @param sql
	 * @return
	 */
	public abstract List<T> executeQuery(String sql);

	@SuppressWarnings("unchecked")
	public List<T> getObjectList(ResultSet set) throws SQLException,
			InstantiationException, IllegalAccessException {
		List<T> objects = new ArrayList<T>();

		Class<?> tableClass = ((Class<T>) ((ParameterizedType) super.getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0]);
		Set<Field> fields = SQLiteTableHelper.getAllFields(tableClass);

		while (set.next()) {
			Object entity = tableClass.newInstance();
			for (Field field : fields) {
				Column column = null;
				field.setAccessible(true);
				if (field.isAnnotationPresent(Column.class)) {
					column = (Column) field.getAnnotation(Column.class);
					if (column.value().equals("") == false) {
						setFieldValue(field, entity, set, column.value());
					} else {
						setFieldValue(field, entity, set, field.getName());
					}
				} else {
					setFieldValue(field, entity, set, field.getName());
				}
			}
			objects.add((T) entity);
		}

		return objects;
	}

	public static void setFieldValue(Field field, Object entity, ResultSet set,
			String columnName) throws IllegalAccessException,
			IllegalArgumentException, SQLException {
		if ((Integer.TYPE == field.getType())
				|| (Integer.class == field.getType())) {
			field.set(entity, Integer.valueOf(set.getInt(columnName)));
		} else if (String.class == field.getType()) {
			field.set(entity, set.getString(columnName));
		} else if ((Long.TYPE == field.getType())
				|| (Long.class == field.getType())) {
			field.set(entity, Long.valueOf(set.getLong(columnName)));
		} else if ((Float.TYPE == field.getType())
				|| (Float.class == field.getType())) {
			field.set(entity, Float.valueOf(set.getFloat(columnName)));
		} else if ((Short.TYPE == field.getType())
				|| (Short.class == field.getType())) {
			field.set(entity, Short.valueOf(set.getShort(columnName)));
		} else if ((Double.TYPE == field.getType())
				|| (Double.class == field.getType())) {
			field.set(entity, Double.valueOf(set.getDouble(columnName)));
		} else if (Character.TYPE == field.getType()) {
			String fieldValue = set.getString(columnName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				field.set(entity, Character.valueOf(fieldValue.charAt(0)));
			}
		}
	}
}
