package com.gtfp.workingmemory.app;

import com.gtfp.workingmemory.db.dbHelper;
import com.gtfp.workingmemory.settings.appSettings;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Drawn on 2015-02-12.
 */
public class appModel {

    private appView mAppView;

    private Context mContext;

    // Variable to hold the database instance
    private dbHelper mDBHelper;

    // An insert of a record will produce the rowid assigned the last record inserted.
    private long mLastRowID = 0;

    private boolean mShowDeleted;


    public appModel(appView mVc) {

        mAppView = mVc;

        mContext = mAppView.mContext;

        mDBHelper = new dbHelper(mContext);
    }


    boolean open() {

        boolean opened = mDBHelper != null && mDBHelper.open().isOpen();

        if (opened) {

            opened = mDBHelper.createCurrentRecs();

            if (!opened) {

                mDBHelper.close();
            }
        }

        return opened;
    }


    void close() {

        mDBHelper.close();
    }


    public long getLastRowID() {

        if (mLastRowID == 0) {

            mLastRowID = mDBHelper.getLastRowID();
        }

        return mLastRowID;
    }


    public boolean save(ToDoItem itemToDo) {

        return mDBHelper.save(itemToDo);
    }


    public boolean delete(ToDoItem itemToDo) {

        itemToDo.isDeleted(true);

        return delete(itemToDo.getId());
    }


    public boolean delete(long id) {

        return mDBHelper.markRec(id);
    }


    public boolean trueDelete(ToDoItem itemToDo){

        return mDBHelper.deleteRec(itemToDo.getId()) > 0;
    }


    public boolean showDeleted() {

        return mShowDeleted;
    }

    boolean showDeleted(boolean showDeleted) {

        mShowDeleted = showDeleted;

        return mShowDeleted;
    }


    ArrayList<ToDoItem> ToDoList() {

        // Determine if deleted records are to be displayed.
        mDBHelper.showDeleted(showDeleted(appSettings.getBoolean("show_deleted", false)));

        return mDBHelper.ToDoList();
    }

    // Returns the database
    SQLiteDatabase getDatabase() {

        return mDBHelper.getDatabase();
    }

    // Returns the records
    Cursor getRecs() {

        return mDBHelper.getRecs();
    }


    void importRec(String columnName, String value){

        mDBHelper.bindRecValues(columnName, value);
    }

    boolean importRec() {

        return mDBHelper.importRec();
    }


    // The App might get destroyed with calling onDestroy, and so take no chances.
    protected void onStop() {

        // close the db connection...
        close();
    }


    protected void onRestart() {

        // db likely closed.
        open();
    }


    protected void onDestroy() {

        mAppView = null;

        mContext = null;

        mDBHelper.onDestroy();

        mDBHelper = null;
    }
}
