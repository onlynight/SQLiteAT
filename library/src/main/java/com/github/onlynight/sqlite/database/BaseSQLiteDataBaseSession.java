package com.github.onlynight.sqlite.database;

import com.github.onlynight.sqlite.table.SQLiteTableHelper;
import com.github.onlynight.sqlite.table.utils.TableModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * database session. it will help you to create a SQL which is use to create
 * table and drop table.
 *
 * @author onlynight
 */
public class BaseSQLiteDataBaseSession {

    private String databaseName;
    private Set<Class<?>> models;
    private Set<TableModel> tableModels;

    public BaseSQLiteDataBaseSession(String databaseName) {
        super();
        this.databaseName = databaseName;
        this.models = new HashSet<>();
    }

    public BaseSQLiteDataBaseSession(String databaseName, Class<?>[] models) {
        super();
        this.databaseName = databaseName;
        this.models = new HashSet<>();
        setAllModels(models);
    }

    public void setAllModels(Class<?>[] models) {
        Collections.addAll(this.models, models);
        this.tableModels = SQLiteTableHelper.getTableModels(this.models);
    }

    public String isTableExist(String tablename) {
        return "SELECT * FROM sqlite_master WHERE type='table' AND name='"
                + tablename + "'";
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Set<Class<?>> getModels() {
        return models;
    }

    public Set<TableModel> getTableModels() {
        return tableModels;
    }

    public List<String> createTables() {
        return SQLiteTableHelper
                .createTables(models.toArray(new Class<?>[]{}));
    }

    public List<String> dropTables() {
        return SQLiteTableHelper.dropTables(models.toArray(new Class<?>[]{}));
    }
}
