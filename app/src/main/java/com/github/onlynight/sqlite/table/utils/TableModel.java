package com.github.onlynight.sqlite.table.utils;

/**
 * contains table class and table name
 * 
 * @author onlynight
 * 
 */
public class TableModel {

	/**
	 * the class of table
	 */
	private Class<?> clazz;
	
	/**
	 * the table name in database
	 */
	private String tableName;

	public TableModel() {
		super();
	}

	public TableModel(Class<?> clazz, String tableName) {
		super();
		this.clazz = clazz;
		this.tableName = tableName;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
