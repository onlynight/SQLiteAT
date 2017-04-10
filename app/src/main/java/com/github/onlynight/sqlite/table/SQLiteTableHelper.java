package com.github.onlynight.sqlite.table;

import com.github.onlynight.sqlite.SQLiteUtils;
import com.github.onlynight.sqlite.table.annotation.Column;
import com.github.onlynight.sqlite.table.annotation.ForeignKey;
import com.github.onlynight.sqlite.table.annotation.Id;
import com.github.onlynight.sqlite.table.annotation.PrimaryKey;
import com.github.onlynight.sqlite.table.annotation.Table;
import com.github.onlynight.sqlite.table.annotation.Tables;
import com.github.onlynight.sqlite.table.utils.ColumnAndAnnotation;
import com.github.onlynight.sqlite.table.utils.TableModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author onlynight
 * @Class if table has @Table annotation , then the helper will help the class
 * create a sql string to create a table
 * @Params if params has the @Colum annotation , then load the annotation's
 * field. if not , the help will use all the fields to create the table
 * by default.
 * <p>
 * the tablehelper will help you to create a sql.
 * @createTable
 */
public class SQLiteTableHelper implements SQLiteUtils {

    public static final String DEFAULT_ID_COLUMN_NAME = "id";

    public static List<String> createTables(Class<?>[] models) {
        List<String> tables = new ArrayList<String>();
        for (Class<?> clazz : models) {
            List<String> tb = createTable(clazz);
            for (String table : tb) {
                tables.add(table);
            }

        }
        return tables;
    }

    public static List<String> dropTable(Class<?> model) {

        List<String> tables = new ArrayList<String>();
        if (model.isAnnotationPresent(Table.class)) {
            Table table = model.getAnnotation(Table.class);
            String tableName = table.value();
            if (!tableName.equals("")) {
                tables.add(SQLITE_CMD_DROP_TABLE_IF_EXIST + " " + tableName);
            } else {
                tables.add(SQLITE_CMD_DROP_TABLE_IF_EXIST + " "
                        + model.getSimpleName());
            }

        } else if (model.isAnnotationPresent(Tables.class)) {
            Tables aTables = model.getAnnotation(Tables.class);
            Table[] tbs = aTables.value();
            for (Table tableName : tbs) {
                tables.add(SQLITE_CMD_DROP_TABLE_IF_EXIST + " "
                        + tableName.value());
            }
        } else {
            String tableName = model.getSimpleName();
            tables.add(SQLITE_CMD_DROP_TABLE_IF_EXIST + " " + tableName);
        }
        return tables;
    }

    public static List<String> dropTables(Class<?>[] models) {
        List<String> tables = new ArrayList<String>();
        for (Class<?> clazz : models) {
            List<String> tb = dropTable(clazz);
            tables.addAll(tb);
        }
        return tables;
    }

    public static List<String> createTable(Class<?> model) {

        List<String> tables = new ArrayList<String>();
        String tableName = "";
        if (model.isAnnotationPresent(Table.class)) { // if class has the @Table
            // annotation
            Table tableAnnotation = model.getAnnotation(Table.class);
            if (!tableAnnotation.value().equals("")) {
                tableName = tableAnnotation.value();
            } else {
                tableName = model.getSimpleName();
            }
            List<PrimaryKey> pKeys = getPrimairyKeys(model);
            tables.add(createTableSQL(tableName, model, pKeys));

        } else if (model.isAnnotationPresent(Tables.class)) { // if class has
            // the @Tables
            // annotation
            Tables aTables = model.getAnnotation(Tables.class);
            Table[] annotations = aTables.value();
            for (Table table : annotations) {
                List<PrimaryKey> pKeys = getPrimairyKeys(model);
                tables.add(createTableSQL(table.value(), model, pKeys));
            }
        } else { // if class has no annotation
            tableName = model.getSimpleName();
            tables.add(createTableSQL(tableName, model, null));
        }

        return tables;
    }

