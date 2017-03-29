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
/**
 * Created by Drawn on 3/4/2017.
 */

public class dbCloud{

    private static String mDBName;

    private static SQLiteHelper mDBHelper;



    private dbCloud(){

    }



    public static void onCreate(appController app){

        mDBName = "sync";

        mDBHelper = new SQLiteHelper(app, mDBName, 1,
                "(rowid INTEGER PRIMARY KEY AUTOINCREMENT, id Long, key VARCHAR, action VARCHAR, timestamp INTEGER);");

        // Recreate the table while developing.
        // recreate();

        DataSync.onCreate(app);
    }



    public static boolean save(long recId, String key){

        boolean save = open();

        if (save){

            ContentValues recValues = new ContentValues();

            recValues.put("id", recId);

            recValues.put("key", key);

            recValues.put("action", "UPDATE");

            // time is seconds
            recValues.put("timestamp", System.currentTimeMillis() / 1000);

            // Record is no longer to be deleted but updated.
            if (getAction(recId).equals("DELETE")){

                save = update(recValues, recId);
            }else{

                save = insertNew(recValues, recId);
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

            ContentValues recValues = new ContentValues();

            recValues.put("id", recId);

            recValues.put("key", key);

            recValues.put("action", "DELETE");

            // time is seconds
            recValues.put("timestamp", System.currentTimeMillis() / 1000);

            delete = update(recValues, recId);
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

//            // While developing, I'm going to have to recreate the table from time to time.
//            mDBHelper.setOnConfigureListener(new SQLiteHelper.OnConfigureListener(){
//
//                public void onConfigure(SQLiteDatabase db){
//
//                    if(mDBHelper.drop(db)){
//
//                        db.setVersion(0);
//                    }
//                }
//            });

            open = mDBHelper.open().isOpen();
        }

        return open;
    }



    public static ArrayList<HashMap<String, String>> getDataArrayList(){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        Cursor rec = getRecs();

//        int rowid = rec.getColumnIndex("rowid");
//
//        int idIdx = rec.getColumnIndex("id");
//
//        int keyIdx = rec.getColumnIndex("key");
//
//        int actionIdx = rec.getColumnIndex("action");
//
//        int timeIdx = rec.getColumnIndex("timestamp");

        while (rec.moveToNext()){

            HashMap<String, String> item = new HashMap<>();

            for (String colName : rec.getColumnNames()){

                item.put(colName, rec.getString(rec.getColumnIndex(colName)));

//                item.put("rowid", rec.getString(rowid));
//
//                item.put("id", rec.getString(idIdx));
//
//                item.put("key", rec.getString(keyIdx));
//
//                item.put("action", rec.getString(actionIdx));
//
//                item.put("timestamp", rec.getString(timeIdx));
            }

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



    public static boolean resync(){

        ArrayList<HashMap<String, String>> recList = getDataArrayList();

        for (HashMap<String, String> record : recList){

            DataSync.insert(record.get("key"), "UPDATE");
        }

        return true;
    }



    public static void onDestroy(){

        if (mDBHelper != null){

            mDBHelper.close();

            mDBHelper = null;
        }

        DataSync.onDestroy();
    }

//**********************************************************

    interface afterValueEventListener{

        void afterDataChange(DataSnapshot snapshot);
    }

    public static class DataSync{

        static DatabaseReference mDBRef;

        static DatabaseReference mDevRef;

        private static String mInstallNum;

        private static FirebaseDatabase mDatabase;

        private static appModel mAppModel;



        static void onCreate(appController app){

            mDatabase = FirebaseDatabase.getInstance();

            mAppModel = app.getModel();

            mInstallNum = app.getInstallNum();
        }



        static DatabaseReference getDBRef(){

            if (mDBRef == null && isOnline()){

                mDatabase.goOnline();

                mDBRef = mDatabase.getReference().child("sync").child(Auth.getUid())
                        .child(mInstallNum);
            }

            return mDBRef;
        }




        static DatabaseReference getDevRef(){

            if(mDevRef == null){

                mDevRef = mDatabase.getReference().child("devices").child(Auth.getUid());
            }

            return mDevRef;
        }




        static boolean sync(final dbInterface dbLocal, final dbInterface dbCloud){

            if (!isOnline()){return false;}

            //  Retrieves the dbFirebase 'cloud' side
            final ArrayList<HashMap<String, String>> cloudList = dbCloud.getDataArrayList();

            // Retrieves the local database.
            final Cursor rec = dbLocal.getRecs();

            // Process the online sync table records if any.
            getDBRef().child("IN").addValueEventListener(new ValueEventListener(){

                @Override
                public void onDataChange(DataSnapshot snapshot){

                    if (snapshot == null){

                        return;
                    }

                    DatabaseReference dbINRef;

                    dbINRef = getDBRef().child("IN");

                    // Only want this to fire once.
                    dbINRef.removeEventListener(this);

                    boolean synced;

                    String key, action;

                    // Retrieve online sync table records
                    ArrayList<HashMap<String, String>> syncList = recArrayList(snapshot);

                    // Retrieve the local database
                    ArrayList<HashMap<String, String>> recList;

                    if (syncList.size() > 0){

                        // Retrieve the local database
                        recList = mAppModel.recArrayList(rec);
                    }else{

                        recList = new ArrayList<>();
                    }

                    for (HashMap<String, String> syncRec : syncList){

                        synced = false;

                        key = syncRec.get("key");

                        action =  syncRec.get("action");

                        //  dbFirebase 'cloud' side copy
                        HashMap<String, String> cloudRec = getCloudItem(key, cloudList);

                        // A local copy
                        HashMap<String, String> localRec = getRecItem(key, recList);

                        // Add to the local device.
                        if (localRec.size() == 0){

                            // It's been deleted and not to be added anyway.
                            if(action.equals("DELETE")){

                                continue;
                            }

                            synced = addToLocal(cloudRec);

                            // Update the appropriate database with the most recent copy.
                        }else{

                            if(action.equals("DELETE")){

                                // Delete the local copy
                                synced = deleteLocal(cloudRec);
                            }else{

                                // Update the local copy
                                synced = updateToLocal(cloudRec);
                            }
                        }

                        if (synced){

                            DatabaseReference dbRef = dbINRef.child(syncRec.get("keyID"));

                            // Delete that record
                            dbRef.removeValue();
                        }
                    }

                    // Update the dbFirebase database now that there is online access.
                    updateCloud();
                }



                private HashMap<String, String> getCloudItem(String key,
                        ArrayList<HashMap<String, String>> list){

                    HashMap<String, String> cloudItem = new HashMap<>();

                    for (HashMap<String, String> item : list){

                        if (item.get("key").equals(key)){

                            cloudItem = item;

                            list.remove(item);

                            break;
                        }
                    }

                    return cloudItem;
                }



                private HashMap<String, String> getRecItem(String key,
                        ArrayList<HashMap<String, String>> list){

                    HashMap<String, String> recItem = new HashMap<>();

                    for (HashMap<String, String> item : list){

                        if (item.get("ToDoKey").equals(key)){

                            recItem = item;

                            break;
                        }
                    }

                    return recItem;
                }



                /**
                 *
                 *  Update the FireBase database now that there is online access.
                 */
                void updateCloud(){

                    // Retrieves the local sync table
                    ArrayList<HashMap<String, String>> syncDataList = getDataArrayList();

                    for (HashMap<String, String> item : syncDataList){

                        String rowId = item.get("rowid");

                        String id = item.get("id");

                        String key = item.get("key");

                        String action = item.get("action");

                        long timestamp = Long.parseLong(item.get("timestamp"));

                        ArrayList<ToDoItem> items = dbLocal.ToDoList(
                                mAppModel
                                        .recArrayList(mAppModel.getRecord(Integer.valueOf(id))));

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

                                insert(itemToDo.getKey(), action);
                            }
                        }
                    }
                }


                boolean addToLocal(HashMap<String, String> cloudItem){

                    // It is a new data item.
                    cloudItem.put("ToDoID", "");

                    return saveToLocal(cloudItem);
                }




                boolean updateToLocal(HashMap<String, String> cloudItem){

                    return saveToLocal(cloudItem);
                }






                boolean saveToLocal(HashMap<String, String> cloudItem){

                    ToDoItem item = toToDoItem(cloudItem);

                    boolean save = item != null;

                    if(save){

                        save = dbLocal.save(item);
                    }

                    return save;
                }




                boolean deleteLocal(HashMap<String, String> cloudItem){

                    ToDoItem item = toToDoItem(cloudItem);

                    boolean delete = item != null;

                    if(delete){

                        delete = dbLocal.deleteRec(toToDoItem(cloudItem).getId()) > 0;
                    }

                    return delete;
                }




               ToDoItem toToDoItem(HashMap<String, String> cloudItem){

                    ArrayList<HashMap<String, String>> rec = new ArrayList<>();

                    rec.add(cloudItem);

                   ToDoItem item;

                    try{

                        item =  dbLocal.ToDoList(rec).get(0);

                        item.setKey(cloudItem.get("key"));

                    }catch(Exception ex){

                        ErrorHandler.logError(ex);

                        item = null;
                    }

                    return item;
                }




                @Override
                public void onCancelled(DatabaseError databaseError){

                    String error = databaseError.getMessage();
                }
            });

            return true;
        }




        private static void deviceDirectory(){

            final DatabaseReference deviceRef = getDevRef().child(mInstallNum);

            deviceRef.addValueEventListener(new ValueEventListener(){

                @Override
                public void onDataChange(DataSnapshot snapshot){

                    if (snapshot == null){ return; }

                    if (snapshot.exists()){ return; }

                    deviceRef.setValue("", new DatabaseReference.CompletionListener(){

                                public void onComplete(DatabaseError error,
                                        DatabaseReference ref){

                                    if (error == null){

                                    }else{

                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError){

                    String error = databaseError.getMessage();
                }
            });


        }



        public static boolean insert(final String key, final String action){

            // Record this device up on the cloud if not already.
            deviceDirectory();

            if (key == null || key.isEmpty()){ return false; }

            if (action == null || action.isEmpty()){ return false; }

            replace(key, new afterValueEventListener(){

                public void afterDataChange(DataSnapshot snapshot){

                    ContentValues recValues = new ContentValues();

                    recValues.put("key", key);

                    recValues.put("action", action.toUpperCase().trim());

                    // time is seconds
                    recValues.put("timestamp", System.currentTimeMillis() / 1000);

                    insert(recValues);
                }
            });

            return true;
        }



        static String insert(ContentValues recValues){

            return insert(getDBRef().child("OUT"), recValues);
        }



        static String insert(DatabaseReference DBRef, ContentValues recValues){

            String key = "";

            try{

                key = DBRef.push().getKey();

                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put(key, toMap(recValues));

                DBRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener(){

                    public void onComplete(DatabaseError error, DatabaseReference ref){

                        if (error != null){

                            ErrorHandler.logError("Could not save sync record!");
                        }
                    }
                });
            }catch (Exception ex){

                key = "";
            }

            return key;
        }



        static void replace(String key, final afterValueEventListener listener){

            getDBRef().child("OUT").orderByChild("key").equalTo(key)
                    .addListenerForSingleValueEvent(new ValueEventListener(){

                        @Override
                        public void onDataChange(DataSnapshot snapshot){

                            DatabaseReference dbOUTRef;

                            dbOUTRef = getDBRef().child("OUT");

                            // Have this only fire once.
                            dbOUTRef.removeEventListener(this);

                            for (DataSnapshot shot : snapshot.getChildren()){

                                // Delete that record
                                dbOUTRef.child(shot.getKey()).removeValue();
                            }

                            if (listener != null){

                                listener.afterDataChange(snapshot);
                            }
                        }



                        @Override
                        public void onCancelled(DatabaseError error){

                        }
                    });
        }



        static HashMap<String, Object> toMap(ContentValues recValues){

            HashMap<String, Object> recMap = new HashMap<>();

            Set<String> keys = recValues.keySet();

            for (String key : keys){

                Object obj = recValues.get(key);

                if (obj instanceof String){

                    recMap.put(key, recValues.getAsString(key));

                }else if (obj instanceof Long){

                    recMap.put(key, recValues.getAsLong(key));

                }else if (obj instanceof Integer){

                    recMap.put(key, recValues.getAsInteger(key));

                }else if (obj instanceof Boolean){

                    recMap.put(key, recValues.getAsBoolean(key));

                }else if (obj instanceof Float){

                    recMap.put(key, recValues.getAsFloat(key));
                }
            }

            return recMap;
        }



        private static ArrayList<HashMap<String, String>> recArrayList(DataSnapshot snapshot){

            ArrayList<HashMap<String, String>> list = new ArrayList<>();

            if (snapshot == null){

                return list;
            }

            // This is awesome! No middle man!
            Object fieldsObj = new Object();

            HashMap fldObj;

            for (DataSnapshot shot : snapshot.getChildren()){

                try{

                    fldObj = (HashMap) shot.getValue(fieldsObj.getClass());

                }catch (Exception ex){

                    ErrorHandler.logError(ex);

                    continue;
                }

                fldObj.put("keyID", shot.getKey());

                list.add(fldObj);
            }

            return list;
        }



        static boolean isOnline(){

            // Is connected to the Internet.
            return mAppModel.getController().NoConnectivity().isEmpty();
        }



        static void onDestroy(){

            mDBRef = null;

            mDevRef = null;

            mDatabase = null;

            mAppModel = null;

            mDBRef = null;
        }
    }
}
