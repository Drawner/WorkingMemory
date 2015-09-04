package com.gtfp.workingmemory.db;

import com.gtfp.workingmemory.todo.*;
import com.gtfp.errorhandler.ErrorHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Drawn on 2015-02-10.
 */
public class dbHelper extends SQLiteOpenHelper {

    private SQLiteDatabase mDB;

    private Context mContext;

    private Cursor mResultSet;

    private ContentValues mRecValues;

    private boolean mUseView;

    static final String DATABASE_NAME = "working";

    static final String DATABASE_FILE = DATABASE_NAME + ".db";

    static final String DBKEY_FIELD = "rowid";

    static final int DATABASE_VERSION = 13;

    // Ported to the file, create.sql
    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME
            + "(ToDoItem VARCHAR, ToDoDateTime VARCHAR, ToDoDateTimeEpoch Long, ToDoTimeZone VARCHAR, ToDoReminderEpoch Long, ToDoReminderChk integer default 0, ToDoLEDColor integer default 0, ToDoFired integer default 0, deleted integer default 0);";

    // SQL statement used to upgrade the database.
    private final String ALTER_TABLE = "ALTER TABLE " + DATABASE_NAME
            + " ADD COLUMN ToDoFired integer default 0;";

    private final String SELECT_ALL = "SELECT " + DBKEY_FIELD + " AS _id, * FROM " + DATABASE_NAME;
            //  + " ORDER BY ToDoDateTimeEpoch ASC"

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + DATABASE_NAME;

    private static final String CREATE_NOT_DELETED =
            "CREATE TEMP VIEW IF NOT EXISTS temp.notdeleted AS SELECT " + DBKEY_FIELD + " AS _id, * FROM " + DATABASE_NAME
                    + " WHERE deleted = 0";

    private static final String CREATE_DELETED =
            "CREATE TEMP VIEW IF NOT EXISTS temp.deleted AS SELECT " + DBKEY_FIELD + " AS _id, * FROM " + DATABASE_NAME
                    + " WHERE deleted = 1";

    private static final String DROP_DELETED = "DROP VIEW IF EXISTS temp.deleted";

    private static final String SELECT_NOT_DELETED = "SELECT " + DBKEY_FIELD + " AS _id, * FROM temp.notdeleted";

    private static final String SELECT_DELETED = "SELECT " + DBKEY_FIELD + " AS _id, * FROM temp.deleted";



    public dbHelper(Context controller) {
        super(controller, DATABASE_FILE, null, DATABASE_VERSION);

        mContext = controller;

        // Stores the record's contents.
        mRecValues = new ContentValues();
    }


    public boolean createCurrentRecs() {

        try {

            mDB.execSQL(CREATE_NOT_DELETED);

            mUseView = true;

        } catch (SQLException ex) {

            return false;
        }

        return true;
    }


    public dbHelper open() {

        try {

            if (!isOpen()){

                mDB = getWritableDatabase();
            }

        } catch (SQLException ex) {

            if (mDB != null && mDB.isOpen()) {

                mDB.close();
            }

            if (mDB != null) {

                mDB = null;
            }
        }

        return this;
    }


    public boolean isOpen() {

        return mDB != null && mDB.isOpen();
    }


    public void close() {
        super.close();

        // It's good to lose the reference here with the connection closed.
        mDB = null;

        // This resource too. It could be huge!
        mResultSet = null;
    }


    public SQLiteDatabase getDatabase() {

        return mDB;
    }


    public void clearResultSet() {

        mResultSet = null;
    }


    public Cursor getResultSet() {

        if (mResultSet == null) {

            if (mUseView){

                mResultSet = getCurrentRecs();
            }else {

                mResultSet = getRecs();
            }
        }

        return mResultSet;
    }


    public boolean showDeleted(boolean showDeleted){

        // Show deleted records means NOT to use the view.
        mUseView = !showDeleted;

        return showDeleted;
    }


    public Cursor runQuery(String sqlStmt) {

        Cursor records;

        try {

            records = mDB.rawQuery(sqlStmt, null);

        } catch (RuntimeException ex) {

            // If something goes wrong, return an empty cursor.
            records = new MatrixCursor(new String[]{"empty"});
        }

        return records;
    }


