package de.xlb.minebase.types;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for creating a Table
 */
public class TableConfig {
    private final String tableName;
    private List<Map<String, String>> tableColumns = new ArrayList<>();
    private String[] primaryKeys = null;

    /**
     * Create Table Config for a Table
     * @param tableName
     */
    public TableConfig(String tableName){
        this.tableName = tableName;
    }

    /**
     * Gets the Tablename
     * @return String
     */
    public String getTableName(){
        return this.tableName;
    }

    /**
     * Column Types for SQLite
     */
    public enum ColTypes{
        NULL("NULL"),
        INTEGER("INTEGER"),
        REAL("REAL"), //FLOAT
        TEXT("TEXT"),
        BLOB("BLOB");

        private String id;

        ColTypes(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Add new Column to Config
     * @param colName
     * @param colType
     */
    public void addColumn(@NotNull String colName, @NotNull ColTypes colType){
        Map<String, String> column = new HashMap<>();
        column.put("name", colName);
        column.put("type", colType.getId());
        this.tableColumns.add(column);
    }

    /**
     * Add Columnconfiglist
     * @param columnList
     */
    public void addColumnList(List<Map<String, String>> columnList){
        this.tableColumns = columnList;
    }

    /**
     * Get Columnconfig
     * @return List<Map<String, String>>
     */
    public List<Map<String, String>> getTableColumns(){
        return this.tableColumns;
    }

    /**
     * Set Primary Key
     * @param primaryKeys
     */
    public void setPrimary(String... primaryKeys){
        this.primaryKeys = primaryKeys;
    }

    /**
     * Get Primary Keys
     * @return String[]
     */
    public String[] getPrimaryKeys(){
        return this.primaryKeys;
    }

}
