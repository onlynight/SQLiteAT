package com.github.onlynight.sqliteat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.onlynight.sqlite.table.SQLiteTableDAO;
import com.github.onlynight.sqlite.table.SQLiteTableHelper;
import com.github.onlynight.sqlite.table.SQLiteTableSession;
import com.github.onlynight.sqlite.table.annotation.Column;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BaseDAO<T> extends SQLiteTableDAO<T> {

    private static final String TAG = "SQLite Hibernate";

    private SQLiteATDataBase database;
    private SQLiteTableSession tableSession;
    private Class<?> model;

    @SuppressWarnings("unchecked")
    public BaseDAO(SQLiteATDataBase database, String tableName) {
        this.database = database;
        this.model = ((Class<T>) ((ParameterizedType) super.getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
        this.tableSession = new SQLiteTableSession(model, tableName);
    }

    @Override
    public boolean isTableExist() {
        String sql = tableSession.isTableExist();
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return cursor.moveToNext();
    }

    @Override
    public void insert(T entity) {
        String sql = tableSession.insert(entity);
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void insert(List<T> entities) {
        String sql = tableSession.insert(entities);
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void delete(T entity) {
        String sql = tableSession.delete(entity);
        if (sql == null) {
            System.out.println("delete sql error");
        }
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void delete(List<T> entities) {
        String sql = tableSession.delete(entities);
        if (sql == null) {
            System.out.println("delete sql error");
        }
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void update(T entity) {
        String sql = tableSession.update(entity);
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public void update(List<T> entities) {
        String sql = tableSession.update(entities);
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL(sql);
    }

    @Override
    public List<T> select(T entity) {
        String sql = tableSession.select(entity);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return getObjectList(cursor);
    }

    @Override
    public List<T> select(String column, String value) {
        String sql = tableSession.select(column, value);
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return getObjectList(cursor);
    }

    @Override
    public List<T> select(String[] columns, String[] values) {
        String sql = tableSession.select(columns, values);
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return getObjectList(cursor);
    }

    @Override
    public List<T> selectAll() {
        String sql = tableSession.selectAll();
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return getObjectList(cursor);
    }

    @Override
    public long count() {
        String sql = tableSession.count();
        Log.d(TAG, sql);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    @Override
    public void execute(String sql) {
        String temp = tableSession.execute(sql);
        Log.d(TAG, temp);
        SQLiteDatabase db = database.getReadableDatabase();
        db.execSQL(temp);
    }

    @Override
    public List<T> executeQuery(String sql) {
        String temp = tableSession.execute(sql);
        Log.d(TAG, temp);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(temp, null);
        return getObjectList(cursor);
    }

    public List<T> rawQuery(String sql, String[] args) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        return getObjectList(cursor);
    }

    public long count(String sql, String[] args) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    @SuppressWarnings("unchecked")
    private List<T> getObjectList(Cursor cursor) {
        List<T> objects = new ArrayList<T>();
        Set<Field> fields = SQLiteTableHelper.getAllFields(model);

        while (cursor.moveToNext()) {
            Object entity;
            try {
                entity = model.newInstance();
                for (Field field : fields) {
                    Column column = null;
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Column.class)) {
                        column = (Column) field.getAnnotation(Column.class);
                        Class<?> fieldType = field.getType();
                        if (!column.value().equals("")) {
                            int c = cursor.getColumnIndex(column.value());
                            if (c >= 0) {
                                setFieldValue(field, entity, fieldType, cursor, c);
                            }
                        } else {
                            int c = cursor.getColumnIndex(field.getName());
                            if (c >= 0) {
                                setFieldValue(field, entity, fieldType, cursor, c);
                            }
                        }
                    } else {
                        int c = cursor.getColumnIndex(field.getName());
                        Class<?> fieldType = field.getType();
                        if (c >= 0) {
                            setFieldValue(field, entity, fieldType, cursor, c);
                        }
                    }
                }
                objects.add((T) entity);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return objects;
    }

    private void setFieldValue(Field field, Object entity, Class<?> fieldType,
                               Cursor cursor, int index) throws IllegalAccessException,
            IllegalArgumentException {
        if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
            field.set(entity, cursor.getInt(index));
        } else if (String.class == fieldType) {
            field.set(entity, cursor.getString(index));
        } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
            field.set(entity, cursor.getLong(index));
        } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
            field.set(entity, cursor.getFloat(index));
        } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
            field.set(entity, cursor.getShort(index));
        } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
            field.set(entity, cursor.getDouble(index));
        } else if (Character.TYPE == fieldType) {
            String fieldValue = cursor.getString(index);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                field.set(entity, fieldValue.charAt(0));
            }
        }
    }
}
