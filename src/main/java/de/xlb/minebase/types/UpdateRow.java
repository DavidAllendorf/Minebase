package de.xlb.minebase.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Define Update
 */
public class UpdateRow {
    private final Map<String, String> updateMap = new HashMap<>();

    /**
     * Add Column and Value to Update
     * @param colName
     * @param colValue
     */
    public void put(String colName, String colValue){
        updateMap.put(colName, colValue);
    }

    /**
     * Get Columnnames to update
     * @return String[]
     */
    public String[] getColumns(){
        return updateMap.keySet().toArray(new String[0]);
    }

    /**
     * Get Columnvalues to update
     * @return String[]
     */
    public String[] getValues(){
        return updateMap.values().toArray(new String[0]);
    }

    /**
     * Is Update Empty
     * @return boolean
     */
    public boolean isEmpty() {
        return updateMap.isEmpty();
    }

    /**
     * Size of Updatemap
     * @return Integer
     */
    public Integer size() {
        return updateMap.size();
    }
}
