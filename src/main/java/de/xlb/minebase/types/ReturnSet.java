package de.xlb.minebase.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReturnSet {
    //#############################################################ERROR###############################################
    private ReturnErrors error = ReturnErrors.TRY;
    private Exception exception = null;

    /**
     * Possible Errors
     */
    public enum ReturnErrors {
        NONE,
        TRY,
        UNKNOWN,
        NO_DATA,
        PARTLY_INSERT,
    }

    /**
     * Returns Custom Error
     * @return ReturnErrors
     */
    public ReturnErrors getError(){
        return this.error;
    }

    /**
     * Set custom Error
     * @param error
     */
    public void setError(ReturnErrors error){
        this.error = error;
    }

    /**
     * Set custom Error and Exception
     * @param error
     * @param exception
     */
    public void setError(ReturnErrors error, Exception exception){
        this.error = error;
        this.exception = exception;
    }

    /**
     * Get Runtime Exception
     * @return Exception
     */
    public Exception getException(){
        return this.exception;
    }

    //###########################################################CHANGED###############################################
    private int changedRows = 0;

    public void setChangedRows(int rows){
        this.changedRows = rows;
    }

    public int getChangedRows(){
        return this.changedRows;
    }

    //###########################################################DATA##################################################
    private List<Map<String, String>> rows = new ArrayList<>();

    /**
     * Adds selected Row
     * @param row
     */
    public void addRow(Map<String, String> row) {
        rows.add(row);
    }

    /**
     * Count of Selected Rows
     * @return int
     */
    public int resultSize() {
        return rows.size();
    }

    /**
     * Gets Row by Index
     * @param index
     * @return Map<String, String>
     */
    public Map<String, String> getRow(int index) {
        if (index < 0 || index >= rows.size()) return null;
        return rows.get(index);
    }

    /**
     * Get Column Value by Index and Name
     * @param rowIndex
     * @param columnName
     * @return String
     */
    public String getColumn(int rowIndex, String columnName) {
        Map<String, String> row = getRow(rowIndex);
        return row != null ? row.get(columnName) : null;
    }
}
