package com.gtfp.workingmemory.db;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.andrioussolutions.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.Auth.Auth;
import com.gtfp.workingmemory.app.appView;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Copyright (C) 2016  Greg T. F. Perry
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
public class dbFireBase implements dbInterface, FirebaseAuth.AuthStateListener{


    private appView mAppView;

    private static dbFireBase mThis;

    private FirebaseApp mApp;

    private static FirebaseDatabase mDatabase;

    private static boolean mShowDeleted;

    private static ArrayList<HashMap<String, String>> mDataArrayList;

    private static Cursor mResultSet;

    private static String[] mDataFields;

    private Context mContext;

    private ContentValues mRecValues;

    private String mLastRowID;

    private Set<OnDataListener> mOnDataListeners = new HashSet<>();

    private dbTasksData mTaskData;




    private dbFireBase(appView mVc){

        mAppView = mVc;

        mDatabase = FirebaseDatabase.getInstance();

        mApp = mDatabase.getApp();

        mContext = mVc.getContext();

        // Stores the record's contents.
        mRecValues = new ContentValues();

        Auth.addAuthStateListener(this);

        mTaskData = new dbTasksData();
    }




    public static dbInterface getInstance(appView mVc){

        if (mThis == null){

            mThis = new dbFireBase(mVc);
        }

        return mThis;
    }




    private static ArrayList<HashMap<String, String>> recArrayList(DataSnapshot snapshot){

        // Collected deleted records as well.
        return recArrayList(snapshot, true);
    }




    public static ArrayList<HashMap<String, String>> recArrayList(DataSnapshot snapshot,
            boolean showDeleted){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        if (snapshot == null){

            return list;
        }

        // This is awesome! No middle man!
        Object fieldsObj = new Object();

        HashMap fldObj;

        for (DataSnapshot shot : snapshot.getChildren()){

            if(!shot.hasChildren()){

                continue;
            }

            try{

                fldObj = (HashMap) shot.getValue(fieldsObj.getClass());

                for (Object key : fldObj.keySet()){

                    switch (fldObj.get(key).getClass().getName()){

                        case "java.lang.Boolean":

                            fldObj.put(key, (Boolean)fldObj.get(key) ? "true" : "false");

                            break;
                        case "java.lang.Long":

                            fldObj.put(key, Long.toString((Long)fldObj.get(key)));

                            break;
                        case "java.lang.Double":

                            fldObj.put(key, Double.toString((Double) fldObj.get(key)));
                    }
                }
            }catch (Exception ex){

                ErrorHandler.logError(ex);

                continue;
            }

            // Skip deleted records
            if (!showDeleted && fldObj.containsKey("Deleted") &&
                    fldObj.get("Deleted").equals(Long.parseLong("1"))){

                continue;
            }

            fldObj.put("key", shot.getKey());

            list.add(fldObj);
        }

        return list;
    }




    public static FirebaseDatabase getDatabase(){

        if(mDatabase == null){

            mDatabase = FirebaseDatabase.getInstance();
        }

        return mDatabase;
    }




     static DatabaseReference prevUserIdDBRef(){

         DatabaseReference DBRef;

         String prevUserId = Auth.getPrevUid();

        if (prevUserId == null || prevUserId.isEmpty()){

            DBRef = getDatabase().getReference().child("tasks").child("dummy");
        }else{

            DBRef = getDatabase().getReference().child("tasks").child(prevUserId);
        }

        return DBRef;
    }





    private static DatabaseReference tasksRef(){

        String id = Auth.getUid();

        return tasksRef(id);
    }



    private static DatabaseReference tasksRef(String id){

        DatabaseReference DBRef;

        if(id == null){

            DBRef = getDatabase().getReference().child("tasks").child("dummy");
        }else{

            DBRef = getDatabase().getReference().child("tasks").child(id);
        }

        return DBRef;
    }




    private static boolean isOnline(){

        boolean online;

        online = mThis.mAppView != null;

        if(online){

            // Is connected to the Internet.
            online = mThis.mAppView.getController().NoConnectivity().isEmpty();
        }

        return online;
    }



