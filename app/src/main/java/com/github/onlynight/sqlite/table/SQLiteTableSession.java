package com.github.onlynight.sqlite.table;

import com.github.onlynight.sqlite.table.annotation.Id;
import com.github.onlynight.sqlite.table.annotation.Table;
import com.github.onlynight.sqlite.table.annotation.Tables;
import com.github.onlynight.sqlite.table.utils.KeyValues;
import com.github.onlynight.sqlite.SQLiteUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * table session. it will help you to create basic SQL, such as insert, update,
 * delete, select and so on.
 *
 * @author onlynight
 */
public class SQLiteTableSession implements SQLiteUtils {

    private static final String PARAMS_COLUMNS = "columns";
    private static final String PARAMS_VALUES = "values";

    private Class<?> clazz;
    private String tableName;
    private Set<Field> idColumns;
    private Set<Field> fields;
    private String paramTableName;
    private String outTableName;

    public SQLiteTableSession(Class<?> clazz, String tableName) {
        this.clazz = clazz;
        this.paramTableName = tableName;
        this.tableName = getTableName(clazz);
        setTableName(this.tableName);
        this.fields = SQLiteTableHelper.getAllFields(clazz);
        this.idColumns = SQLiteTableHelper.getIdColumn(clazz, fields);
    }

    /**
     * use the class model and input table name to search if the table is
     * annotated
     *
     * @param model
     * @return
     */
    private String getTableName(Class<?> model) {
        if (model.isAnnotationPresent(Table.class)) {
            if (model.getAnnotation(Table.class).value().equals("") == false) {
                return model.getAnnotation(Table.class).value();
            } else {
                return model.getSimpleName();
            }
        } else if (model.isAnnotationPresent(Tables.class)) {
            Table[] tables = model.getAnnotation(Tables.class).value();
            for (Table table : tables) {
                if (table.value().equals(paramTableName)) {
                    return table.value();
                }
            }
        } else {
            return model.getSimpleName();
        }
        return null;
    }

    /**
     * create an insert one entity SQL string
     *
     * @param entity
     * @return
     */
    public String insert(Object entity) {
        Map<String, String> set = getInsertColumnsAndValues(entity);
        String sql = SQLITE_CMD_INSERT + " " + tableName + "("
                + set.get(PARAMS_COLUMNS) + ") " + SQLITE_KEYWORD_VALUES + "("
                + set.get(PARAMS_VALUES) + ");";
        return sql;
    }

    /**
     * create an insert entities SQL string
     *
     * @param entities
     * @return
     */
    public String insert(List<Object> entities) {
        StringBuilder sql = new StringBuilder();
        for (Object t : entities) {
            sql.append(this.insert(t));
        }
        return sql.toString();
    }

