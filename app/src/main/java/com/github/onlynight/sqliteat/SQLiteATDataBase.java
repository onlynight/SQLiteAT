package com.github.onlynight.sqliteat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.onlynight.sqlite.database.BaseSQLiteDataBaseSession;

import java.util.List;

public class SQLiteATDataBase extends SQLiteOpenHelper {

    private BaseSQLiteDataBaseSession session;

    public SQLiteATDataBase(Context context, String name, int version, Class<?>[] models) {
        super(context, name, null, version);
        session = new BaseSQLiteDataBaseSession(name, models);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        List<String> tables = session.createTables();
        for (String sql : tables) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<String> tables = session.dropTables();
        for (String sql : tables) {
            db.execSQL(sql);
        }
        onCreate(db);
    }

    public boolean isTableExist(String tableName) {
        String sql = session.isTableExist(tableName);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return cursor.moveToNext();
    }

}
