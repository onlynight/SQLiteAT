package demo.database.dao;

import com.github.onlynight.sqliteat.BaseDAO;
import com.github.onlynight.sqliteat.SQLiteATDataBase;

import demo.database.model.Monkey;

/**
 * Created by lion on 2017/4/10.
 */

public class MonkeyDao extends BaseDAO<Monkey> {

    public static final String TABLE_NAME = "Monkey";

    public MonkeyDao(SQLiteATDataBase database) {
        super(database, TABLE_NAME);
    }
}