    /**
     * create a delete entity SQL string
     *
     * @param entity
     * @return
     */
    public String delete(Object entity) {
        String sql = null;
        Set<Field> ids = idColumns;
        if (idColumns.size() == 1) {// single primary key
            try {
                Field id = ids.iterator().next();
                id.setAccessible(true);
                sql = SQLITE_KEYWORD_DELETE + " " + SQLITE_KEYWORD_FROM + " "
                        + tableName + " " + SQLITE_KEYWORD_WHERE + " "
                        + id.getName() + "=" + String.valueOf(id.get(entity))
                        + ";";
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else if (idColumns.size() > 1) {// multi primary key
            StringBuilder params = new StringBuilder();
            for (Field field : ids) {
                try {
                    field.setAccessible(true);
                    params.append(field.getName()).append("=")
                            .append(String.valueOf(field.get(entity)))
                            .append(" and ");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            params.delete(params.length() - 5, params.length());
            sql = SQLITE_KEYWORD_DELETE + " " + SQLITE_KEYWORD_FROM + " "
                    + tableName + " " + SQLITE_KEYWORD_WHERE + " "
                    + params.toString() + ";";
        } else {// no primary key
            // TODO no this situation
        }
        return sql;
    }

    public String delete(List<Object> entities) {
        StringBuilder sql = new StringBuilder();
        for (Object t : entities) {
            sql.append(this.delete(t));
        }
        return sql.toString();
    }

    public String update(Object entity) {
        String sql = null;
        Set<Field> ids = idColumns;
        String params = getUpdateColumnsAndValues(entity);
        if (idColumns.size() == 1) {// single primary key
            try {
                sql = SQLITE_KEYWORD_UPDATE
                        + " "
                        + tableName
                        + " "
                        + SQLITE_KEYWORD_SET
                        + " "
                        + params
                        + " "
                        + SQLITE_KEYWORD_WHERE
                        + " "
                        + idColumns.iterator().next().getName()
                        + "="
                        + String.valueOf(idColumns.iterator().next()
                        .get(entity)) + ";";
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else if (idColumns.size() > 1) {// multi primary key
            StringBuilder where = new StringBuilder();
            try {
                for (Field field : ids) {
                    field.setAccessible(true);
                    where.append(field.getName()).append("=")
                            .append(String.valueOf(field.get(entity)))
                            .append(" and ");
                }
                where.delete(where.length() - 5, where.length());
                sql = SQLITE_KEYWORD_UPDATE + " " + tableName + " "
                        + SQLITE_KEYWORD_SET + " " + params + " "
                        + SQLITE_KEYWORD_WHERE + " " + where.toString() + ";";
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {// no primary key
            // TODO no this situation
        }
        return sql;
    }

    public String update(List<Object> entities) {
        StringBuilder sql = new StringBuilder();
        for (Object t : entities) {
            sql.append(this.update(t));
        }
        return sql.toString();
    }

    public String select(Object entity) {
        String sql = null;
        Set<Field> ids = idColumns;
        if (idColumns.size() == 1) {// single primary key
            try {
                Field id = ids.iterator().next();
                id.setAccessible(true);
                sql = SQLITE_KEYWORD_SELECT + " * " + SQLITE_KEYWORD_FROM + " "
                        + tableName + " " + SQLITE_KEYWORD_WHERE + " "
                        + id.getName() + "=" + String.valueOf(id.get(entity))
                        + ";";
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else if (idColumns.size() > 1) {// multi primary key
            StringBuilder params = new StringBuilder();
            for (Field field : ids) {
                try {
                    field.setAccessible(true);
                    params.append(field.getName()).append("=")
                            .append(String.valueOf(field.get(entity)))
                            .append(" and ");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            params.delete(params.length() - 5, params.length());
            sql = SQLITE_KEYWORD_SELECT + " * " + SQLITE_KEYWORD_FROM + " "
                    + tableName + " " + SQLITE_KEYWORD_WHERE + " "
                    + params.toString() + ";";
        } else {// no primary key
            // TODO no this situation
        }
        return sql;
    }

    public String select(String column, String value) {
        String sql = SQLITE_KEYWORD_SELECT + " * " + SQLITE_KEYWORD_FROM + " "
                + tableName + " " + SQLITE_KEYWORD_WHERE + " " + column + "='"
                + value + "';";
        return sql;
    }

    public String select(String[] columns, String[] values) {

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            params.append(columns[i]).append("=").append(values[i])
                    .append(" AND ");
        }
        params.delete(params.length() - 5, params.length());

        String sql = SQLITE_KEYWORD_SELECT + " * " + SQLITE_KEYWORD_FROM + " "
                + tableName + " " + SQLITE_KEYWORD_WHERE + " "
                + params.toString() + ";";

        return sql;
    }

    public String selectAll() {
        String sql = "select * from " + tableName + ";";
        return sql;
    }

    public String count() {
        String sql = "select count( * ) from " + tableName + ";";
        return sql;
    }

    public String isTableExist() {
        String sql = "select * from sqlite_master where type='table' and name='"
                + tableName + "'";
        return sql;
    }

    public String execute(String sql) {
        return sql;
    }

    public String executeQuery(String sql) {
        return sql;
    }

    private void setTableName(String tablename) {
        this.outTableName = tablename;
    }

    /**
     * get the table name
     *
     * @return
     */
    public String getTableName() {
        return outTableName;
    }

    /**
     * get the generic type of the class
     *
     * @return
     */
    public Class<?> getGenericType() {
        return this.clazz;
    }

    /**
     * create an update SQL string's params list
     *
     * @param entity
     * @return
     */
    private String getUpdateColumnsAndValues(Object entity) {
        StringBuilder pairs = new StringBuilder();

        Set<Entry<String, Object>> dataSet = getColumnsAndValues(entity)
                .entrySet();
        for (Entry<String, Object> entry : dataSet) {
            Object value = entry.getValue();
            if (value instanceof String && ((String) value).length() > 0) {
                pairs.append(entry.getKey()).append("=").append(value)
                        .append(",");
            } else {
                pairs.append(entry.getKey()).append("=").append("''")
                        .append(",");
            }
        }
        pairs.deleteCharAt(pairs.length() - 1);

        return pairs.toString();
    }

    /**
     * create an insert SQL string's column name and value
     *
     * @param entity
     * @return the map include columns and values
     */
    private Map<String, String> getInsertColumnsAndValues(Object entity) {

        Map<String, String> params = new HashMap<String, String>();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        Set<Entry<String, Object>> entries = getColumnsAndValues(entity)
                .entrySet();
        for (Entry<String, Object> entry : entries) {
            columns.append(entry.getKey()).append(",");
            Object value = entry.getValue();
            if (value instanceof String && ((String) value).length() > 0) {
                values.append(entry.getValue()).append(",");
            } else {
                values.append("''").append(",");
            }
        }

        columns.deleteCharAt(columns.length() - 1);
        values.deleteCharAt(values.length() - 1);
        params.put(PARAMS_COLUMNS, columns.toString());
        params.put(PARAMS_VALUES, values.toString());
        return params;
    }

    /**
     * get field name and value to write into database.
     *
     * @param entity
     * @return key value pairs
     */
    private KeyValues getColumnsAndValues(Object entity) {
        KeyValues keyValues = new KeyValues();
        String key;
        Object value;
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(Id.class)
                        && !field.getName().equals(SQLiteTableHelper.DEFAULT_ID_COLUMN_NAME)) {
                    key = field.getName();
                    if (String.valueOf(field.get(entity)) != null
                            && !String.valueOf(field.get(entity)).equals("")
                            && !String.valueOf(field.get(entity)).equals("null")) {
                        value = "'" + SQLiteTableHelper.sqliteEscape(String.valueOf(field.get(entity))) + "'";
                    } else {
                        value = String.valueOf(field.get(entity));
                    }
                    keyValues.put(key, value);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return keyValues;
    }
}
