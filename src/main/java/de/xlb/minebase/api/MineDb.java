package de.xlb.minebase.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.xlb.minebase.types.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Main Database Class
 * Database: SQLITE
 */
public class MineDb {
    //Const
    private final String SCHEMA_DEFAULT = "schema_default.json";

    //Vars
    private final File pluginPath;
    private Connection connection;
    private final Logger log;
    private Map<String, Map<String, Object>> schema;

    //LogColors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * Creates DB Instance
     * @param log
     * @param pluginPath
     * @param dbName
     * @throws Exception
     */
    public MineDb(Logger log, File pluginPath, String dbName) throws Exception {
        this.log = log;
        this.pluginPath = pluginPath;
        initDb(log, pluginPath, dbName);
    }

    /**
     * Creates DB Instance with default Database data.db
     * @param log
     * @param pluginPath
     * @throws Exception
     */
    public MineDb(Logger log, File pluginPath) throws Exception {
        this.log = log;
        this.pluginPath = pluginPath;
        initDb(log, pluginPath, "data.db");
    }

    private void initDb(Logger log, File pluginPath, String dbName) throws  Exception{
        //Check Folder
        if (!pluginPath.exists()) {
            pluginPath.mkdirs();
        }

        //Create Connection
        File dbFile = new File(pluginPath, "data.db");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        log.info("Database connected");
    }

    /**
     * Call on Shutdown to impede Locks
     * @throws SQLException
     */
    public void close() throws SQLException {
        connection.close();
    }
    //##################################################################################################################
    //################################################CRUD##############################################################
    //##################################################################################################################

