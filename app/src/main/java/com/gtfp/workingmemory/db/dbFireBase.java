package com.gtfp.workingmemory.db;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.gtfp.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.Auth.Auth;
import com.gtfp.workingmemory.app.appView;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Drawn on 9/27/2016.
 */

public class dbFireBase implements dbInterface{


    private static appView mAppView;

    private static dbInterface mThis;

    private static FirebaseApp mApp;

    private static FirebaseDatabase mDatabase;

    private static boolean mShowDeleted;

    private static ArrayList<HashMap<String, String>> mDataArrayList;

    private static boolean mIgnoreTrigger = false;

    private static Cursor mResultSet;

    private static String[] mDataFields;

//    private static ArrayList<HashMap<String, String>> recArrayList(DataSnapshot snapshot,
//            boolean showDeleted){
//
//        ArrayList<HashMap<String, String>> list = new ArrayList<>();
//
//        if (snapshot == null){
//
//            return list;
//        }
//
//        Tasks task;
//
//        DesiredField annotation;
//
//        Field[] fields = Tasks.class.getDeclaredFields();
//
//        Method[] methods = Tasks.class.getDeclaredMethods();
//
//        String value;
//
//        String fldName;
//
//        long number;
//
//        int integer;
//
//        boolean skip = false;
//
//        for (DataSnapshot shot : snapshot.getChildren()){
//
//            try{
//
//                task = shot.getValue(Tasks.class);
//
//            }catch (Exception ex){
//
//                ErrorHandler.logError(ex);
//
//                continue;
//            }
//
//            HashMap<String, String> row = new HashMap<>();
//
//            row.put("key", shot.getKey());
//
//            for (int i = 0; i < fields.length; i++){
//
//                annotation = fields[i].getAnnotation(DesiredField.class);
//
//                if (annotation == null){
//
//                    continue;
//                }
//
//                try{
//
//                    switch (methods[i].getReturnType().getName()){
//                        case "long":
//
//                            number = (Long) methods[i].invoke(task);
//
//                            value = Long.toString(number);
//
//                            break;
//
//                        case "int":
//
//                            integer = (Integer) methods[i].invoke(task);
//
//                            value = String.valueOf(integer);
//
//                            break;
//                        default:
//
//                            value = (String) methods[i].invoke(task);
//                    }
//
//                    fldName = fields[i].getName();
//
//                    if (!showDeleted && fldName.equals("Deleted") && value.equals("1")){
//
//                        skip = true;
//
//                        break;
//                    }
//                }catch (Exception ex){
//
//                    ErrorHandler.logError(ex);
//
//                    continue;
//                }
//
//                row.put(fldName, value);
//            }
//
//            if (skip){
//
//                skip = false;
//
//                continue;
//            }
//
//            list.add(row);
//        }
//
//        return list;
//    }

    private Context mContext;

    private DatabaseReference mUserTasks;

    private DataSnapshot mDataSnapshot;

    private ContentValues mRecValues;

    private String mLastRowID;

    private Set<OnDataListener> mOnDataListeners = new HashSet<>();



    private dbFireBase(Context context){

        mContext = context;

        // Stores the record's contents.
        mRecValues = new ContentValues();
    }



