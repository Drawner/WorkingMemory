package com.gtfp.workingmemory.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.gtfp.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.Auth.Auth;
import com.gtfp.workingmemory.app.appController;
import com.gtfp.workingmemory.app.appModel;
import com.gtfp.workingmemory.frmwrk.db.SQLiteHelper;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.gtfp.workingmemory.db.dbFireBase.recArrayList;
/**
 * Created by Drawn on 3/4/2017.
 */

public class dbCloud{

    private static String mDBName;

    private static SQLiteHelper mDBHelper;

    private static ContentValues mRecValues;



    private dbCloud(){

    }



    public static void onCreate(appController app){

        mDBName = "sync";

        mDBHelper = new SQLiteHelper(app, mDBName, 1,
                "(id Long, key VARCHAR, action VARCHAR, timestamp INTEGER);");

        // Recreate the table while developing.
        // recreate();

        // Stores the record's contents.
        mRecValues = new ContentValues();

        DataSync.onCreate(app);
    }



    public static boolean save(long recId, String key){

        boolean save = open();

        if (save){

            mRecValues.put("id", recId);

            mRecValues.put("key", key);

            mRecValues.put("action", "UPDATE");

            // time is seconds
            mRecValues.put("timestamp", System.currentTimeMillis() / 1000);

            // Record is no longer to be deleted but updated.
            if (getAction(recId).equals("DELETE")){

                save = update(mRecValues, recId);
            }else{

                save = insertNew(mRecValues, recId);
            }
        }

        return save;
    }



    public static boolean insertNew(ContentValues recValues, long recId){

        long rowId = mDBHelper.getRowID(recId);

        boolean insert = rowId < 1;

        // Only insert 'new' records. A 'sync' will delete all these records.
        if (insert){

            insert = mDBHelper.insert(recValues) > 0;
        }else{

            // A record is already there.
            insert = true;
        }

        return insert;
    }



    public static boolean delete(long recId, String key){

        boolean delete = open();

        if (delete){

            mRecValues.put("id", recId);

            mRecValues.put("key", key);

            mRecValues.put("action", "DELETE");

            // time is seconds
            mRecValues.put("timestamp", System.currentTimeMillis() / 1000);

            delete = update(mRecValues, recId);
        }

        return delete;
    }



    public static boolean update(ContentValues recValues, long recId){

        long rowId = mDBHelper.getRowID(recId);

        boolean update = rowId > 0;

        if (update){

            update = mDBHelper.update(recValues, rowId) > 0;
        }else{

            update = mDBHelper.insert(recValues) > 0;
        }

        return update;
    }



    public static String getAction(long recId){

        String stmt = "SELECT action from " + mDBName + " WHERE id = " + recId;

        Cursor rec = mDBHelper.runQuery(stmt);

        String action = "";

        // You've got to move the cursor's position pointer after the query.
        if (rec.moveToFirst()){

            try{

                action = rec.getString(0);

            }catch (Exception ex){

                action = "";
            }
        }

        return action;
    }



    private static boolean open(){

        if (mDBHelper != null && mDBHelper.isOpen()){ return true; }

        boolean open = mDBHelper != null;

        if (open){

            open = mDBHelper.open().isOpen();
        }

        return open;
    }



    public static ArrayList<HashMap<String, String>> getDataArrayList(){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        Cursor rec = getRecs();

        int idIdx = rec.getColumnIndex("id");

        int keyIdx = rec.getColumnIndex("key");

        int actionIdx = rec.getColumnIndex("action");

        int timeIdx = rec.getColumnIndex("timestamp");

        while (rec.moveToNext()){

            HashMap<String, String> item = new HashMap<>();

            item.put("id", rec.getString(idIdx));

            item.put("key", rec.getString(keyIdx));

            item.put("action", rec.getString(actionIdx));

            item.put("timestamp", rec.getString(timeIdx));

            list.add(item);
        }

        return list;
    }



    public static Cursor getRecs(){

        open();

        return mDBHelper.runQuery("SELECT * FROM " + mDBName);
    }



    private static void recreate(){

        mDBHelper.setOnConfigureListener(new SQLiteHelper.OnConfigureListener(){

            @Override
            public void onConfigure(SQLiteDatabase db){

                if (mDBHelper.drop(db)){

                    db.setVersion(0);
                }
            }
        });
    }



    public static boolean sync(final dbInterface dbLocal, final dbInterface dbCloud){

        // Perform off the main UI thread.
        new Thread(){

            @Override
            public void run(){

                try{

                    DataSync.sync(dbLocal, dbCloud);

                }catch (Exception ex){

                    ErrorHandler.logError(ex);
                }
            }
        }.start();

        return true;
    }