    public Cursor getRecs() {

        return runQuery(SELECT_ALL);
    }

    public Cursor getCurrentRecs(){

        return runQuery(SELECT_NOT_DELETED);
    }

    public Cursor getDeletedRecs(){

        return runQuery(SELECT_DELETED);
    }


    public long getLastRowID() {

        Cursor records;

        final String SELECT_LASTROWID = "SELECT " + DBKEY_FIELD + " AS _id, * FROM " + DATABASE_NAME
                + " ORDER BY " + DBKEY_FIELD + " DESC LIMIT 1";

        records = runQuery(SELECT_LASTROWID);

        long lastRowID = 0;

        // You've got to move the cursor's position pointer after the query.
        if (records.moveToFirst()) {

            try {

                lastRowID = records.getLong(0);

            } catch (RuntimeException ex) {

                lastRowID = 0;
            }
        }

        return lastRowID;
    }


    public ArrayList<ToDoItem> ToDoList() {

        ToDoItem todoItem;

        ArrayList<ToDoItem> todoList = new ArrayList<ToDoItem>();

        clearResultSet();

        ArrayList<HashMap<String, String>> todoItems = recArrayList();

        if (todoItems.size() == 0){

          return todoList;
        }

        HashMap<String, String> rec = todoItems.get(0);

        Set<String> keys = rec.keySet();

        boolean id, item, dtEpoch, rmEpoch, rmChk, LEDColor, hasFired, setAlarm, deleted;

        for (HashMap<String, String> todoRec : todoItems) {

            todoItem = new ToDoItem();

            id = item = dtEpoch = rmEpoch = rmChk = LEDColor = hasFired = setAlarm = deleted = true;

            for (String key : keys) {

                if (id && key.equals("_id")) {

                    todoItem.setId(todoRec.get(key));

                    id = false;
                } else if (item && key.equals("ToDoItem")) {

                    todoItem.setItemName(todoRec.get(key));

                    item = false;
                } else if (dtEpoch && key.equals("ToDoDateTimeEpoch")) {

                    todoItem.setDueDate(Long.valueOf(todoRec.get(key)));

                    dtEpoch = false;
                } else if (rmEpoch && key.equals("ToDoReminderEpoch")) {

                    todoItem.setReminderEpoch(Long.valueOf(todoRec.get(key)));

                    rmEpoch = false;
                } else if (rmChk && key.equals("ToDoReminderChk")) {

                    todoItem.setReminderChk(Integer.valueOf(todoRec.get(key)));

                    rmChk = false;
                } else if (LEDColor && key.equals("ToDoLEDColor")) {

                    todoItem.setLEDColor(Integer.valueOf(todoRec.get(key)));

                    LEDColor = false;
                } else if (hasFired && key.equals("ToDoFired")) {

                    todoItem.hasFired(todoRec.get(key).equals("1"));

                    hasFired = false;
                } else if (setAlarm && key.equals("ToDoSetAlarm")) {

                    todoItem.setAlarm(todoRec.get(key).equals("1"));

                    setAlarm = false;
                } else if (deleted && key.equals("deleted")) {

                    todoItem.isDeleted(todoRec.get(key).equals("1"));

                    deleted = false;
                }
            }

            todoList.add(todoItem);
        }

        return todoList;
    }


    public ArrayList<HashMap<String, String>> recArrayList() {

        Cursor recs = getResultSet();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        while (recs.moveToNext()) {

            HashMap<String, String> row = new HashMap<String, String>();

            //iterate over the columns
            for (int col = 0; col < recs.getColumnNames().length; col++) {

                row.put(recs.getColumnName(col), recs.getString(col));
            }

            list.add(row);
        }

        return list;
    }


    public int deleteRec(Long rowID) {

        String id = Long.toString(rowID);

        String where = DBKEY_FIELD + "=?";

        try {

            return mDB.delete(DATABASE_NAME, where, new String[]{id});

        } catch (RuntimeException ex) {

            ErrorHandler.logError(ex);

            return -1;
        }
    }