    /**
     *  Select Row/Rows
     * @param table     users
     * @param columns   ["id", "name", "age"]
     * @param condition ["age > 18", "name is not null"]
     * @param sort      ["name desc", "age asc"]
     * @return ResultSet
     */
    public ReturnSet select(String table, String[] columns, String[] condition, String[] sort) {
        ReturnSet returnSet = new ReturnSet();
        StringBuilder sql = new StringBuilder("SELECT ");

        // Add columns
        if (columns == null || columns.length == 0) {
            sql.append("*");
        } else {
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]);
                if (i < columns.length - 1) {
                    sql.append(", ");
                }
            }
        }

        sql.append(" FROM ").append(table);

        // Add WHERE conditions
        if (condition != null && condition.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < condition.length; i++) {
                sql.append(condition[i]);
                if (i < condition.length - 1) {
                    sql.append(" AND ");
                }
            }
        }

        // Add ORDER BY
        if (sort != null && sort.length > 0) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < sort.length; i++) {
                sql.append(sort[i]);
                if (i < sort.length - 1) {
                    sql.append(", ");
                }
            }
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            var meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = meta.getColumnName(i);
                    String value = rs.getString(i);
                    row.put(columnName, value);
                }
                returnSet.addRow(row);
            }

            returnSet.setError(ReturnSet.ReturnErrors.NONE);

        } catch (SQLException e) {
            log.severe("Error executing SELECT query: " + e.getMessage());
            returnSet.setError(ReturnSet.ReturnErrors.UNKNOWN, e);
        }
        return returnSet;
    }

    /**
     * Insert one Row
     * @param table users
     * @param data  [id=1, name=Smith, age=30, ...]
     * @return ResultSet
     */
    public ReturnSet insert(String table, InsertRow data) {
        ReturnSet returnSet = new ReturnSet();
        if (data == null || data.isEmpty()) {
            log.severe("No data provided for insert operation");
            returnSet.setError(ReturnSet.ReturnErrors.NO_DATA);
            return returnSet;
        }

        String[] columns = data.getColumns();
        String[] values = data.getValues();
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");

        //Columns
        StringJoiner columnJoin = new StringJoiner(", ");
        for (String col : columns) {
            columnJoin.add(col);
        }

        sql.append(columnJoin).append(") VALUES (");

        //Placeholder
        for (int i = 0; i < data.size(); i++) {
            sql.append("?");
            if(i < data.size() - 1){
                sql.append(", ");
            }
        }

        sql.append(")");

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            int paramIndex = 1;

            //Set Value
            for (String val : values) {
                pstmt.setString(paramIndex++, val);
            }

            returnSet.setChangedRows(pstmt.executeUpdate());
            returnSet.setError(ReturnSet.ReturnErrors.NONE);
        } catch (SQLException e) {
            log.severe("Error executing INSERT query: " + e.getMessage());
            returnSet.setError(ReturnSet.ReturnErrors.UNKNOWN, e);
        }
        return returnSet;
    }

    /**
     *  Update Rows under the defined Condition
     * @param table     users
     * @param data      [name=Smith, age=31, ...]
     * @param condition ["age<50", "id=1"]
     * @return ResultSet
     */
    public ReturnSet update(String table, UpdateRow data, String[] condition) {
        ReturnSet returnSet = new ReturnSet();
        if (data == null || data.isEmpty()) {
            log.severe("No data provided for update operation");
            returnSet.setError(ReturnSet.ReturnErrors.NO_DATA);
            return returnSet;
        }

        String[] columns = data.getColumns();
        String[] values = data.getValues();
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");


        //Columns
        StringJoiner columnJoin = new StringJoiner(", ");
        for (String col : columns) {
            columnJoin.add(col+ " = ?");
        }
        sql.append(columnJoin);

        // Add WHERE conditions
        if (condition != null && condition.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < condition.length; i++) {
                sql.append(condition[i]);
                if (i < condition.length - 1) {
                    sql.append(" AND ");
                }
            }
        }

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            int paramIndex = 1;

            //Set Value
            for (String val : values) {
                pstmt.setString(paramIndex++, val);
            }

            returnSet.setChangedRows(pstmt.executeUpdate());
            returnSet.setError(ReturnSet.ReturnErrors.NONE);
        } catch (SQLException e) {
            log.severe("Error executing UPDATE query: " + e.getMessage());
            returnSet.setError(ReturnSet.ReturnErrors.UNKNOWN, e);
        }
        return returnSet;
    }

    /**
     * Delete Rows under the defined Condition
     * @param table     users
     * @param condition ["age > 150", "name != null"]
     * @return ResultSet
     */
    public ReturnSet delete(String table, String[] condition) {
        ReturnSet returnSet = new ReturnSet();
        StringBuilder sql = new StringBuilder("DELETE FROM ")
                .append(table);

        // Add WHERE conditions
        if (condition != null && condition.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < condition.length; i++) {
                sql.append(condition[i]);
                if (i < condition.length - 1) {
                    sql.append(" AND ");
                }
            }
        } else {
            log.warning("Executing DELETE without WHERE condition on table: " + table);
        }

        try {
            Statement stmt = connection.createStatement();
            returnSet.setChangedRows(stmt.executeUpdate(sql.toString()));
            returnSet.setError(ReturnSet.ReturnErrors.NONE);
        } catch (SQLException e) {
            log.severe("Error executing DELETE query: " + e.getMessage());
            returnSet.setError(ReturnSet.ReturnErrors.UNKNOWN, e);
        }
        return returnSet;
    }

    //##################################################################################################################
    //################################################DDL###############################################################
    //##################################################################################################################

    /**
     * Doesn't create Table when Table exists, even if Tableschema is different
     * @param conf
     */
    public void createTable(TableConfig conf){

        String tableName = conf.getTableName();

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        List<Map<String, String>> columns = conf.getTableColumns();
        String[] primaryKeys = conf.getPrimaryKeys();

        for (int i = 0; i < columns.size(); i++) {
            Map<String, String> col = columns.get(i);
            sql.append(col.get("name")).append(" ").append(col.get("type"));
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        // Primary Key Definition
        if (primaryKeys != null && primaryKeys.length > 0) {
            sql.append(", PRIMARY KEY (");
            sql.append(String.join(", ", primaryKeys));
            sql.append(")");
        }

        sql.append(");");


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
            log.info(ANSI_GREEN + "Created/Checked Table: "+tableName + ANSI_RESET);
        }catch (SQLException e){
            log.severe("Error creating Table: " + e.getMessage());
        }
    }

    public void deleteTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName + ";";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info(ANSI_RED + "Deleted Table: " + tableName + ANSI_RESET);
        } catch (SQLException e) {
            log.severe("Error deleting Table: " + e.getMessage());
        }
    }

    /**
     * Migrates an existing table to match the given schema.
     * If differences are detected (columns or primary keys), the table is rebuilt and data migrated.
     *
     * @param conf Configuration for the table schema.
     */
    public void migrateTable(TableConfig conf) {
        String tableName = conf.getTableName();
        List<Map<String, String>> newColumns = conf.getTableColumns();
        Set<String> newPrimaryKeys = new HashSet<>(Arrays.asList(Optional.ofNullable(conf.getPrimaryKeys()).orElse(new String[0])));

        try {
            log.info(ANSI_PURPLE + "Start Migration" + ANSI_RESET);
            Map<String, String> existingColumns = getExistingColumns(tableName);
            Set<String> existingPrimaryKeys = getExistingPrimaryKeys(tableName);

            if (schemasMatch(existingColumns, existingPrimaryKeys, newColumns, newPrimaryKeys)) {
                log.info(ANSI_PURPLE + "Table '" + tableName + "' has no schema changes. Skipping migration." + ANSI_RESET);
                return;
            }

            List<String> commonColumns = newColumns.stream()
                    .map(col -> col.get("name"))
                    .filter(existingColumns::containsKey)
                    .toList();

            String backupTable = "tmp_backup_" + tableName + "_" + System.currentTimeMillis();

            if (!commonColumns.isEmpty()) {
                createBackupTable(tableName, backupTable, commonColumns);
            }

            deleteTable(tableName);
            createTable(conf);

            if (!commonColumns.isEmpty()) {
                restoreDataFromBackup(tableName, backupTable, commonColumns);
            }

            log.info(ANSI_PURPLE + "Table '" + tableName + "' successfully migrated." + ANSI_RESET);

        } catch (SQLException e) {
            log.severe("Migration failed for table '" + tableName + "': " + e.getMessage());
        }
    }

    //##################################################################################################################
    //################################################Schema############################################################
    //##################################################################################################################

    /**
     *  Load Schema into Java and Database
     * @param jsonPath
     * @param jsonName
     */
    public void loadSchema(String jsonPath, String jsonName) {
        try {
            File jsonFilePath = new File(jsonPath, jsonName);
            if (!jsonFilePath.exists()) {
                try {
                    jsonFilePath.getParentFile().mkdirs();
                    jsonFilePath.createNewFile();
                    schema = new HashMap<>();
                    log.warning("Schema file created. Add a schema and reload");
                    return;
                } catch (Exception e) {
                    log.severe("Error creating schema: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            if (jsonFilePath.length() == 0) {
                schema = new HashMap<>();
                log.warning("Schema file is empty. Add a schema and reload");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            schema = mapper.readValue(jsonFilePath, Map.class);
            log.info("Schema loaded");
            createSchema();
        } catch (IOException e) {
            log.severe("Error loading schema: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void createSchema(){
        if(schema == null){
            log.severe("Schema is Empty or isn't loaded correctly");
            return;
        }
        for (Map.Entry<String, Map<String, Object>> entry : schema.entrySet()) {
            //Load Table/Columns
            String tableKey = entry.getKey();
            Map<String, Object> tableDef = entry.getValue();
            TableConfig conf = new TableConfig((String) tableDef.get("table"));

            // Set columns
            conf.addColumnList((List<Map<String, String>>) tableDef.get("columns"));

            // Extract primary_keys and set them
            List<String> primaryKeyList = (List<String>) tableDef.get("primary_keys");
            if (primaryKeyList != null) {
                String[] primaryKeys = primaryKeyList.toArray(new String[0]);
                conf.setPrimary(primaryKeys);
            }
            createTable(conf);
            migrateTable(conf);
        }
    }

    //##################################################################################################################
    //###########################################Utils##################################################################
    //##################################################################################################################

    /**
     * Get all Columns from a Table(live Database)
     * @param tableName
     * @return
     * @throws SQLException
     */
    private Map<String, String> getExistingColumns(String tableName) throws SQLException {
        Map<String, String> columns = new LinkedHashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                columns.put(rs.getString("name"), rs.getString("type").toUpperCase());
            }
        }
        return columns;
    }

    /**
     * Get all PK froms a Table(live Database)
     * @param tableName
     * @return
     * @throws SQLException
     */
    private Set<String> getExistingPrimaryKeys(String tableName) throws SQLException {
        Set<String> primaryKeys = new LinkedHashSet<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (rs.getInt("pk") > 0) {
                    primaryKeys.add(rs.getString("name"));
                }
            }
        }
        return primaryKeys;
    }

    /**
     * Checks if Schema = DbTable
     * @param existingCols
     * @param existingPKs
     * @param newCols
     * @param newPKs
     * @return
     */
    private boolean schemasMatch(Map<String, String> existingCols, Set<String> existingPKs,
                                 List<Map<String, String>> newCols, Set<String> newPKs) {
        if (existingCols.size() != newCols.size()) return false;

        for (Map<String, String> col : newCols) {
            String name = col.get("name");
            String type = col.get("type").toUpperCase();
            if (!type.equals(existingCols.getOrDefault(name, ""))) return false;
        }

        return existingPKs.equals(newPKs);
    }

    /**
     * Creates Backup Table
     * @param tableName
     * @param backupName
     * @param columns
     */
    private void createBackupTable(String tableName, String backupName, List<String> columns) {
        String columnList = String.join(", ", columns);
        String sql = "CREATE TEMP TABLE " + backupName + " AS SELECT " + columnList + " FROM " + tableName;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Backup table '" + backupName + "' created.");
        } catch (SQLException e) {
            log.warning("Backup creation failed: " + e.getMessage());
        }
    }

    /**
     * Insertet Data from backupName to tableName
     * @param tableName
     * @param backupName
     * @param columns
     */
    private void restoreDataFromBackup(String tableName, String backupName, List<String> columns) {
        String columnList = String.join(", ", columns);
        String sql = "INSERT INTO " + tableName + " (" + columnList + ") SELECT " + columnList + " FROM " + backupName;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Data restored to new table from backup.");
        } catch (SQLException e) {
            log.warning("Failed to restore data: " + e.getMessage());
        }
    }
    //##################################################################################################################
    //###########################################Addition-Interfaces####################################################
    //##################################################################################################################

    //###############################################CRUD###############################################################

    /**
     * Select without order
     * @param table
     * @param columns
     * @param condition
     * @return ReturnSet
     */
    public ReturnSet select(String table, String[] columns, String[] condition) {
        return select(table, columns, condition, null);
    }

    /**
     * Select without order and condition
     * @param table
     * @param columns
     * @return ReturnSet
     */
    public ReturnSet select(String table, String[] columns) {
        return select(table, columns, null, null);
    }

    /**
     * Select without order, condition and column-selection
     * @param table
     * @return ReturnSet
     */
    public ReturnSet select(String table) {
        return select(table, null, null, null);
    }

    /**
     * Update without Condition
     * Warning! Updates all defined Columns in a Table
     * @param table
     * @param data
     * @return
     */
    public ReturnSet update(String table, UpdateRow data) {
        return update(table, data, null);
    }

    /**
     * Insert multiple Rows
     * @param table
     * @param rows
     * @return
     */
    public ReturnSet insert(String table, InsertMultiRows rows){
        ReturnSet returnSet = new ReturnSet();
        int insertedCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            InsertRow row = rows.get(i);
            ReturnSet tmpReturnSet = insert(table, row);
            if(tmpReturnSet.getError() == ReturnSet.ReturnErrors.NONE){
                insertedCount++;
            }
        }

        returnSet.setChangedRows(insertedCount);
        if(insertedCount != rows.size()){
            returnSet.setError(ReturnSet.ReturnErrors.PARTLY_INSERT);
        }

        return returnSet;
    }

    //#############################################Schema###############################################################

    /**
     * Load Default Schema
     */
    public void loadSchema(){
        loadSchema(pluginPath.getPath() , SCHEMA_DEFAULT);
    }

    /**
     * Load Custom Named Schema in defined Path
     * @param jsonName
     */
    public void loadSchema(String jsonName){
        loadSchema(pluginPath.getPath() , jsonName);
    }
}