    public static void onDestroy(){

        if (mDBHelper != null){

            mDBHelper.close();

            mDBHelper = null;
        }

        DataSync.onDestroy();
    }



    public static class DataSync{

        static DatabaseReference mDBRef;

        private static String mInstallNum;

        private static FirebaseDatabase mDatabase;

        private static appModel mAppModel;

        private static boolean mIgnoreTrigger;



        static void onCreate(appController app){

            mDatabase = FirebaseDatabase.getInstance();

            mAppModel = app.getModel();

            mInstallNum = app.getInstallNum();
       }



        static DatabaseReference getDBRef(){

            if (mDBRef == null && isOnline()){

                mDatabase.goOnline();

                mDBRef = mDatabase.getReference().child("sync").child(Auth.getUid())
                        .child("device-" + mInstallNum);
            }

            return mDBRef;
        }



        static boolean sync(final dbInterface dbLocal, final dbInterface dbCloud){

            boolean sync = false;

            ArrayList<HashMap<String, String>> cloudList = dbCloud.getDataArrayList();

            Cursor rec = dbLocal.getRecs();

            final ArrayList<HashMap<String, String>> syncDataList = getDataArrayList();

            syncCloud(new ValueEventListener(){

                @Override
                public void onDataChange(DataSnapshot snapshot){

                    if (snapshot == null){

                        return;
                    }

                    ArrayList<HashMap<String, String>> list = recArrayList(snapshot, true);

                    for (HashMap<String, String> item : list){

                    }

                    updateCloud();
                }



                void updateCloud(){

                    for (HashMap<String, String> item : syncDataList){

                        String rowId = item.get("id");

                        String key = item.get("key");

                        String action = item.get("action");

                        long timestamp = Long.parseLong(item.get("timestamp"));

                        ArrayList<ToDoItem> items = dbLocal.ToDoList(
                                mAppModel
                                        .recArrayList(mAppModel.getRecord(Integer.valueOf(rowId))));

                        for (ToDoItem itemToDo : items){

                            // Add it to the cloud
                            if (key.isEmpty()){

                                // Indicate it's a new item
                                itemToDo.newItem(true);

                                // Update the cloud or vic versa
                            }else{

                            }

                            if (dbCloud.save(itemToDo) && dbLocal.save(itemToDo)){

                                mDBHelper.delete(rowId);
                            }
                        }
                    }
                }



                @Override
                public void onCancelled(DatabaseError databaseError){

                    String error = databaseError.getMessage();
                }
            });

            return true;
        }



        static void syncCloud(ValueEventListener listener){

            if(!isOnline()){return;}

            getDBRef().addValueEventListener(listener);

//            // Query the database
//            Query queryRef = mDBRef.orderByKey();
        }



        static String insert(ContentValues recValues){

            String key = "";

            try{

                key = getDBRef().push().getKey();

                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put(key, toMap(recValues));

                mIgnoreTrigger = true;

                getDBRef().updateChildren(childUpdates);

            }catch (Exception ex){

                mIgnoreTrigger = false;

                key = "";
            }

            return key;
        }




        public static String insert(long recId, String key){

            mRecValues.put("id", recId);

            mRecValues.put("key", key);

            mRecValues.put("action", "UPDATE");

            // time is seconds
            mRecValues.put("timestamp", System.currentTimeMillis() / 1000);

            return  insert(mRecValues);
        }




        static HashMap<String, Object> toMap(ContentValues recValues){

            HashMap<String, Object> recMap = new HashMap<>();

            Set<String> keys =  recValues.keySet();

            for (String key : keys){

               Object obj = recValues.get(key);

                if(obj instanceof String){

                    recMap.put(key, recValues.getAsString(key));

                }else if(obj instanceof Long){

                    recMap.put(key, recValues.getAsLong(key));

                }else if (obj instanceof Integer){

                    recMap.put(key, recValues.getAsInteger(key));

                }else if(obj instanceof Boolean){

                    recMap.put(key, recValues.getAsBoolean(key));

                }else if(obj instanceof Float){

                    recMap.put(key, recValues.getAsFloat(key));
                }
            }

            return recMap;
        }



        static boolean isOnline(){

            // Is connected to the Internet.
            return mAppModel.getController().NoConnectivity().isEmpty();
        }


        static void onDestroy(){

            mDatabase = null;

            mAppModel = null;

            mDBRef = null;
        }
    }
}