    public boolean markRec(Long rowID) {

        String id = Long.toString(rowID);

        String sql = "UPDATE OR FAIL " + DATABASE_NAME + " SET deleted = 1 WHERE " + DBKEY_FIELD + "=" + id;

        try {

            mDB.execSQL(sql);

        } catch (SQLException ex) {

            ErrorHandler.logError(ex);

            return false;
        }

        return true;
    }


    public boolean save(ToDoItem itemToDo) {

        long rowId = itemToDo.getId();

        if (rowId > 0) {

            return updateRec(itemToDo) > 0;

        } else {

            rowId = insertRec(itemToDo);

            if (rowId < 0) {
                // Note, there was an error with a negative number.
                // but will be set to zero in the variable.
                rowId = 0;
            }

            return rowId > 0;
        }
    }


    int updateRec(ToDoItem itemToDo) {

       return updateRec(bindRecValues(itemToDo), itemToDo.getId());
    }

    int updateRec(ContentValues recValues, long id) {
        int result;

        try {

            result = mDB.update(DATABASE_NAME, recValues, DBKEY_FIELD + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (RuntimeException ex) {

            ErrorHandler.logError(ex);

            result = 0;
        }

        return result;
    }


    long insertRec(ToDoItem itemToDo) {

        return insertRec(bindRecValues(itemToDo));
    }


    long insertRec(ContentValues recValues) {

        long result;

        try {

            result = mDB.insert(DATABASE_NAME, null, recValues);

        } catch (RuntimeException ex) {

            result = 0;
        }

        return result;
    }


    public ContentValues bindRecValues(ToDoItem itemToDo) {

        // Assign values for each row.
        mRecValues.put("ToDoItem", itemToDo.getItemName());

        long epochTime = itemToDo.getDueDateInEpoch();

        mRecValues.put("ToDoDateTime", ToDoItem.EpochToDateTime(epochTime));

        mRecValues.put("ToDoDateTimeEpoch", epochTime);

        mRecValues.put("ToDoReminderEpoch", itemToDo.getReminderEpoch());

        mRecValues.put("ToDoReminderChk", itemToDo.getReminderChk());

        mRecValues.put("ToDoLEDColor", itemToDo.getLEDColor());

        mRecValues.put("ToDoFired", itemToDo.hasFired());

        mRecValues.put("ToDoSetAlarm", itemToDo.setAlarm());

        mRecValues.put("deleted", itemToDo.isDeleted());

        return mRecValues;
    }


    public void bindRecValues(String columnName, String value){

        if (columnName.equals("ToDoItem")){

            mRecValues.put(columnName, value);

        } else if (columnName.equals("ToDoDateTime")){

            mRecValues.put(columnName, value);

        } else if (columnName.equals("ToDoDateTimeEpoch")){

            mRecValues.put(columnName, setBindRecLong(value));

        } else if (columnName.equals("ToDoTimeZone")){

            mRecValues.put(columnName, setBindRecInt(value));

        } else if (columnName.equals("ToDoReminderEpoch")){

            mRecValues.put(columnName, setBindRecLong(value));

        } else if (columnName.equals("ToDoReminderChk")){

            mRecValues.put(columnName, setBindRecInt(value));

        } else if (columnName.equals("ToDoLEDColor")){

            mRecValues.put(columnName, setBindRecInt(value));

        } else if (columnName.equals("ToDoFired")){

            mRecValues.put(columnName, setBindRecInt(value) != 0);

        } else if (columnName.equals("ToDoSetAlarm")){

            mRecValues.put(columnName, setBindRecInt(value) != 0);

        } else if (columnName.equals("deleted")){

            mRecValues.put(columnName, setBindRecInt(value) != 0);
        }
    }


    public int setBindRecInt(String value){

       if (value == null || value.isEmpty()) return 0;

        try {

            return Integer.parseInt(value);

        }catch(NumberFormatException ex){

            return 0;
        }
    }


    public long setBindRecLong(String value){

        if (value == null || value.isEmpty()) return 0L;

        try{

            return Long.parseLong(value, 10);

        }catch(NumberFormatException ex){

            return 0L;
        }
    }


    public boolean importRec() {

        return insertRec(mRecValues) > 0;
    }


    public boolean dropTable() {

        boolean dropped;

        try {

            mDB.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);

            dropped = true;

        } catch (SQLException ex) {

            dropped = false;
        }

        return dropped;
    }


    @Override
    public void onConfigure(SQLiteDatabase db) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    }