    public static Set<Field> getIdColumn(Class<?> model, Set<Field> fields) {

        Set<Field> ids = null;
        /**
         * if the table has the composite primary key, it means that the table
         * has no primary key.
         */
        List<PrimaryKey> fks = getPrimairyKeys(model);
        if (fks != null) {
            ids = new HashSet<Field>();
            for (PrimaryKey primaryKey : fks) {
                for (Field field : fields) {
                    if (field.getName().equals(primaryKey.value())) {
                        ids.add(field);
                    }
                }
            }
            return ids;
        }

        /**
         * if annotate the @Id on one field, it means that you want to have an
         * auto increment primary key.
         */
        List<ColumnAndAnnotation> columns = getAllColumn(fields);
        for (ColumnAndAnnotation column : columns) {
            if (column.getAnnotation() instanceof Id) {
                ids = new HashSet<Field>();
                ids.add(column.getField());
                return ids;
            }
        }

        /**
         * if the table has the column that named by DEFAULT_ID_COLUMN_NAME, it
         * means that you want to have an auto increment primary key.
         */
        for (Field field : fields) {
            if (field.getName().equals(DEFAULT_ID_COLUMN_NAME)) {
                ids = new HashSet<Field>();
                ids.add(field);
                return ids;
            }
        }

        /**
         * in total, the table has no one of the description, than the table has
         * no auto increment column.
         */
        return null;
    }

