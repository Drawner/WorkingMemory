package com.gtfp.workingmemory.app;

/**
 * Copyright (C) 2015  Greg T. F. Perry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;

import com.gtfp.workingmemory.Auth.Auth;
import com.gtfp.workingmemory.db.dbCloud;
import com.gtfp.workingmemory.db.dbFireBase;
import com.gtfp.workingmemory.db.dbInterface;
import com.gtfp.workingmemory.db.dbSQLlite;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gtfp.workingmemory.settings.appSettings.getBoolean;

public class appModel implements dbCloud.onLoginListener{


    private appView mAppView;

    // Variable to hold the database instance
    private dbInterface mDBHelper;

    private dbInterface m2ndDB;

    // An insert of a record will produce the rowid assigned the last record inserted.
    private long mLastRowID = 0;

    private boolean mShowDeleted;





    appModel(appView mVc){

        mAppView = mVc;

//        mContext = mAppView.getContext();

//        mDBHelper = getDBHelper(mVc);

        mDBHelper = dbSQLlite.getInstance(mAppView.getContext());

        onDBSetup dbSetup = new onDBSetup();

        // Set a listener when the database is first created.
        ((dbSQLlite) mDBHelper).setOnCreateListener(dbSetup);
        ((dbSQLlite) mDBHelper).setOnConfigureListener(dbSetup);

        m2ndDB = dbFireBase.getInstance(mAppView);

        dbCloud.addOnLoginListener(this);
    }



    private dbInterface getDBHelper(appView mVc){

        return dbSQLlite.getInstance(mVc.getContext());
    }



    dbInterface resetDBHelper(){

        if (mAppView == null){ return null; }

        if (mDBHelper != null){

            mDBHelper.onDestroy();

            mDBHelper = null;
        }

        mDBHelper = getDBHelper(mAppView);

        return mDBHelper;
    }



    boolean open(){

        boolean opened = mDBHelper != null && mDBHelper.open().isOpen();

        if (opened){

            opened = mDBHelper.createCurrentRecs();

            if (!opened){

                mDBHelper.close();
            }
        }

        return opened;
    }



    public appView getView(){

        return mAppView;
    }



    public appController getController(){

        return mAppView.getController();
    }



    void close(){

        mDBHelper.close();

        m2ndDB.close();
    }



    public long getLastRowID(){

        if (mLastRowID == 0){

            mLastRowID = mDBHelper.getLastRowID();
        }

        return mLastRowID;
    }



    public boolean save(ToDoItem itemToDo){

        boolean save = mDBHelper.save(itemToDo);

        if (save){

            boolean sync = m2ndDB.isOpen();

            if (sync){

                String key = itemToDo.getKey();

                // New if the key is empty.
                itemToDo.newItem(key.isEmpty());

                sync = m2ndDB.save(itemToDo);

                if (sync){

                    if (key.isEmpty()){

                        mDBHelper.save(itemToDo);
                    }

                    dbCloud.insert(itemToDo.getKey(), "UPDATE");
                }
            }

            if (!sync){

                dbCloud.save(itemToDo.getId(), itemToDo.getKey());
            }
        }
        return save;
    }



    public boolean delete(ToDoItem itemToDo){

        long id = itemToDo.getId();

        boolean delete = delete(id);

        if (delete){

            itemToDo.isDeleted(true);

            if (!m2ndDB.isOpen() || !((dbFireBase) m2ndDB).markRec(itemToDo.getKey())){

                // Record the deletion for the next sync.
                dbCloud.delete(id, itemToDo.getKey());
            }else{

                if (dbCloud.insert(itemToDo.getKey(), "DELETE")){

                    itemToDo.setKey("");
                }
            }
        }

        return delete;
    }



    public boolean delete(long id){

        return mDBHelper.markRec(id);
    }



    boolean trueDelete(ToDoItem itemToDo){

        return mDBHelper.deleteRec(itemToDo.getId()) > 0;
    }

//    // Returns the database
//    SQLiteDatabase getDatabase(){
//
//        return mDBHelper.getDatabase();
//    }



    boolean showDeleted(){

        return mShowDeleted;
    }



    private boolean showDeleted(boolean showDeleted){

        mShowDeleted = showDeleted;

        return mShowDeleted;
    }



    ArrayList<ToDoItem> ToDoList(){

        // Determine if deleted records are to be displayed.
        mDBHelper.showDeleted(showDeleted(getBoolean("show_deleted", false)));

        mDBHelper.clearResultSet();

        return ToDoList(mDBHelper.getResultSet());
    }



    public ArrayList<ToDoItem> ToDoList(Cursor recs){

        return mDBHelper.ToDoList(recArrayList(recs));
    }



    public ArrayList<ToDoItem> ToDoList(ArrayList<HashMap<String, String>> recs){

        return mDBHelper.ToDoList(recs);
    }




    @Override
    public void onLogin(){

        dbCloud.reSave(mDBHelper);
    }




    public ArrayList<HashMap<String, String>> recArrayList(Cursor query){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String value;

        while (query.moveToNext()){

            HashMap<String, String> row = new HashMap<>();

            //iterate over the columns
            for (int col = 0; col < query.getColumnNames().length; col++){

                //TODO You've got to use getType() and return  ArrayList<HashMap<String, Object>>   No?
                value = query.getString(col);

                if (value == null){

                    // Type String
                    if (query.getType(col) == 3){

                        value = "";
                    }else{

                        value = "-1";
                    }
                }

                row.put(query.getColumnName(col), value);
            }

            list.add(row);
        }

        return list;
    }



    // Returns the records
    Cursor getRecs(){

        return mDBHelper.getRecs();
    }




    public Cursor getRecs(String whereClause){

        return mDBHelper.getRecs(whereClause);
    }




    public Cursor getRecord(int rowId){

        return mDBHelper.getRecord(rowId);
    }




    void importRec(String columnName, String value){

        mDBHelper.bindRecValues(columnName, value);
    }



    boolean importRec(){

        return mDBHelper.importRec();
    }



    boolean createEmptyTemp(){

        return mDBHelper.createEmptyTemp();
    }



    boolean insertTempRec(){

        return mDBHelper.insertTempRec() > 0;
    }



    Cursor getNewTempRecs(){

        return mDBHelper.getNewTempRecs();
    }



    public boolean insertIfNewRec(){

        boolean newRec = mDBHelper.ifNewRec();

        if (newRec){

            newRec = mDBHelper.importRec();
        }

        return newRec;
    }




    private void sync(ArrayList<HashMap<String, String>> dataArrayList){

        dbCloud.sync(mDBHelper, m2ndDB, this);

//         if(dataArrayList.size() > 0 && mDBHelper.getRecs().getCount() == 0){
//
//             ArrayList<ToDoItem> items = mDBHelper.ToDoList(dataArrayList);
//
//             mAppView.mToDoListAdapter.setItems(items);
//
//             for(ToDoItem item : items){
//
//                 save(item);
//             }
//         }
    }


    protected void onStart(){

        open();

        // Signed either anonymously or Google or Facebook
        if(Auth.isSignedIn()){

            Auth.onStart();

            ((dbFireBase)m2ndDB).open(new dbFireBase.OnDataListener(){

                public void onDownload(ArrayList<HashMap<String, String>> dataArrayList){

                    mAppView.getModel().sync(dataArrayList);
                }
            });
        }else{

            Auth.onStart(new OnSuccessListener<AuthResult>(){

                public void onSuccess(AuthResult result){

                    ((dbFireBase)m2ndDB).open(new dbFireBase.OnDataListener(){

                        public void onDownload(ArrayList<HashMap<String, String>> dataArrayList){

                            mAppView.getModel().sync(dataArrayList);
                        }
                    });
                }
            });
        }
    }



    // The App might get destroyed with calling onDestroy, and so take no chances.
    void onStop(){

        // close the db connection...
        close();

        Auth.onStop();
    }



    void onRestart(){

        // db likely closed.
        //       open();
    }



    protected void onDestroy(){

        mAppView = null;

//        mContext = null;

        mDBHelper.onDestroy();

        mDBHelper = null;

        m2ndDB.onDestroy();

        m2ndDB = null;

        Auth.onDestroy();
    }




    private class onDBSetup implements dbSQLlite.OnCreateListener, dbSQLlite.OnConfigureListener{

        public void onCreate(SQLiteDatabase db){

            if(db.getVersion() == 0){

                // Add this device reference to the cloud.
                dbCloud.setDeviceDirectory();
            }
        }



        public void onConfigure(SQLiteDatabase db){

//            db.setVersion(0);

            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }
    }
}
