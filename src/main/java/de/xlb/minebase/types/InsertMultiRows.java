package de.xlb.minebase.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for Multiple Insert Rows
 */
public class InsertMultiRows {
    private final List<InsertRow> insertList = new ArrayList<>();

    /**
     * Add Row to Insert List
     * @param row
     */
    public void add(InsertRow row) {
        insertList.add(row);
    }

    /**
     * Return selected Row
     * @param row
     * @return InsertRow
     */
    public InsertRow get(int row) {
        return insertList.get(row);
    }

    /**
     * Count of Rows
     * @return int
     */
    public int size() {
        return insertList.size();
    }

    /**
     * Is List empty
     * @return boolean
     */
    public boolean isEmpty() {
        return insertList.isEmpty();
    }
}
