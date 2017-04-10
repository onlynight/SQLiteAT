package demo.database;

import android.content.Context;

import com.github.onlynight.sqliteat.SQLiteATDataBase;

import demo.database.model.Animal;
import demo.database.model.Monkey;

/**
 * Created by lion on 2017/4/10.
 */

public class TestDataBase extends SQLiteATDataBase {

    private static final String NAME = "test.db";
    private static final int VERSION = 1;
    private static final Class<?> clazz[] = {Animal.class, Monkey.class};

    public TestDataBase(Context context) {
        super(context, NAME, VERSION, clazz);
    }

}