    protected void execSqlFile(String sqlFile, SQLiteDatabase db ) throws SQLException, IOException {

        int line = 0;

        for( String sqlInstruction : dbSQLParser
                .parseSqlFile(SQL_DIR + "/" + sqlFile, this.mContext.getAssets())) {

            line = line + 1;

            if (line == 1 && sqlInstruction.substring(0, 7).equals("ATTACH ")){

                db.endTransaction();
            }

            db.execSQL(sqlInstruction);
        }
    }

    private static final String SQL_DIR = "sql" ;

    private static final String CREATEFILE = "create.sql";

    private static final String UPGRADEFILE_PREFIX = "upgrade-";

    private static final String UPGRADEFILE_SUFFIX = ".sql";

    // Called when no database exists or if there is a new 'version' indicated.
    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

//            db.execSQL(DATABASE_CREATE);
            execSqlFile(CREATEFILE, db);

        } catch (IOException ex) {

            ErrorHandler.logError(ex);

        } catch (SQLException ex) {

            ErrorHandler.logError(ex);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        try {

            for( String sqlFile : dbSQLParser.list(SQL_DIR, this.mContext.getAssets())) {

                if ( sqlFile.startsWith(UPGRADEFILE_PREFIX)) {

                    int fileVersion = Integer.parseInt(sqlFile.substring( UPGRADEFILE_PREFIX.length(),  sqlFile.length() - UPGRADEFILE_SUFFIX.length()));

                    if ( fileVersion > oldVersion && fileVersion <= newVersion ) {

                        execSqlFile( sqlFile, db );
                    }
                }
            }
        } catch (IOException ex) {

            // TODO Modify ErrorHandler to log the error directly.
            Log.e(mContext.getClass().getSimpleName(),"Database upgrade failed. Version " + oldVersion + " to " + newVersion);

        } catch (RuntimeException ex) {

            // TODO Modify ErrorHandler to log the error directly.
            // TODO Should notify the application the startup failed.
            Log.e(mContext.getClass().getSimpleName(),"Database upgrade failed. Version " + oldVersion + " to " + newVersion);

            // Be sure to throw the exception back up to be handled there as well.
            throw ex;
        }
    }

//    // Called when there is a database version mismatch meaning that the version
//    // of the database needs to be upgraded to the current version.
//    // Upgrade the existing database to conform to the new version. Multiple
//    // previous versions can be handled by comparing _oldVersion and _newVersion.
//    // The simplest case is to drop the old table and create a new one.    @Override
//    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
//
//        if (_oldVersion >= _newVersion) {
//
//            Log.w(mContext.getClass().getSimpleName(),
//                    "Cannot 'upgrade' from version " + _newVersion + " to " + _oldVersion
//                            + ". Upgrade attempt failed.");
//        }
//
//        try {
//
//            _db.execSQL(ALTER_TABLE);
//
//        } catch (RuntimeException ex) {
//            // TODO Modify ErrorHandler to log the error directly.
//            Log.e(mContext.getClass().getSimpleName(),
//                    "Database upgrade failed. Version " + _oldVersion + " to " + _newVersion);
//
//            // Be sure to throw the exception back up to be handled there as well.
//            throw ex;
//        }
//
//        // Log the version upgrade.
//        Log.i(mContext.getClass().getSimpleName(),
//                "Database upgrade. Version " + _oldVersion + " to " + _newVersion);
//    }


    public void onDestroy() {

        close();

        // Just making sure.
        mDB = null;

        mContext = null;

        // This could be huge!
        mResultSet = null;

        mRecValues = null;
    }

}