    public static dbInterface getInstance(appView mVc){

        if (mThis == null){

            mAppView = mVc;

            mThis = new dbFireBase(mVc.getContext());

            mDatabase = FirebaseDatabase.getInstance();

            mApp = mDatabase.getApp();
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

            try{

                fldObj = (HashMap) shot.getValue(fieldsObj.getClass());

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



    private static ArrayList<HashMap<String, Object>> getRecList(DataSnapshot snapshot){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        if (snapshot == null){

            return list;
        }

        Tasks task;

        DesiredField annotation;

        Field[] fields = Tasks.class.getDeclaredFields();

        Method[] methods = Tasks.class.getDeclaredMethods();

        for (DataSnapshot shot : snapshot.getChildren()){

            try{

                task = shot.getValue(Tasks.class);

            }catch (Exception ex){

                ErrorHandler.logError(ex);

                continue;
            }

            HashMap<String, Object> row = new HashMap<>();

            row.put("key", shot.getKey());

            for (int i = 0; i < fields.length; i++){

                annotation = fields[i].getAnnotation(DesiredField.class);

                if (annotation == null){

                    continue;
                }

                try{

                    switch (methods[i].getReturnType().getName()){
                        case "long":

                            row.put(fields[i].getName(),
                                    Long.valueOf(methods[i].invoke(task).toString()));

                            break;

                        case "int":

                            row.put(fields[i].getName(),
                                    Integer.valueOf(methods[i].invoke(task).toString()));

                            break;
                        default:

                            row.put(fields[i].getName(), methods[i].invoke(task));
                    }
                }catch (Exception ex){

                    ErrorHandler.logError(ex);
                }
            }

            list.add(row);
        }

        return list;
    }



    @Override
    public dbInterface open(){

        if (isOpen()){ return this; }

        // No internet connection is available.
        if (!mAppView.getController().NoConnectivity().isEmpty()){return this;}

        // TODO What happens when the connection is closed. Does that mean a requery??
        mDatabase.goOnline();

        try{

            if (mUserTasks == null){

                mUserTasks = mDatabase.getReference().child("tasks").child(Auth.getUid());

                addUserTasksListener(mUserTasks);
            }
        }catch (Exception ex){

            mUserTasks = null;
        }

        return this;
    }



    public dbInterface open(OnDataListener listener){

        setOnDataListener(listener);

        return open();
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
    public boolean isOpen(){

        return mUserTasks != null;
    }



    @Override
    public void close(){

        if (mDatabase != null){

            mDatabase.goOffline();
        }
    }



    @Override
    public boolean createCurrentRecs(){

        if (mIgnoreTrigger){

            mIgnoreTrigger = false;

            return true;
        }

        // Prevent an immediate query. Wait for onDataChange()
//        mIgnoreTrigger = true;

        Query queryRef = mUserTasks.orderByKey();

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



    // Catch any changes  by other phones.
    private boolean addUserTasksListener(DatabaseReference dbRef){

        if (dbRef == null){ return false; }

        // Perform the retrieval right away.
        mIgnoreTrigger = false;

        dbRef.addValueEventListener(

                new ValueEventListener(){

                    @Override
                    public void onDataChange(DataSnapshot snapshot){

                        if (snapshot == null){

                            return;
                        }

                        // If these were my own changes ignore.
                        if (mIgnoreTrigger){

                            mIgnoreTrigger = false;

                            return;
                        }

                        mDataArrayList = recArrayList(snapshot, mShowDeleted);

                        if (mOnDataListeners != null){

                            for (OnDataListener listener : mOnDataListeners){

                                listener.onDownload(mDataArrayList);
                            }
                        }

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

        if (itemToDo.newItem()){

            String key = insertRec(itemToDo);

            itemToDo.setKey(key);

            return !key.isEmpty();
        }else{

            return updateRec(itemToDo) > 0;
        }
    }


    @Override
    public boolean markRec(Long id){ return false;}




    public boolean markRec(final String key){

        if (key == null || key.isEmpty()){

            // Considered successful.
            return true;
        }

        // Query the user's tasks.
        Query queryRef = mUserTasks.orderByKey();

        queryRef.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot snapshot){

                if (snapshot == null){

                    return;
                }

//                HashMap<String, Object> rec = new HashMap<>();

                // This is awesome! No middle man!
                Object fieldsObj = new Object();

                HashMap fldObj = null;

                for (DataSnapshot item : snapshot.getChildren()){

                    if (item.getKey().equals(key)){

                        try{

                            fldObj = (HashMap) item.getValue(fieldsObj.getClass());

//                            Iterator it = fldObj.entrySet().iterator();
//
//                            while (it.hasNext()) {
//
//                                Map.Entry pair = (Map.Entry)it.next();
//
//                                System.out.println(pair.getKey() + " = " + pair.getValue());
//
//                                it.remove(); // avoids a ConcurrentModificationException
//                            }

                            break;

                        }catch (Exception ex){

                            break;
                        }
                    }
                }

                if (fldObj == null || fldObj.size() == 0){ return; }

                fldObj.put("Deleted", 1);

                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put(key, fldObj);

                try{

                    mUserTasks.updateChildren(childUpdates);

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



    private HashMap<String, Object> getValue(DataSnapshot snapshot, String keyFld, long keyValue){

        HashMap<String, Object> value = new HashMap<>();

        ArrayList<HashMap<String, Object>> arrayList = getRecList(snapshot);

        if (arrayList == null || arrayList.size() == 0){

            return value;
        }

        for (HashMap<String, Object> rec : arrayList){

            if ((Long) rec.get(keyFld) == keyValue){

                value = rec;

                break;
            }
        }

        return value;
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

                childUpdates.put(key, setTask(itemToDo).toMap());

                mIgnoreTrigger = true;

                mUserTasks.updateChildren(childUpdates);
            }

        }catch (Exception ex){

            mIgnoreTrigger = false;

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

        try{

            key = mUserTasks.push().getKey();

            mLastRowID = key;
//
            Map<String, Object> childUpdates = new HashMap<>();
//
////            childUpdates.put(Auth.getUid() + "/" + key, task);

            childUpdates.put(key, setTask(itemToDo).toMap());

            mIgnoreTrigger = true;

            mUserTasks.updateChildren(childUpdates);

/*            mUserTasks.push().setValue(task, new DatabaseReference.CompletionListener(){

                @Override
                public void onComplete(DatabaseError databaseError,
                        DatabaseReference databaseReference){

                    if (databaseError != null){

                        System.out.println("Data could not be saved " + databaseError.getMessage());

                    }else{

                        System.out.println("Data saved successfully.");
                    }
                }
            });*/

        }catch (Exception ex){

            mIgnoreTrigger = false;

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
        if (mIgnoreTrigger){

            mIgnoreTrigger = false;

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



    @Override
    public void onDestroy(){

        close();

        mAppView = null;

        mThis = null;

        mContext = null;

        mDatabase = null;

        mApp = null;

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
}
