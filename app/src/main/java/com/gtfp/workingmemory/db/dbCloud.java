package com.gtfp.workingmemory.db;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.gtfp.workingmemory.settings.appSettings;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.gtfp.workingmemory.db.dbCloud.DataSync.getDevRef;
/**
 * Created by Drawn on 3/4/2017.
 */

public class dbCloud{

    private static String mDBName;

    private static SQLiteHelper mDBHelper;

    private static appModel mAppModel;




    private dbCloud(){

    }



    public static void onCreate(appController app){

        mAppModel = app.getModel();

        mDBName = "sync";

        mDBHelper = new SQLiteHelper(app, mDBName, 1,
                "(rowid INTEGER PRIMARY KEY AUTOINCREMENT, id Long, key VARCHAR, action VARCHAR, timestamp INTEGER);");

        // Recreate the table while developing.
        // recreate();

        DataSync.onCreate(app);
    }



    public static void onStart(){

        onLogin.addAuthStateListener();
    }



    public static void onStop(){

        onLogin.removeAuthStateListener();
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



    public static boolean insert(String key, String action){

        return DataSync.insert(key, action);
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



    private static String getAction(long recId){

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



    private static ContentValues getRec(String key){

        ContentValues recValues = new ContentValues();

        if (key == null || key.isEmpty()){

            return recValues;
        }

        String stmt = "SELECT * from " + mDBName + " WHERE key = \"" + key.trim() + "\"";

        Cursor rec = mDBHelper.runQuery(stmt);

        if (rec.moveToFirst()){

            for (String name : rec.getColumnNames()){

                int index = rec.getColumnIndex(name);

                switch (rec.getType(index)){
                    // null
                    case 0:

                        recValues.put(name, "");

                        break;
                    // int
                    case 1:

                        recValues.put(name, rec.getInt(index));

                        break;
                    // float
                    case 2:

                        recValues.put(name, rec.getFloat(index));

                        break;
                    // string
                    case 3:

                        recValues.put(name, rec.getString(index));

                        break;
                    // blob
                    case 4:

                        recValues.put(name, rec.getBlob(index));

                        break;
                    default:

                        recValues.put(name, rec.getString(index));
                }
            }
        }

        return recValues;
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



    public static ArrayList<HashMap<String, String>> getDataArrayList(Cursor rec){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

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



    public static boolean sync(dbInterface dbLocal, dbInterface dbCloud){

        DataSync.sync(dbLocal, dbCloud);

        return true;
    }



    private static HashMap<String, String> getCloudItem(String key,
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



    private static HashMap<String, String> getRecItem(String key,
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



    public static void addOnLoginListener(onLoginListener listener){

        onLogin.addOnLoginListener(listener);
    }



    public static void removeOnLoginListener(onLoginListener listener){

        onLogin.removeOnLoginListener(listener);
    }



    public static void reSave(dbInterface dbLocal){

        dbLocal localDB = new dbLocal(dbLocal);

        ArrayList<HashMap<String, String>> dataList = getDataArrayList(dbLocal.getCurrentRecs());

        // Save all the records again.
        for (HashMap<String, String> rec : dataList){

            ToDoItem item = localDB.toToDoItem(rec);

            // Clear the key to have it inserted in the new 'cloud' table.
//            item.setKey("");

            mAppModel.save(item);
        }

        final String prevUid = Auth.getPrevUid();

        if (prevUid.isEmpty()){return;}

        getDevRef(prevUid).removeValue(new DatabaseReference.CompletionListener(){

            public void onComplete(DatabaseError error, DatabaseReference ref){

                if (error != null){

                    ErrorHandler.logError(error.toException());
                    return;
                }

                // The parent is the prevUid listing all the user's devices.
                DataSync.syncRef(prevUid).getParent()
                        .removeValue(new DatabaseReference.CompletionListener(){

                            public void onComplete(DatabaseError error, DatabaseReference ref){

                                if (error != null){

                                    ErrorHandler.logError(error.toException());
                                    return;
                                }

                                dbFireBase.prevUserIdDBRef()
                                        .removeValue(new DatabaseReference.CompletionListener(){

                                            public void onComplete(DatabaseError error,
                                                    DatabaseReference ref){

                                                if (error != null){

                                                    ErrorHandler.logError(error.toException());
                                                }
                                            }
                                        });
                            }
                        });
            }
        });
    }



    public static boolean resync(){

        // Local sync table
        ArrayList<HashMap<String, String>> recList = getDataArrayList(getRecs());

        for (HashMap<String, String> record : recList){

            DataSync.insert(record.get("key"), "UPDATE");
        }

        return true;
    }



    public static void onDestroy(){

        mAppModel = null;

        if (mDBHelper != null){

            mDBHelper.close();

            mDBHelper = null;
        }

        DataSync.onDestroy();

        onLogin.onDestroy();
    }

//**********************************************************


    interface afterValueEventListener{

        void afterDataChange(DataSnapshot snapshot);
    }


    public interface onLoginListener{

        void onLogin();
    }
//**********************************************************

    static class dbLocal{

        dbInterface mLocalDB;



        dbLocal(dbInterface dbLocal){

            mLocalDB = dbLocal;
        }



        boolean add(HashMap<String, String> cloudItem){

            // It is a new data item.
            cloudItem.put("ToDoID", "");

            return save(cloudItem);
        }



        boolean update(HashMap<String, String> cloudItem){

            return save(cloudItem);
        }



        boolean save(HashMap<String, String> cloudItem){

            ToDoItem item = toToDoItem(cloudItem);

            boolean save = item != null;

            if (save){

                save = save(item);
            }

            return save;
        }



        public boolean save(ToDoItem itemToDo){

            dbSQLlite sqlDB = ((dbSQLlite) mLocalDB);

            Long recId = sqlDB.getRecID(itemToDo.getKey());

            if (!itemToDo.newItem(recId < 1)){

                itemToDo.setId(recId);

                return mLocalDB.updateRec(itemToDo) > 0;
            }else{

                Long rowId = sqlDB.insertRec(itemToDo);

                if (rowId < 0){
                    // Note, there was an error with a negative number.
                    // but will be set to zero in the variable.
                    rowId = 0L;
                }

                // This is my savior. It's the assigned ID I hope.
                itemToDo.setId(rowId);

                return rowId > 0;
            }
        }



        boolean delete(HashMap<String, String> cloudItem){

            ToDoItem item = toToDoItem(cloudItem);

            boolean delete = item != null;

            if (delete){

                delete = mLocalDB.deleteRec(toToDoItem(cloudItem).getId()) > 0;
            }

            return delete;
        }



        ToDoItem toToDoItem(HashMap<String, String> cloudItem){

            ArrayList<HashMap<String, String>> rec = new ArrayList<>();

            rec.add(cloudItem);

            ToDoItem item;

            try{

                item = mLocalDB.ToDoList(rec).get(0);

                if (cloudItem.containsKey("key")){

                    item.setKey(cloudItem.get("key"));
                }

            }catch (Exception ex){

                ErrorHandler.logError(ex);

                item = null;
            }

            return item;
        }
    }


    public static class DataSync{

        static DatabaseReference mDevRef;

        private static String mInstallNum;

        private static FirebaseDatabase mDatabase;

        private static appModel mAppModel;



        static void onCreate(appController app){

            mDatabase = FirebaseDatabase.getInstance();

            mAppModel = app.getModel();

            mInstallNum = app.getInstallNum();
        }



        static DatabaseReference syncRef(String userId){

            if (!isOnline()){

                mDatabase.goOnline();
            }

            String id;

            if (userId == null || userId.isEmpty()){

                id = null;
            }else{

                id = userId.trim();
            }

            DatabaseReference ref;

            if (id == null){

                // Important to give a reference that's not  there in case called by deletion routine.
                ref = mDatabase.getReference().child("sync").child("dummy").child(mInstallNum);
            }else{

                ref = mDatabase.getReference().child("sync").child(id)
                        .child(mInstallNum);
            }

            return ref;
        }



        static DatabaseReference getDevRef(String userId){

            String id;

            if (userId == null || userId.isEmpty()){

                id = null;
            }else{

                id = userId.trim();
            }

            DatabaseReference ref;

            if (id == null){

                // Important to provide a reference that is not likely there in case called by deletion routine.
                ref = mDatabase.getReference().child("devices").child(mInstallNum);
            }else{

                ref = mDatabase.getReference().child("devices").child(id)
                        .child(mInstallNum);
            }

            return ref;
        }



        static boolean sync(final dbInterface dbLocal, final dbInterface dbCloud){

            if (!isOnline()){return false;}

            final DatabaseReference syncINRef = syncRef(Auth.getUid()).child("IN");

            // Process the online sync table records if any.
            syncINRef.addValueEventListener(new ValueEventListener(){

                @SuppressWarnings("unchecked")
                @Override
                public void onDataChange(final DataSnapshot snapshot){

                    if (snapshot == null){

                        return;
                    }

                    // Only want this to fire once.
                    syncINRef.removeEventListener(this);

                    // Create a new instance every time as each can only be called once.
                    asyncData task = new asyncData();

                    if(task.isRunning()){

                       return;
                    }

                    HashMap<String, Object> params = new HashMap<>();

                    params.put("snapshot", snapshot);

                    params.put("dbLocal", dbLocal);

                    params.put("dbCloud", dbCloud);

                    task.execute(params);

//                    try{
//
//                        syncData(snapshot, dbLocal, dbCloud);
//
//                        // Update the dbFirebase database now that there is online access.
//                        updateCloud(dbLocal, dbCloud);
//
//                    }catch (Exception ex){
//
//                        ErrorHandler.logError(ex);
//                    }
                }



                @Override
                public void onCancelled(DatabaseError databaseError){

                    String error = databaseError.getMessage();
                }
            });

            return true;
        }



        private static void syncData(DataSnapshot snapshot, dbInterface dbLocal,
                dbInterface dbCloud){

            DatabaseReference dbINRef = syncRef(Auth.getUid()).child("IN");

            boolean synced;

            String key, action;

            int timestamp;

            // Retrieve online sync table records
            ArrayList<HashMap<String, String>> syncList = recArrayList(snapshot);

            // Retrieve the local database
            ArrayList<HashMap<String, String>> recList;

            //  Retrieves the dbFirebase 'cloud' side
            final ArrayList<HashMap<String, String>> cloudList;

            if (syncList.size() > 0){

                // Retrieves the local database.
                final Cursor rec = dbLocal.getRecs();

                recList = mAppModel.recArrayList(rec);

                cloudList = dbCloud.getDataArrayList();
            }else{

                recList = new ArrayList<>();

                cloudList = new ArrayList<>();
            }

            // Online sync records
            for (HashMap<String, String> syncRec : syncList){

                synced = false;

                key = syncRec.get("key");

                action = syncRec.get("action");

                timestamp = Integer.parseInt(syncRec.get("timestamp"));

                // Retrieve the local sync record if any.
                ContentValues localSync = getRec(key);

                if (localSync.size() > 0){

                    // If the local is more up to date
                    if (timestamp < (Integer) localSync.get("timestamp")){

                        synced = true;

                    // The remote change is more recent.
                    }else{

                        // Delete the local sync entry. It's old.
                        mDBHelper.delete((Integer)localSync.get("rowid"));
                    }
                }

                if (!synced){

                    //  dbFirebase 'cloud' side copy
                    HashMap<String, String> cloudRec = getCloudItem(key, cloudList);

                    // A local copy
                    HashMap<String, String> localRec = getRecItem(key, recList);

                    dbLocal local = new dbLocal(dbLocal);

                    // Add to the local device.
                    if (localRec.size() == 0){

                        if (action.equals("DELETE")){

                            // It's been deleted and not to be added anyway.
                            synced = true;
                        }else{

                            synced = local.add(cloudRec);
                        }

                        // Update the appropriate database with the most recent copy.
                    }else{

                        if (action.equals("DELETE")){

                            if (cloudRec.size() == 0){

                                // It has been deleted already
                                synced = true;
                            }else{

                                // Delete the local copy
                                synced = local.delete(cloudRec);
                            }
                        }else{

                            // Update the local copy
                            synced = local.update(cloudRec);
                        }
                    }
                }

                if (synced){

                    DatabaseReference dbRef = dbINRef.child(syncRec.get("keyID"));

                    // Delete that record
                    dbRef.removeValue();
                }
            }
        }



        /**
         * Update the FireBase database now that there is online access.
         */
        private static void updateCloud(dbInterface dbLocal, dbInterface dbCloud){

            // Retrieves the local sync table
            ArrayList<HashMap<String, String>> syncDataList = getDataArrayList(getRecs());

            for (HashMap<String, String> item : syncDataList){

                String rowId = item.get("rowid");

                String id = item.get("id");

                String key = item.get("key");

                String action = item.get("action");

                ArrayList<ToDoItem> items = dbLocal.ToDoList(
                        mAppModel
                                .recArrayList(mAppModel.getRecord(Integer.valueOf(id))));

                for (ToDoItem itemToDo : items){

                    // Determine if it's a new item
                    itemToDo.newItem(key.isEmpty());

                    if (dbCloud.save(itemToDo)){

                        // Save the key just assigned.
                        if(key.isEmpty()){

                            dbLocal.save(itemToDo);
                        }

                        mDBHelper.delete(rowId);

                        // Note the change now to any other devices.
                        insert(itemToDo.getKey(), action);
                    }
                }
            }
        }



        private static void deviceDirectory(){

            final DatabaseReference deviceRef = getDevRef(Auth.getUid());

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

            return insert(syncRef(Auth.getUid()).child("OUT"), recValues);
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

            final DatabaseReference syncRef = syncRef(Auth.getUid());

            syncRef.child("OUT").orderByChild("key").equalTo(key)
                    .addListenerForSingleValueEvent(new ValueEventListener(){

                        @Override
                        public void onDataChange(DataSnapshot snapshot){

                            DatabaseReference dbOUTRef;

                            dbOUTRef = syncRef.child("OUT");

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



        static ArrayList<HashMap<String, String>> recArrayList(DataSnapshot snapshot){

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

                    for (Object key : fldObj.keySet()){

                        switch (fldObj.get(key).getClass().getName()){

                            case "java.lang.Boolean":

                                fldObj.put(key, (Boolean) fldObj.get(key) ? "true" : "false");

                                break;
                            case "java.lang.Long":

                                fldObj.put(key, Long.toString((Long) fldObj.get(key)));

                                break;
                            case "java.lang.Double":

                                fldObj.put(key, Double.toString((Double) fldObj.get(key)));

                                break;
                            case "java.lang.Integer":

                                fldObj.put(key, Integer.toString((Integer) fldObj.get(key)));
                        }
                    }

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

            mDevRef = null;

            mDatabase = null;

            mAppModel = null;
        }
    }


    private static class onLogin implements FirebaseAuth.AuthStateListener{

        private static onLogin mInstance;

        private static Set<onLoginListener> mLoginListeners = new HashSet<>();

        String mPrevProvider;



        private static void addAuthStateListener(){

            if (appSettings.get("LoggedIn", false)){return;}

            if (mInstance == null){

                mInstance = new onLogin();
            }

            Auth.addAuthStateListener(mInstance);
        }



        private static void removeAuthStateListener(){

            if (mInstance != null){

                Auth.removeAuthStateListener(mInstance);
            }
        }



        private static boolean addOnLoginListener(onLoginListener listener){

            return mLoginListeners.add(listener);
        }



        private static boolean removeOnLoginListener(onLoginListener listener){

            return mLoginListeners.remove(listener);
        }



        public static void onDestroy(){

            if (mLoginListeners != null){

                mLoginListeners.clear();

//                mLoginListeners = null;
            }
        }



        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

            if (appSettings.get("LoggedIn", false)){return;}

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null){ return;}

            String provider = Auth.userProfile(user, "provider");

            boolean onLogin;

            if (mPrevProvider == null){

                mPrevProvider = provider;

                onLogin = false;
            }else{

                onLogin = !mPrevProvider.equals(provider);
            }

            if (onLogin && Auth.isLoggedIn()){

                if (mLoginListeners != null){

                    for (onLoginListener listener : mLoginListeners){

                        listener.onLogin();
                    }
                }

                appSettings.set("LoggedIn", true);
            }
        }
    }




    private static class asyncData extends AsyncTask<HashMap<String, Object>, Void, Boolean>{

        static boolean mRunning = false;



        // Ensure this task is not already running.
        public boolean isRunning(){

            return mRunning;
        }



        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            mRunning = true;
        }



        @Override
        protected Boolean doInBackground(HashMap<String, Object>... params){

            Boolean sync;

            try{

                DataSnapshot snapshot = (DataSnapshot)params[0].get("snapshot");

                dbInterface dbLocal = (dbInterface)params[0].get("dbLocal");

                dbInterface dbCloud = (dbInterface)params[0].get("dbCloud");

                sync = snapshot != null && snapshot.hasChildren();

                if(sync){

                    DataSync.syncData(snapshot, dbLocal, dbCloud);
                }

                sync = getRecs().getCount() > 0;

                if(sync){

                    // Update the dbFirebase database now that there is online access.
                    DataSync.updateCloud(dbLocal, dbCloud);
                }

            }catch (Exception ex){

                sync = false;

                ErrorHandler.logError(ex);
            }

            return sync;
        }



        @Override
        protected void onPostExecute(Boolean synced){

            if(synced){

                mAppModel.getView().refreshList();
            }

            mRunning = false;
        }
    }
}