    public void onAuthStateChanged(@NonNull FirebaseAuth auth){

        // Catch any changes  by other phones.
        tasksRef().addValueEventListener(mTaskData);
    }


//    public boolean save(ToDoItem itemToDo){
//
//        String key = itemToDo.getKey();
//
//        itemToDo.newItem(key.isEmpty());
//
//        if (!itemToDo.newItem()){
//
//            // There has to be an identifier.
//            if (itemToDo.getId() < 1){
//
//                // Assign a unique id for for the Alarm
//                itemToDo.setId(Double.doubleToLongBits(Math.random()));
//            }
//
//            return updateRec(itemToDo) > 0;
//        }else{
//
//            // Assign a unique id for for the Alarm
//            itemToDo.setId(doubleToLongBits(Math.random()));
//
//            key = insertRec(itemToDo);
//
//            itemToDo.setKey(key);
//
//            return !key.isEmpty();
//        }
//    }



    @Override
    public dbInterface open(){

        // No internet connection is available.
        if (!isOpen()){ return this; }

        try{

            // TODO What happens when the connection is closed. Does that mean a requery??
            mDatabase.goOnline();

//            // Catch any changes  by other phones.
//            tasksRef().addValueEventListener(mTaskData);

        }finally{}

        return this;
    }



    public dbInterface open(OnDataListener listener){

        setOnDataListener(listener);

        return open();
    }




    @Override
    public boolean isOpen(){

        return isOnline();
    }



    @Override
    public void close(){

    }



    public void goOffline(){

        if (mDatabase != null){

            mDatabase.goOffline();
        }
    }



    @Override
    public boolean createCurrentRecs(){

        if (Semaphore.got()){

            return true;
        }

        Query queryRef = tasksRef().orderByKey();

        if (queryRef == null){ return false; }

        // Just one query
        queryRef.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot snapshot){

                if (snapshot == null){

                    return;
                }

                mDataArrayList = recArrayList(snapshot, mShowDeleted);

                mAppView.onDataChange(snapshot);
            }