    private static String createTableSQL(String tableName, Class<?> model,
                                         List<PrimaryKey> primaryKeys) {

        StringBuilder sql = new StringBuilder();
        List<ColumnAndAnnotation> columns = getAllColumn(getAllFields(model));

        sql.append(SQLITE_KEYWORD_CREATE + " " + SQLITE_KEYWORD_TABLE + " "
                + tableName + " (");
        if (primaryKeys != null) { // if the table has the composite primary
            // keys, the create primary key
            for (ColumnAndAnnotation column : columns) {
                sql.append(createColumSQL(column, true));
            }
            sql.append(createPrimaryKeys(primaryKeys));
        } else {
            for (ColumnAndAnnotation column : columns) {
                sql.append(createColumSQL(column, false));
            }
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        return sql.toString();
    }

    private static String createPrimaryKeys(List<PrimaryKey> primaryKeys) {
        StringBuilder sql = new StringBuilder();
        StringBuilder args = new StringBuilder();
        for (PrimaryKey primaryKey : primaryKeys) {
            args.append(primaryKey.value()).append(",");
        }
        args.deleteCharAt(args.length() - 1);
        sql.append(SQLITE_CONSTRAINT_PRIMARY_KEY).append("(")
                .append(args.toString()).append(")");
        return addBlank(sql.toString());
    }

    private static List<PrimaryKey> getPrimairyKeys(Class<?> model) {
        List<PrimaryKey> keys = null;

        if (model.isAnnotationPresent(Table.class)) {
            Table table = model.getAnnotation(Table.class);
            PrimaryKey[] primaryKeys = table.primaryKeys();
            if (primaryKeys.length > 0) {
                keys = new ArrayList<PrimaryKey>();
                Collections.addAll(keys, primaryKeys);
            }
        }

        return keys;
    }

    public static Set<Field> getAllFields(Class<?> model) {

        Set<Field> fields = new HashSet<Field>();
        Class<?> current = model;

        for (Field field : current.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }

        while (current != Object.class) {
            Field[] temp = current.getSuperclass().getDeclaredFields();
            for (Field field : temp) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }

        return fields;
    }

    public static List<ColumnAndAnnotation> getAllColumn(Set<Field> fields) {
        List<ColumnAndAnnotation> columns = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                ColumnAndAnnotation columnAndAnnotation = new ColumnAndAnnotation(
                        field, field.getAnnotation(Column.class));
                columns.add(columnAndAnnotation);
            } else if (field.isAnnotationPresent(Id.class)) {
                ColumnAndAnnotation columnAndAnnotation = new ColumnAndAnnotation(
                        field, field.getAnnotation(Id.class));
                columns.add(columnAndAnnotation);
            } else {
                ColumnAndAnnotation columnAndAnnotation = new ColumnAndAnnotation(
                        field, createColumnAnnotation(field));
                columns.add(columnAndAnnotation);
            }
        }
        return columns;
    }

    public static boolean hasAutoIncrementPK(Class<?> model) {

        /**
         * if the table has the composite primary key, it means that the table
         * has no primary key.
         */
        List<PrimaryKey> fks = getPrimairyKeys(model);
        if (fks != null) {
            return false;
        }

        /**
         * if the table has the column that named by DEFAULT_ID_COLUMN_NAME, it
         * means that you want to have an auto increment primary key.
         */
        Set<Field> fields = getAllFields(model);
        for (Field field : fields) {
            if (field.getName().equals(DEFAULT_ID_COLUMN_NAME)) {
                return true;
            }
        }

        /**
         * if annotate the @Id on one field, it means that you want to have an
         * auto increment primary key.
         */
        List<ColumnAndAnnotation> columns = getAllColumn(fields);
        for (ColumnAndAnnotation column : columns) {
            if (column instanceof Id) {
                return true;
            }
        }

        /**
         * in total, the table has no one of the description, than the table has
         * no auto increment column.
         */
        return false;
    }

    public static String getColumType(Class<?> fieldType) {

        if (String.class == fieldType) {
            return SQLITE_DATA_TYPE_TEXT;
        } else if (Integer.TYPE == fieldType || Integer.class == fieldType) {
            return SQLITE_DATA_TYPE_INTEGER;
        } else if (Long.TYPE == fieldType || Long.class == fieldType) {
            return SQLITE_DATA_TYPE_LONG;
        } else if (Float.TYPE == fieldType || Float.class == fieldType) {
            return SQLITE_DATA_TYPE_FLOAT;
        } else if (Short.TYPE == fieldType || Short.class == fieldType) {
            return SQLITE_DATA_TYPE_SHORT;
        } else if (Double.TYPE == fieldType || Double.class == fieldType) {
            return SQLITE_DATA_TYPE_DOUBLE;
        } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
            return SQLITE_DATA_TYPE_BOOLEAN;
        }
        return SQLITE_DATA_TYPE_TEXT;
    }

    public static List<String> getTables(Class<?> model) {
        List<String> tables = new ArrayList<String>();
        String tableName;
        if (model.isAnnotationPresent(Table.class)) {
            // if class has the @Table
            // annotation
            Table tableAnnotation = model.getAnnotation(Table.class);
            if (!tableAnnotation.value().equals("")) {
                tableName = tableAnnotation.value();
            } else {
                tableName = model.getSimpleName();
            }
            tables.add(tableName);

        } else if (model.isAnnotationPresent(Tables.class)) {
            // if class has
            // the @Tables
            // annotation
            Tables aTables = model.getAnnotation(Tables.class);
            Table[] annotations = aTables.value();
            for (Table table : annotations) {
                tables.add(table.value());
            }
        } else { // if class has no annotation
            tableName = model.getSimpleName();
            tables.add(tableName);
        }
        return tables;
    }

    public static Set<TableModel> getTableModels(Set<Class<?>> models) {
        Set<TableModel> tableModels = new HashSet<TableModel>();

        TableModel tableModel = null;
        for (Class<?> model : models) {
            if (model.isAnnotationPresent(Table.class)) {
                // if class has the @Table
                // annotation
                Table tableAnnotation = model.getAnnotation(Table.class);
                if (!tableAnnotation.value().equals("")) {
                    tableModel = new TableModel(model, tableAnnotation.value());
                } else {
                    tableModel = new TableModel(model, model.getSimpleName());
                }
                tableModels.add(tableModel);
            } else if (model.isAnnotationPresent(Tables.class)) {
                // if class has
                // the @Tables
                // annotation
                Tables aTables = model.getAnnotation(Tables.class);
                Table[] annotations = aTables.value();
                for (Table table : annotations) {
                    tableModel = new TableModel(model, table.value());
                    tableModels.add(tableModel);
                }
            } else { // if class has no annotation
                tableModel = new TableModel(model, model.getSimpleName());
                tableModels.add(tableModel);
            }
        }

        return tableModels;
    }

    private static String createColumSQL(ColumnAndAnnotation column,
                                         boolean compositePrimaryKey) {

        StringBuilder columnSql = new StringBuilder();

        // if the field has the @Id annotation and the table has no composite
        // primary key
        if ((column.getAnnotation() instanceof Id)
                && !compositePrimaryKey) {
            String idCloumn = column.getField().getName() + " "
                    + getColumType(column.getField().getType()) + " "
                    + SQLITE_CONSTRAINT_PRIMARY_KEY + " "
                    + SQLITE_KEYWORD_AUTOINCREASEMENT + ",";
            columnSql.append(idCloumn);
        }

        // if the field has named DEFAULT_ID_COLUMN_NAME and the table has no
        // composite primary key
        else if ((column.getAnnotation() instanceof Column)
                && ((Column) column.getAnnotation()).value().equals(
                DEFAULT_ID_COLUMN_NAME) && !compositePrimaryKey) {
            String idCloumn = DEFAULT_ID_COLUMN_NAME + " "
                    + getColumType(column.getField().getType()) + " "
                    + SQLITE_CONSTRAINT_PRIMARY_KEY + " "
                    + SQLITE_KEYWORD_AUTOINCREASEMENT + ",";
            columnSql.append(idCloumn);
        }

        // else the field has the @Column annotation
        else if (column.getAnnotation() instanceof Column) {

            String col = column.getField().getName() + " "
                    + getColumType(column.getField().getType());
            columnSql.append(addBlank(col));

            Column annotation = (Column) column.getAnnotation();
            if (annotation.unique()) {
                String unique = addBlank(SQLITE_CONSTRAINT_UNIQUE);
                columnSql.append(unique);
            }
            if (annotation.notnull()) {
                String notnull = addBlank(SQLITE_CONSTRAINT_NOT_NULL);
                columnSql.append(notnull);
            }
            if (annotation.check() != null &&
                    !annotation.check().equals("")) {
                String check = addBlank(SQLITE_CONSTRAINT_CHECK + "("
                        + annotation.check() + ")");
                columnSql.append(check);
            }
            if (annotation.default_value() != null && !annotation.default_value().equals("")) {
                String default_value = addBlank(SQLITE_CONSTRAINT_DEFAULT + " "
                        + "'" + annotation.default_value() + "'");
                columnSql.append(default_value);
            }

            String foreign = addForeignKey(annotation.value(),
                    annotation.foreignkey());
            if (foreign != null) {
                columnSql.append(foreign);
            }

            columnSql.append(",");
        }

        return columnSql.toString();
    }

    private static String addBlank(String src) {
        return " " + src + " ";
    }

    private static String addForeignKey(String name, ForeignKey foreignKey) {
        String sql = null;
        if (foreignKey != null && foreignKey.srcClass() != Class.class) {
            sql = " " + SQLITE_KEYWORD_REFERENCES + " "
                    + foreignKey.srcClass().getSimpleName() + "("
                    + foreignKey.column() + ")";
            if (foreignKey.onDelete()) {
                sql += addBlank(ON_DELETE_CASCADE);
            }
            if (foreignKey.onUpdate()) {
                sql += addBlank(ON_UPDATE_CASCADE);
            }
        }
        return sql;
    }

    private static Column createColumnAnnotation(final Field field) {
        if (!field.isAnnotationPresent(Column.class)) {
            return new Column() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Column.class;
                }

                @Override
                public String value() {
                    return field.getName();
                }

                @Override
                public boolean unique() {
                    return false;
                }

                @Override
                public boolean notnull() {
                    return false;
                }

                @Override
                public ForeignKey foreignkey() {
                    return null;
                }

                @Override
                public String default_value() {
                    return null;
                }

                @Override
                public String check() {
                    return null;
                }
            };
        }
        return null;
    }

    public static String sqliteEscape(String keyWord) {
        keyWord = keyWord.replace("'", "''");
        return keyWord;
    }
}
