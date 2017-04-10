package demo.database.dao;

import com.github.onlynight.sqliteat.BaseDAO;
import com.github.onlynight.sqliteat.SQLiteATDataBase;

import demo.database.model.Animal;

/**
 * Created by lion on 2017/4/10.
 */

public class AnimalDao extends BaseDAO<Animal> {

    public static final String TABLE_NAME = "Animal";

    public AnimalDao(SQLiteATDataBase database) {
        super(database, TABLE_NAME);
    }
}