            @Override
            public void onCancelled(DatabaseError databaseError){

                String error = databaseError.getMessage();

                mAppView.onCancelled(databaseError);
            }
        });

        return true;
    }




    @Override
    public void clearResultSet(){

        int breakpoint = 0;
    }



    @Override
    public Cursor getResultSet(){

        if (mDataFields == null){

            Field[] fields = Tasks.class.getDeclaredFields();

            List<String> columns = new ArrayList<>();

            for (int i = 0; i < fields.length; i++){

                DesiredField annotation = fields[i].getAnnotation(DesiredField.class);

                if (annotation != null){

                    columns.add(fields[i].getName());
                }
            }

            mDataFields = new String[columns.size()];

            int cnt = 0;

            for (String col : columns){

                mDataFields[cnt] = col;

                cnt++;
            }
        }

        MatrixCursor dataMatrix = new MatrixCursor(mDataFields);

        if (mDataArrayList != null && mDataArrayList.size() > 0){

            Set<String> keys = mDataArrayList.get(0).keySet();

            MatrixCursor.RowBuilder nextRow;

            for (HashMap<String, String> rec : mDataArrayList){

                nextRow = dataMatrix.newRow();

                for (String key : keys){

                    nextRow.add(key, rec.get(key));
                }
            }
        }

        mResultSet = dataMatrix;

        return mResultSet;
    }



    @Override
    public boolean showDeleted(boolean showDeleted){

        // Show deleted records means NOT to use the view.
        mShowDeleted = showDeleted;

        return showDeleted;
    }



    @Override
    public Cursor runQuery(String sqlStmt){

        return null;
    }



    @Override
    public Cursor getRecs(){

        return null;
    }



    @Override
    public Cursor getRecord(int rowId){return null; }



    @Override
    public Cursor getCurrentRecs(){

        return null;
    }



    @Override
    public Cursor getDeletedRecs(){

        return null;
    }



    @Override
    public String getLastRowID(){

        return mLastRowID;
    }



    @Override
    public int deleteRec(Long rowID){

        return 0;
    }



    @Override
    public boolean save(ToDoItem itemToDo){

        boolean save;

        if (itemToDo.newItem()){

            String key = insertRec(itemToDo);

            itemToDo.setKey(key);

            save = !key.isEmpty();
        }else{

            save = updateRec(itemToDo) > 0;
        }

        if(save){

            Semaphore.write();
        }

        return save;
    }



    @Override
    public boolean markRec(Long id){ return false;}



    public boolean markRec(final String key){

        if (key == null || key.isEmpty()){

            // Considered successful.
            return true;
        }

        final DatabaseReference tasksDB = tasksRef();

        // Query the user's tasks.
        Query queryRef = tasksDB.orderByKey();

        queryRef.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot snapshot){

                if (snapshot == null){

                    return;
                }

                try{

                    DatabaseReference rec =  tasksDB.child(key);

                    // Delete that record
                    rec.removeValue();

                    // Retain the semaphore.
                    Semaphore.write();

                }catch (Exception ex){

                    ErrorHandler.logError(ex);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError){}
        });

        // Assume it always works.
        return true;
    }



    @Override
    public int updateRec(ToDoItem itemToDo){

        // Override the interface to return a 1 when successful.
        int result = 1;

        Map<String, Object> childUpdates = new HashMap<>();

        String key = itemToDo.getKey();

        try{

            // Special case when there's no corresponding  record on the cloud.
            if (key.isEmpty()){

                result = insertRec(itemToDo).isEmpty() ? 0 : 1;
            }else{

                childUpdates.put(key, toMap(itemToDo));

                tasksRef().updateChildren(childUpdates, new DatabaseReference.CompletionListener(){

                    @Override
                    public void onComplete(DatabaseError databaseError,
                            DatabaseReference databaseReference){

                        if (databaseError != null){

                            ErrorHandler.logError(databaseError.toException());
                        }
                    }
                });
            }

        }catch (Exception ex){

            ErrorHandler.logError(ex);

            result = 0;
        }

        return result;
    }



    @Override
    public int updateRec(ContentValues recValues, long id){

        int result = 0;

        return result;
    }



    private String insertRec(ToDoItem itemToDo){

        String key = "";

        DatabaseReference dbRef;

        try{

            dbRef = tasksRef();

            key =  dbRef.push().getKey();

            mLastRowID = key;

            Map<String, Object> childUpdates = new HashMap<>();

            childUpdates.put(key, toMap(itemToDo));

            dbRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener(){

                @Override
                public void onComplete(DatabaseError databaseError,
                        DatabaseReference databaseReference){

                    if (databaseError != null){

                        ErrorHandler.logError(databaseError.toException());
                    }
                }
            });

        }catch (Exception ex){

            key = "";
        }

        return key;
    }



    public long insertRec(ContentValues recValues){

        return 0L;
    }



    // Assign values for each row.
    public ContentValues bindRecValues(ToDoItem itemToDo){

        mRecValues.put("_id", itemToDo.getId());

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

        if (columnName.equals("_id")){

            mRecValues.put(columnName, value);

        }else if (columnName.equals("ToDoItem")){

            mRecValues.put(columnName, value);

        }else if (columnName.equals("ToDoDateTime")){

            mRecValues.put(columnName, value);

        }else if (columnName.equals("ToDoDateTimeEpoch")){

            mRecValues.put(columnName, dbSQLlite.setBindRecLong(value));

        }else if (columnName.equals("ToDoTimeZone")){

            mRecValues.put(columnName, dbSQLlite.setBindRecInt(value));

        }else if (columnName.equals("ToDoReminderEpoch")){

            mRecValues.put(columnName, dbSQLlite.setBindRecLong(value));

        }else if (columnName.equals("ToDoReminderChk")){

            mRecValues.put(columnName, dbSQLlite.setBindRecInt(value));

        }else if (columnName.equals("ToDoLEDColor")){

            mRecValues.put(columnName, dbSQLlite.setBindRecInt(value));

        }else if (columnName.equals("ToDoFired")){

            mRecValues.put(columnName, dbSQLlite.setBindRecInt(value) != 0);

        }else if (columnName.equals("ToDoSetAlarm")){

            mRecValues.put(columnName, dbSQLlite.setBindRecInt(value) != 0);

        }else if (columnName.equals("deleted")){

            mRecValues.put(columnName, dbSQLlite.setBindRecInt(value) != 0);
        }
    }



    private Tasks setTask(ToDoItem item){

        long epochTime = item.getDueDateInEpoch();

        return new Tasks(
                item.getId()
                , item.getItemName()
                , ToDoItem.EpochToDateTime(epochTime)
                , epochTime
                , 0
                , item.getReminderEpoch()
                , item.getReminderChk()
                , item.getLEDColor()
                , item.hasFired() ? 1 : 0
                , item.isDeleted() ? 1 : 0);
    }



    private Map<String, Object> toMap(ToDoItem item){

        HashMap<String, Object> map = new HashMap<>();

        map.put("ToDoID", item.getId());

        map.put("ToDoItem", item.getItemName());

        long epochTime = item.getDueDateInEpoch();

        map.put("ToDoDateTime", ToDoItem.EpochToDateTime(epochTime));

        map.put("ToDoDateTimeEpoch", Long.toString(epochTime));

        map.put("ToDoTimeZone", "0");

        map.put("ToDoReminderEpoch", Long.toString(item.getReminderEpoch()));

        map.put("ToDoReminderChk", Integer.toString(item.getReminderChk()));

        map.put("ToDoLEDColor", Integer.toString(item.getLEDColor()));

        map.put("ToDoFired", item.hasFired() ? "1" : "0");

        map.put("Deleted", item.isDeleted() ? "1" : "0");

        return map;
    }



    @Override
    public boolean createEmptyTemp(){

        return false;
    }



    @Override
    public long insertTempRec(){

        return 0;
    }



    @Override
    public Cursor getNewTempRecs(){

        return null;
    }



    @Override
    public boolean ifNewRec(){ return false;}



    @Override
    public boolean importRec(){

        return false;
    }



    @Override
    public Cursor getRecs(String whereClause){

        return null;
    }



    public ArrayList<HashMap<String, String>> getDataArrayList(){

        return mDataArrayList;
    }



    public ArrayList<ToDoItem> ToDoList(ArrayList<HashMap<String, String>> recs){

        ArrayList<ToDoItem> todoList = new ArrayList<>();

        // If these were my own changes ignore.
        if (Semaphore.got()){

            return todoList;
        }

        if (recs == null || recs.size() == 0){

            return todoList;
        }

        ToDoItem todoItem;

        HashMap<String, String> rec = recs.get(0);

        Set<String> keys = rec.keySet();

        boolean id, keyID, item, dtEpoch, rmEpoch, rmChk, LEDColor, hasFired, setAlarm, deleted;

        for (HashMap<String, String> todoRec : recs){

            todoItem = new ToDoItem();

            id = keyID = item = dtEpoch = rmEpoch = rmChk = LEDColor = hasFired = setAlarm = deleted
                    = true;

            for (String key : keys){

                if (id && key.equals("ToDoID")){

                    todoItem.setId(todoRec.get(key));

                    id = false;

                }else if (keyID && key.equals("key")){

                    todoItem.setKey(todoRec.get(key));

                    keyID = false;
                }else if (item && key.equals("ToDoItem")){

                    todoItem.setItemName(todoRec.get(key));

                    item = false;
                }else if (dtEpoch && key.equals("ToDoDateTimeEpoch")){

                    todoItem.setDueDate(Long.valueOf(todoRec.get(key)));

                    dtEpoch = false;
                }else if (rmEpoch && key.equals("ToDoReminderEpoch")){

                    todoItem.setReminderEpoch(Long.valueOf(todoRec.get(key)));

                    rmEpoch = false;
                }else if (rmChk && key.equals("ToDoReminderChk")){

                    todoItem.setReminderChk(Integer.valueOf(todoRec.get(key)));

                    rmChk = false;
                }else if (LEDColor && key.equals("ToDoLEDColor")){

                    todoItem.setLEDColor(Integer.valueOf(todoRec.get(key)));

                    LEDColor = false;
                }else if (hasFired && key.equals("ToDoFired")){

                    todoItem.hasFired(todoRec.get(key).equals("1"));

                    hasFired = false;
                }else if (setAlarm && key.equals("ToDoSetAlarm")){

                    todoItem.setAlarm(todoRec.get(key).equals("1"));

                    setAlarm = false;
                }else if (deleted && key.equals("Deleted")){

                    todoItem.isDeleted(todoRec.get(key).equals("1"));

                    deleted = false;
                }
            }

            todoList.add(todoItem);
        }

        return todoList;
    }




    public boolean setOnDataListener(OnDataListener listener){

        return mOnDataListeners.add(listener);
    }




    public boolean removeOnDataListener(OnDataListener listener){

        return mOnDataListeners.remove(listener);
    }




    public static void sync(){

        Sync.data();
    }




    @Override
    public void onDestroy(){

        goOffline();

        mAppView = null;

        mThis = null;

        mContext = null;

        mDatabase = null;

        mApp = null;

        mOnDataListeners.clear();

        mOnDataListeners = null;
    }




    public interface OnDataListener{

        void onDownload(ArrayList<HashMap<String, String>> dataArrayList);
    }

    @Target(value = ElementType.FIELD)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface DesiredField{}

    // @IgnoreExtraProperties
    // Must be static to work within a class file and Firebase!
    public static class Tasks{

        @DesiredField
        private Long ToDoID;

        @DesiredField
        private String ToDoItem;

        @DesiredField
        private String ToDoDateTime;

        @DesiredField
        private Long ToDoDateTimeEpoch;

        @DesiredField
        private Integer ToDoTimeZone = 0;

        @DesiredField
        private Long ToDoReminderEpoch;

        @DesiredField
        private Integer ToDoReminderChk = 0;

        @DesiredField
        private Long ToDoLEDColor = 0L;

        @DesiredField
        private Integer ToDoFired = 0;

        @DesiredField
        private Integer Deleted = 0;



        public Tasks(){
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }



        public Tasks(
                long ToDoID
                , String ToDoItem
                , String ToDoDateTime
                , long ToDoDateTimeEpoch
                , int ToDoTimeZone
                , long ToDoReminderEpoch
                , int ToDoReminderChk
                , long ToDoLEDColor
                , int ToDoFired
                , int Deleted){

            this.ToDoID = ToDoID;
            this.ToDoItem = ToDoItem;
            this.ToDoDateTime = ToDoDateTime;
            this.ToDoDateTimeEpoch = ToDoDateTimeEpoch;
            this.ToDoTimeZone = ToDoTimeZone;
            this.ToDoReminderEpoch = ToDoReminderEpoch;
            this.ToDoReminderChk = ToDoReminderChk;
            this.ToDoLEDColor = ToDoLEDColor;
            this.ToDoFired = ToDoFired;
            this.Deleted = Deleted;
        }



        public long getToDoID(){

            return ToDoID;
        }



        public String getToDoItem(){

            return ToDoItem;
        }



        public String getToDoDateTime(){

            return ToDoDateTime;
        }



        public long getToDoDateTimeEpoch(){

            return ToDoDateTimeEpoch;
        }



        public int getToDoTimeZone(){

            return ToDoTimeZone;
        }



        public long getToDoReminderEpoch(){

            return ToDoReminderEpoch;
        }



        public int getToDoReminderChk(){

            return ToDoReminderChk;
        }



        public long getToDoLEDColor(){

            return ToDoLEDColor;
        }



        public int getToDoFired(){

            return ToDoFired;
        }



        public int getDeleted(){

            return Deleted;
        }



        @Exclude
        public Map<String, Object> toMap(){

            HashMap<String, Object> result = new HashMap<>();

            result.put("ToDoID", ToDoID);

            result.put("ToDoItem", ToDoItem);

            result.put("ToDoDateTime", ToDoDateTime);

            result.put("ToDoDateTimeEpoch", ToDoDateTimeEpoch);

            result.put("ToDoTimeZone", ToDoTimeZone);

            result.put("ToDoReminderEpoch", ToDoReminderEpoch);

            result.put("ToDoReminderChk", ToDoReminderChk);

            result.put("ToDoLEDColor", ToDoLEDColor);

            result.put("ToDoFired", ToDoFired);

            result.put("Deleted", Deleted);

            return result;
        }
    }




    private class dbTasksData implements ValueEventListener{

        @Override
        public void onDataChange(DataSnapshot snapshot){

            if (snapshot == null){

                return;
            }

            // If these were my own changes ignore.
            if (Semaphore.got(snapshot)){

                 return;
            }

            // List Firebase data
            mDataArrayList = recArrayList(snapshot, mShowDeleted);

            if (mOnDataListeners != null){

                for (OnDataListener listener : mOnDataListeners){

                    listener.onDownload(mDataArrayList);
                }
            }

            if(mAppView != null)  mAppView.onDataChange(snapshot);
        }



        @Override
        public void onCancelled(DatabaseError databaseError){

            String error = databaseError.getMessage();

            if(mAppView != null)  mAppView.onCancelled(databaseError);
        }
    }




    private static class Sync{

        static void data(){

        }
    }
}
