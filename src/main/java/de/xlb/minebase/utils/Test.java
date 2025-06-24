package de.xlb.minebase.utils;

import de.xlb.minebase.api.MineDb;
import de.xlb.minebase.types.*;
import static de.xlb.minebase.utils.Builder.*;
import java.io.File;
import java.util.logging.Logger;

/**
 * Testclass, Serves simultaneously as an Example
 */
public class Test {
    private final String TEST_TABLE = "test_table_minebase";
    // ANSI Escape Codes Colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";

    private final MineDb api;
    private final Logger log;
    private final File pluginPath;

    public Test(Logger log, File pluginPath){
        try {
            this.api = new MineDb(log, pluginPath);
        } catch (Exception e) {
            log.severe("Database couldn't be initialized");
            throw new RuntimeException(e);
        }

        this.log = log;
        this.pluginPath = pluginPath;

        createTestSchema();

        run(schema(), "schema");
        run(insert(), "insert");
        run(update(), "update");
        run(select(), "select");
        run(selectSum(), "selectSum");
        run(delete(), "delete");

        deleteTestSchema();
    }

    public Boolean schema(){
        try{
            api.loadSchema();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean insert(){
        try{
            //Test Insert Simple Row
            InsertRow row = new InsertRow();
            row.put("uuid", "1");
            row.put("name", "Test_1");
            row.put("progress", "10");
            ReturnSet returnValSimple = api.insert(TEST_TABLE,row);

            if(returnValSimple.getChangedRows() != 1){
                log.severe("Simple Row Insert Error");
                return false;
            }

            //Test Insert Multi Row
            InsertMultiRows multiRows = new InsertMultiRows();
            InsertRow row1 = new InsertRow();
            row1.put("uuid", "2");
            row1.put("name", "Test_2");
            row1.put("progress", "20");
            multiRows.add(row1);

            InsertRow row2 = new InsertRow();
            row2.put("uuid", "3");
            row2.put("name", "Test_3");
            row2.put("progress", "30");
            multiRows.add(row2);

            InsertRow row3 = new InsertRow();
            row3.put("uuid", "4");
            row3.put("name", "Test_4");
            row3.put("progress", "40");
            multiRows.add(row3);

            ReturnSet returnVal = api.insert(TEST_TABLE,multiRows);

            return returnVal.getChangedRows() == 3;
        } catch (Exception e) {
            log.severe(e.toString());
            return false;
        }
    }

    public Boolean update(){
        try{
            UpdateRow updateData = new UpdateRow();
            updateData.put("uuid", "33");
            updateData.put("name", "Test_3_Updated");
            updateData.put("progress", "30.3");
            ReturnSet returnVal = api.update(TEST_TABLE, updateData, cond("uuid = 3"));
            return returnVal.getChangedRows() == 1;
        } catch (Exception e) {
            log.severe(e.toString());
            return false;
        }

    }

    public Boolean delete(){
        try{
            ReturnSet returnVal = api.delete(TEST_TABLE, cond("uuid = 2"));
            return returnVal.getChangedRows() == 1;
        } catch (Exception e) {
            log.severe(e.toString());
            return false;
        }
    }

    public Boolean select(){
        try{
            ReturnSet rs =  api.select(TEST_TABLE, col("uuid","name", "progress"), cond("uuid > 3"), sort("uuid desc"));
            if(!rs.getColumn(0,"uuid").equalsIgnoreCase("33")){
                return false;
            }

            if(!rs.getColumn(0,"name").equalsIgnoreCase("Test_3_Updated")){
                return false;
            }

            if(!rs.getColumn(0,"progress").equalsIgnoreCase("30.3")){
                return false;
            }

            if(!rs.getColumn(1,"uuid").equalsIgnoreCase("4")){
                return false;
            }

            if(!rs.getColumn(1,"name").equalsIgnoreCase("Test_4")){
                return false;
            }

            return rs.getColumn(1, "progress").equalsIgnoreCase("40.0");
        }catch (Exception e){
            log.severe(e.toString());
            return false;
        }
    }

    public Boolean selectSum(){
        try{
            ReturnSet rs =  api.select(TEST_TABLE, col("sum(progress) as sum_progress"));
            return rs.getColumn(0, "sum_progress").equalsIgnoreCase("100.3");
        }catch (Exception e){
            log.severe(e.toString());
            return false;
        }
    }

    private void run(Boolean test, String toTest){
        if(test){
            log.info(ANSI_GREEN + "SUCCESS: " + toTest + ANSI_RESET);
        }else{
            log.severe("FAILED: "+ toTest);
        }
    }

    private void createTestSchema(){
        TableConfig conf = new TableConfig(TEST_TABLE);
        conf.addColumn("uuid", TableConfig.ColTypes.INTEGER);
        conf.addColumn("name", TableConfig.ColTypes.TEXT);
        conf.addColumn("progress", TableConfig.ColTypes.REAL);
        conf.setPrimary("uuid");
        api.createTable(conf);
    }

    private void deleteTestSchema(){
        api.deleteTable(TEST_TABLE);
    }
}