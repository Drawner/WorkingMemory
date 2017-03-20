package com.gtfp.workingmemory.db;

import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Drawn on 9/26/2016.
 */

public interface dbInterface{

    dbInterface open();

    boolean isOpen();

    void close();

    boolean createCurrentRecs();

 //** Not even used.
//    dbInterface getDatabase();

    void clearResultSet();

   Cursor getResultSet();

    boolean showDeleted(boolean showDeleted);

    Cursor runQuery(String sqlStmt);

    Cursor getRecs();

    Cursor getCurrentRecs();

    Cursor getDeletedRecs();

    <T> T getLastRowID();

    ArrayList<ToDoItem> ToDoList(ArrayList<HashMap<String, String>> recs);

    ArrayList<HashMap<String, String>> getDataArrayList();

    int deleteRec(Long rowID);

    boolean save(ToDoItem itemToDo);

    boolean markRec(Long rowID);

    int updateRec(ToDoItem itemToDo);

    int updateRec(ContentValues recValues, long id);

    long insertRec(ContentValues recValues);

    boolean createEmptyTemp();

    long insertTempRec();

    Cursor getNewTempRecs();

    boolean ifNewRec();

    void bindRecValues(String columnName, String value);

    boolean importRec();

    Cursor getRecs(String whereClause);

    Cursor getRecord(int rowId);

    void onDestroy();

}
