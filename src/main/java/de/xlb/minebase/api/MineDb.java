package de.xlb.minebase.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.xlb.minebase.types.ChangeData;
import de.xlb.minebase.types.InsertList;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MineDb {
    //Const
    private final String SCHEMA_DEFAULT = "schema_default.json";

    //Vars
    private final File pluginPath;
    private final Connection connection;
    private final Logger log;
    private Map<String, Map<String, Object>> schema;


    public MineDb(Connection connection, Logger log, File pluginPath) {
        this.connection = connection;
        this.log = log;
        this.pluginPath = pluginPath;
    }

    public void close() throws SQLException {
        connection.close();
    }
    //##################################################################################################################
    //################################################CRUD##############################################################
    //##################################################################################################################

    /**
     *
     * @param table     letters
     * @param columns   ["a", "b", "c"]
     * @param condition ["a > 0", "b is null"]
     * @param sort      ["b desc", "a asc"]
     * @return ResultSet
     */
    public ResultSet select(String table, String[] columns, String[] condition, String[] sort) {
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

        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(sql.toString());
        } catch (SQLException e) {
            log.severe("Error executing SELECT query: " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * @param table       letters
     * @param columnsData [{"name", "A"}, {"name", "B"}]
     * @return ResultSet
     */
    public ResultSet create(String table, InsertList columnsData) {
        if (columnsData == null || columnsData.isEmpty()) {
            log.severe("No data provided for insert operation");
            return null;
        }

        // Get column names and values from the first map
        ChangeData firstRow = columnsData.get(0);
        List<String> columnNames = new ArrayList<>(firstRow.keySet());

        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(table)
                .append(" (");

        // Add column names
        for (int i = 0; i < columnNames.size(); i++) {
            sql.append(columnNames.get(i));
            if (i < columnNames.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(") VALUES ");

        // Add placeholders for each row
        for (int i = 0; i < columnsData.size(); i++) {
            sql.append("(");
            for (int j = 0; j < columnNames.size(); j++) {
                sql.append("?");
                if (j < columnNames.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");

            if (i < columnsData.size() - 1) {
                sql.append(", ");
            }
        }

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

            // Set values for each row
            int paramIndex = 1;
            for (ChangeData row : columnsData) {
                for (String column : columnNames) {
                    pstmt.setString(paramIndex++, row.get(column));
                }
            }

            pstmt.executeUpdate();
            return pstmt.getGeneratedKeys();
        } catch (SQLException e) {
            log.severe("Error executing INSERT query: " + e.getMessage());
            return null;
        }
    }

    /**
     *  Table update
     *  TODO: MultiUpdates
     * @param table     letters
     * @param data      ["name", "updtA"]
     * @param condition ["a > 0", "b is null"]
     * @return ResultSet
     */
    public ResultSet update(String table, ChangeData data, String[] condition) {
        if (data == null || data.isEmpty()) {
            log.severe("No data provided for update operation");
            return null;
        }

        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(table)
                .append(" SET ");

        // Add column=value pairs
        List<String> columns = new ArrayList<>(data.keySet());
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i)).append(" = ?");
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

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

            // Set values for each column
            for (int i = 0; i < columns.size(); i++) {
                pstmt.setString(i + 1, data.get(columns.get(i)));
            }

            pstmt.executeUpdate();
            return pstmt.getGeneratedKeys();
        } catch (SQLException e) {
            log.severe("Error executing UPDATE query: " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * @param table     letters
     * @param condition ["a > 0", "b is null"]
     * @return ResultSet
     */
    public ResultSet delete(String table, String[] condition) {
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
            stmt.executeUpdate(sql.toString());
            return null;
        } catch (SQLException e) {
            log.severe("Error executing DELETE query: " + e.getMessage());
            return null;
        }
    }

    //##################################################################################################################
    //################################################DDL###############################################################
    //##################################################################################################################
    public void createTable(String tableName, List<Map<String, String>> columns){

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        for (int i = 0; i < columns.size(); i++) {
            Map<String, String> col = columns.get(i);
            sql.append(col.get("name")).append(" ").append(col.get("type"));
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(");");

        // Ausführen
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
            log.info("Created Table: "+tableName);
        }catch (SQLException e){
            log.severe("Error creating Table: " + e.getMessage());
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
    public void loadSchema(String jsonPath, String jsonName){
        try {
            File jsonFilePath = new File(jsonPath, jsonName);
            if (!jsonFilePath.exists()) {
                try {
                    jsonFilePath.getParentFile().mkdirs();
                    jsonFilePath.createNewFile();
                } catch (Exception e) {
                    log.severe("Error creating schema: " + e.getMessage());
                    throw new RuntimeException(e);
                }
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
            createTable((String) tableDef.get("table"), (List<Map<String, String>>) tableDef.get("columns"));
        }
    }

    //##################################################################################################################
    //###########################################Addition-Interfaces####################################################
    //##################################################################################################################

    //###############################################CRUD###############################################################
    public ResultSet select(String table, String[] columns, String[] condition) {
        return select(table, columns, condition, null);
    }

    public ResultSet select(String table, String[] columns) {
        return select(table, columns, null, null);
    }

    public ResultSet select(String table) {
        return select(table, null, null, null);
    }

    public ResultSet update(String table, ChangeData data) {
        return update(table, data, null);
    }

    //#############################################Schema###############################################################
    public void loadSchema(){
        loadSchema(pluginPath.getPath() , SCHEMA_DEFAULT);
    }

    public void loadSchema(String jsonName){
        loadSchema(pluginPath.getPath() , jsonName);
    }
}
