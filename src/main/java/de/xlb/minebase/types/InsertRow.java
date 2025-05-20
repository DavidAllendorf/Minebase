package de.xlb.minebase.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to define Insert Row
 */
public class InsertRow {
    private final Map<String, String> insertMap = new HashMap<>();

    /**
     * Define Columnname and Value
     * @param colName
     * @param colValue
     */
    public void put(String colName, String colValue){
        insertMap.put(colName, colValue);
    }

    /**
     * Gets all Columns
     * @return String[]
     */
    public String[] getColumns(){
        return insertMap.keySet().toArray(new String[0]);
    }

    /**
     * Get all Values
     * @return String[]
     */
    public String[] getValues(){
        return insertMap.values().toArray(new String[0]);
    }

    /**
     * Is Insert Row empty
     * @return
     */
    public boolean isEmpty() {
        return insertMap.isEmpty();
    }

    /**
     * Count of Columns
     * @return
     */
    public Integer size() {
        return insertMap.size();
    }
}
