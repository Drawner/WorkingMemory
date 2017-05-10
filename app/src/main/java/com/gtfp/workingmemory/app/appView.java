package com.gtfp.workingmemory.app;
/**
 * Created by Drawn on 2015-02-07.
 */

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.gtfp.errorhandler.CrashFragment;
import com.gtfp.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.Auth.Auth;
import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.db.dbFireBase;
import com.gtfp.workingmemory.edit.appCRUD;
import com.gtfp.workingmemory.edit.rowView;
import com.gtfp.workingmemory.google.LegalNoticesActivity;
import com.gtfp.workingmemory.google.SignInActivity;
import com.gtfp.workingmemory.settings.appSettings;
import com.gtfp.workingmemory.todo.ToDoAlarm;
import com.gtfp.workingmemory.todo.ToDoItem;
import com.gtfp.workingmemory.todo.ToDoItemsList;
import com.gtfp.workingmemory.todo.ToDoListAdapter;
import com.gtfp.workingmemory.utils.dialog;
import com.gtfp.workingmemory.wallpaper.Wallpaper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class appView implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener, dialog.DialogBoxListener,
        FirebaseAuth.AuthStateListener, ValueEventListener{


    private static final String ALARM_ID = "id";

    private static final int CONTENT_VIEW_ID = View.generateViewId();

    private static String PACKAGE_NAME;

    private static boolean mClearNotification;

    private static boolean mSyncData;

    private static boolean mRestoreSync;

    private static boolean mDeleteSync = false;

    private final int REQUEST_CODE = 1;

    private final int GOOGLE_REQUEST = 2;

    private final int ERROR_REQUEST = 3;

    public appController mController;

    public appModel mAppModel;

    public ToDoListAdapter mToDoListAdapter;

    public ToDoAlarm.Manager mAlarmManager;

    public Wallpaper mItemsOnWallpaper;

    FragmentManager mFragMngr;

    Context mContext;

//    private googleSignIn mGoogleSignIn;

    private boolean mFirstCall = true;

    private appCRUD mAppCRUD;

    private ListView mTaskList;

    private int mToDoListID = -1;

    private Intent mIntent;

    private boolean mTrulyDelete = false;

    private boolean mSigMatch;

    private File mSyncFile;

    private appSettings mAppSettings;

    private dialog mDialogue;

    private Calendar mToday;

    private MenuItem mMenuItem;

    private MenuItem mErrorMenuItem;

    private MenuItem mThrowErrorMenu;

    private CharSequence mItemTitle;

    private FrameLayout mFrameLayout;

    public appView(appController app){
        this((Context) app);

        mController = app;

        // TODO Need a better way to read the Preferences.xml.
        mAppSettings = new appSettings(this);

        mFragMngr = mController.getFragmentManager();

        // Sign into Google.
//        mGoogleSignIn = new googleSignIn(mContext);

        mItemsOnWallpaper = new Wallpaper(app);

        // Created here when there is not activity/app running but a dialogue window to delete the task.
        mAppCRUD = new appCRUD(this);

        mClearNotification = appSettings.getBoolean("clear_notification", false);

        mSyncData = appSettings.getBoolean("sync_data", false);
    }

    // This is called when there is not activity available.
    public appView(Context context){

        mContext = context;

        mAppModel = new appModel(this);

        // Set up the alarm system.
        mAlarmManager = new ToDoAlarm.Manager(mContext);

        mDialogue = new dialog(mContext);

        mDialogue.setDialogBoxListener(this);

        // Must initialize the Calendar here in this method.
        mToday = Calendar.getInstance();

        // Provide this App's label to a 'global' Static reference.
        App.setLabel(context);

        App.setModel(mAppModel);

        App.setView(this);
    }

    // Used by external components.
    public static boolean showDialogue(){

        return appSettings.getBoolean("popup_notification", true);
    }

    // Used by external components.
    public static boolean clearNotification(){

        return mClearNotification;
    }

    // Called by the controller
    protected void onCreate(Bundle savedInstanceState){

        mController.setContentView(R.layout.activity_main);

//        mFrameLayout = new FrameLayout(mController);
//
//        mFrameLayout.setId(CONTENT_VIEW_ID);
//
//        mController.addContentView(mFrameLayout, new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        Button btnNewToDo = (Button) mController.findViewById(R.id.btnNewToDo);

        // The New button.
        btnNewToDo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                mAppCRUD.newToDoItem();
            }
        });

        mTaskList = (ListView) mController.findViewById(R.id.lvToDos);

        // One click will edit that selected item.
        mTaskList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId){

                mToDoListID = position;

                mAppCRUD.edit(position);
            }
        });

        // Let's not and see if they're saved by ToDoAlarm.BroadcastReceiver
        // Ensure any existing items have their alarms set.
        //  setAlarms(mToDoListAdapter.ToDoList());

        // Context menu
        mController.registerForContextMenu(mTaskList);

        //  Possibly called by an alarm to edit an existing item
        editAlarm(mController.getIntent());

//        syncData();
    }

    // Called by the controller.
    boolean onCreateOptionsMenu(Menu menu){

        mController.getMenuInflater().inflate(R.menu.options_menu, menu);

        // Hides a segment of the menu popup.
//        if (!BuildConfig.DEBUG){
//
//            menu.removeGroup(R.id.error_group);
//        }
        return true;
    }

    // Called by the Controller.
    boolean onOptionsItemSelected(MenuItem item){

        String text;

        switch (item.getItemId()){
            case R.id.settings:

                runFragment(new appSettings.appPreferences(), android.R.id.content);

                return true;

            case R.id.login:

                Auth.addAuthStateListener(new signInApp());

                return true;

            case R.id.logout:

                Auth.signOut();

                return true;

            case R.id.sync:

                syncData("get");

                return true;
//            case R.id.errors:
//
//                listErrors();
//
//                return true;
//
//
//            case R.id.deleteSync:
//
//                deleteSync();
//
//                return true;
//
//            case R.id.help:
//
//                text = "Help Context coming soon...";
//
//                try{
//
//                    mDialogue.showBox(text);
//
//                }catch (NullPointerException ex){
//
//                    return false;
//                }
//
//                return true;

            case R.id.about:

                text = "Working Memory\n"
                        + "\nCopyright (c) 2017, G Perry, Toronto, ON., Canada"
                        + "\n\nDistributed \"AS IS\","
                        + "\nWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND."
                        + "\n\nVersion " + versionOfApp();

                mDialogue.showBox(text);

                return true;

            case R.id.legal:

                runActivity(LegalNoticesActivity.class);

            default:

                return false;
        }
    }

    // Called every time the menu is clicked on.
    boolean onPrepareOptionsMenu(Menu menu){

        if (App.inDebugger()){

            onErrorMenu(menu);
        }

        if(!Auth.isLoggedIn()){

            menu.removeItem(R.id.logout);

            menu.removeItem(R.id.sync);
        }

        onThrowError(menu);

        mSigMatch = 174992623 == mController.getSignature();

        return true;
    }




    void onErrorMenu(Menu menu){

        if (mErrorMenuItem == null){

            MenuItem item;

            for (int cnt = 0; cnt < menu.size(); cnt++){

                item = menu.getItem(cnt);

                if (item.getTitle().equals("Errors")){

                    mErrorMenuItem = item;

                    mItemTitle = item.getTitle();

                    break;
                }
            }
        }

        if (mErrorMenuItem != null){

            int count = ErrorHandler.getErrorCount();

            mErrorMenuItem.setVisible(count > 0);

            if (count == 0){

                mErrorMenuItem.setTitle(mItemTitle);
            }else{

                mErrorMenuItem.setTitle(mItemTitle + " (" + String.valueOf(count) + ")");
            }
        }
    }

    void onThrowError(Menu menu){

        if (mThrowErrorMenu == null){

            MenuItem item;

            for (int cnt = 0; cnt < menu.size(); cnt++){

                item = menu.getItem(cnt);

                if (item.getTitle().equals("Throw Error")){

                    mThrowErrorMenu = item;

                    mThrowErrorMenu
                            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

                                public boolean onMenuItemClick(MenuItem item){

                                    //** Invoke an error
                                    mThrowErrorMenu = null;

                                    int id = mThrowErrorMenu.getItemId();

                                    return true;
                                }
                            });

                    mThrowErrorMenu.setVisible(App.inDebugger());

                    break;
                }
            }
        }
    }

    // Return the results from another Activity
    // Called by the controller
    void onActivityResult(int requestCode, int resultCode, Intent data){

        // REQUEST_CODE is defined above
        if (resultCode != Activity.RESULT_OK){

            return;
        }

        if (requestCode == REQUEST_CODE){

            ToDoItem todoItem = (ToDoItem) data.getSerializableExtra("item");

            //mAppCRUD.save(todoItem);
            saveEdit(todoItem);

        }else if (requestCode == GOOGLE_REQUEST){

            ArrayList<CharSequence> memories = data.getCharSequenceArrayListExtra("data");
        }
    }

    boolean saveEdit(ToDoItem itemToDo){

        // In case it WAS marked as deleted.
        itemToDo.isDeleted(false);

        return save(itemToDo);
    }

    public boolean save(ToDoItem itemToDo){

        // In case it WAS marked as deleted.
        //itemToDo.isDeleted(false);

        boolean saved = mAppModel.save(itemToDo);

        if (!saved){

            return saved;
        }

        // It's a new item
        if (itemToDo.newItem()){

            //           itemToDo.setId(mAppModel.getLastRowID());

            mToDoListAdapter.add(itemToDo);

            restoreSync();
        }else{

            mToDoListAdapter.save(itemToDo);

            // Remove any associated notification.
            mAlarmManager.cancelNotification(itemToDo);
        }

        // In some circumstances, an alarm is not set.
        if (itemToDo.setAlarm()){

            // Establish the alarm for this item.
            mAlarmManager.setAlarm(itemToDo);
        }

        if (appSettings.getBoolean("set_as_wallpaper", false)){

            mItemsOnWallpaper.set();
        }

        return saved;
    }




    public boolean delete(int todoListID){

        // Flag to permanently delete a record.
        boolean trueDelete = false;

        boolean deleted = false;

        // Special case: When the app is not running.
        if (mToDoListAdapter == null){

            mToDoListAdapter = new ToDoListAdapter(mAppModel.ToDoList());
        }

        // Retrieve the currently 'selected' item.
        ToDoItem todoItem = (ToDoItem) mToDoListAdapter.getItem(todoListID);

        todoItem.setListPosn(todoListID);

        boolean alreadyDeleted = todoItem.isDeleted();

        if (alreadyDeleted){

            mToDoListID = todoListID;

            mDialogue.show("Permanently delete this already deleted record?");
        }else{

            if (mTrulyDelete){

                // Important to reset in case of failure.
                mTrulyDelete = false;

                trueDelete = true;

                deleted = mAppModel.trueDelete(todoItem);
            }else{

                deleted = mAppModel.delete(todoItem);
            }
        }

        if (deleted){

            // Remove from listing if NOT to show deleted records
            if (!trueDelete && !alreadyDeleted && showDeleted()){

                mToDoListAdapter.save(todoItem);
            }else{

                mToDoListAdapter.remove(todoItem);
            }

            mAlarmManager.cancelAlarm(todoItem);

//            restoreSync();
        }

        return deleted;
    }




    public void refreshList(){

        //        mToDoListAdapter = new ToDoListAdapter(mAppModel.ToDoList());
        mToDoListAdapter.setItems(mAppModel.ToDoList());

        mTaskList.setAdapter(mToDoListAdapter);
    }




    // Called by the controller
    void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo){

        if (v.getId() == R.id.lvToDos){

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            mToDoListID = info.position;

            menu.clearHeader();

            menu.setHeaderTitle(getRowTitle(info));

            mController.getMenuInflater().inflate(R.menu.context_menu, menu);
        }
    }




    // Called by the controller
    boolean onContextItemSelected(MenuItem item){

        // Select the menu
        switch (item.getItemId()){
            case R.id.edit:

                mAppCRUD.edit(mToDoListID);

                return true;

            case R.id.delete:

//                mAppCRUD.delete(mToDoListID);
                delete(mToDoListID);

                return true;
            default:

                return mController.getParent().onContextItemSelected(item);
        }
    }




    public boolean open(){

        return mAppModel.open();
    }




    public void close(){

        mAppModel.close();
    }




    private ArrayList<ToDoItem> listToDos(){

        return mAppModel.ToDoList();
    }




    private String getRowTitle(AdapterView.AdapterContextMenuInfo info){

        rowView row = (rowView) info.targetView.getTag();

        String title = row.mTextView.getText().toString();

        title = title.substring(0, title.length() >= 20 ? 20 : title.length());

        return title + "...";
    }




    // Traditionally called by a BroadcastReceiver to reset alarms when a phone is rebooted
    public void resetAlarms(){

        // Goes directly to the database to set the alarms.
        setAlarms(mAppModel.ToDoList());
    }




    private void setAlarms(List<ToDoItem> todoList){

        Calendar today = getThreshold();

        for (ToDoItem todoItem : todoList){

            if (todoItem.getCalendar().after(today)){

                mAlarmManager.setAlarm(todoItem);
            }
        }
    }




    // Returns the date less a certain # of hours.
    private Calendar getThreshold(){

        mToday.setTimeInMillis(System.currentTimeMillis());
        mToday.set(Calendar.SECOND, 0);
        mToday.set(Calendar.MILLISECOND, 0);

        // Goes back a # of hours.
        int minus = -8;

        mToday.add(Calendar.HOUR_OF_DAY, minus);

        return mToday;
    }

    private boolean editAlarm(Intent intent){

        if (!intent.hasExtra("item")){
            return false;
        }

        ToDoItem itemToDo = (ToDoItem) intent.getSerializableExtra("item");

        if (itemToDo == null){
            return false;
        }

        if(mToDoListAdapter == null){
            return false;
        }

        // itemToDo.getListPosn() is not good! Array may be sorted!
        int position = mToDoListAdapter.getListId(itemToDo);

        if (position < 0){
            return false;
        }

        mAppCRUD.edit(position);

        return true;
    }

    // Traditionally called by a Notification to delete an item.
    public boolean delete(long id){

        return mAppCRUD.delete(id);
    }

    // Show 'deleted' records or not.
    boolean showDeleted(){

        return mAppModel.showDeleted();
    }

    // TODO Put this in a framework as a standard method.
    private boolean hideActionBar(){

        ActionBar bar = mController.getActionBar();

        if (bar != null){

            bar.hide();
        }

        return bar != null;
    }

    private boolean runActivity(Class<?> cls){

        if (mIntent == null){

            mIntent = new Intent(mController, cls);

//             mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }else{

            mIntent = mIntent.setClass(mController, cls);
        }

        try{

            mController.startActivity(mIntent);

            return true;

        }catch (Exception ex){

            return false;
        }
    }

    private boolean runFragment(Fragment newFragment, int layoutID){

        try{

            // Check to see it is not already open.
            String fragName = newFragment.getClass().getSimpleName();

            if (!mFragMngr.popBackStackImmediate(fragName, 0)
                    && mFragMngr.findFragmentByTag(fragName) == null){

                FragmentTransaction ft = mFragMngr
                        .beginTransaction()
                        .addToBackStack(fragName)
                        .replace(layoutID, newFragment, fragName);

                ft.commit();
            }

            return true;

        }catch (RuntimeException ex){

            return false;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

        // No Internet connection
        if (!mController.NoConnectivity().isEmpty()){

            return;
        }
    }




    @Override
    // Implements ValueEventListener
    public void onDataChange(DataSnapshot snapshot){

        if (snapshot == null){

            return;
        }
    }

    @Override
    // Implements ValueEventListener
    public void onCancelled(DatabaseError databaseError){

        String error = databaseError.getMessage();
    }




    // Called the moment when an applications preference setting is changed.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){

        if (key.equals("order_of_items")){

            // This method could be called even if this app is destroyed. android:launchMode="singleTask" or  Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (mToDoListAdapter != null){

                mToDoListAdapter.sort();
            }

        }else if (key.equals("clear_notification")){

            mClearNotification = appSettings.getBoolean("clear_notification", false);

        }else if (key.equals("sync_data")){

            mSyncData = appSettings.getBoolean("sync_data", false);

            // Either way it should do a sync.
            syncData();

        }else if (key.equals("show_deleted")){

            if (mAppModel.open()){

                mToDoListAdapter.refresh(listToDos());
            }
        }else if (key.equals("data_online")){

            // Switch the helper here.
            mAppModel.resetDBHelper();

            if (mAppModel.open()){

                mToDoListAdapter.refresh(listToDos());
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference){

        String key = preference.getKey();

        if (key.equals("remove_pastdue")){

            // Still return true; it's been handled even unsuccessfully.
            if (!mAppModel.open()){
                return true;
            }

            mToDoListAdapter.Iterate(new ToDoItemsList.ToDoIterator(){

                public boolean Iterate(ToDoItem itemToDo){

                    if (itemToDo.isPastDue()){

                        mAppModel.delete(itemToDo);
                    }

                    return true;
                }
            });

            mToDoListAdapter.refresh(listToDos());

        }else if (key.equals("remove_deleted")){

            // Still return true; it's been handled even unsuccessfully.
            if (!mAppModel.open()){

                return true;
            }

            mToDoListAdapter.Iterate(new ToDoItemsList.ToDoIterator(){

                public boolean Iterate(ToDoItem itemToDo){

                    if (itemToDo.isDeleted()){

                        mAppModel.trueDelete(itemToDo);
                    }

                    return true;
                }
            });

            mToDoListAdapter.refresh(listToDos());

        }else if (key.equals("export_records")){

            exportData();

            mToDoListAdapter.refresh(listToDos());

        }else if (key.equals("import_records")){

            importData();

            mToDoListAdapter.refresh(listToDos());
        }

        // Very important to return true
        return true;
    }

    public void dialogResult(boolean result){

        // Which dialog window is open?
        if (mMenuItem != null){

            if (result){

                onOptionsItemSelected(mMenuItem);
            }

            // Important to reset now.
            mMenuItem = null;
        }else{

            deleteResult(result);
        }
    }

    void deleteResult(boolean result){

        if (!result){

            return;
        }

        ToDoItem todoItem = (ToDoItem) mToDoListAdapter.getItem(mToDoListID);

        // Allows you to truly delete the record.
        todoItem.isDeleted(false);

        todoItem = null;

        // Important to now indicate a true delete is to be performed.
        mTrulyDelete = true;

        delete(mToDoListID);
    }

    public appController getController(){

        return mController;
    }

    public Activity getActivity(){

        return mController;
    }

    public Context getContext(){

        return mContext;
    }

    public appModel getModel(){

        return mAppModel;
    }

    public String getPackageName(){

        if (PACKAGE_NAME == null){
            PACKAGE_NAME = App.setPackageName(mController);
        }

        return PACKAGE_NAME;
    }

    private boolean deleteSync(){

        // When calling the routine more than once.
        if (mDeleteSync){

            mDeleteSync = false;
        }else{

            mDeleteSync = true;

            syncData("delete");
        }

        return mDeleteSync;
    }

    void listErrors(){

//        Intent errIntent = new Intent(mController, CrashActivity.class);
//
//        errIntent.putExtra("ERROR_LIST", true);
//
//        mController.startActivity(errIntent);

        Fragment fragment = new CrashFragment();

        final Bundle args = new Bundle();

        args.putBoolean("ERROR_LIST", true);

        fragment.setArguments(args);

        runFragment(fragment, android.R.id.content);
    }

    public void syncData(){

        if (mSyncData){

            syncData("get");
        }
    }

    // Sync with the data stored on your Google Drive
    private void syncData(String param){

        dbFireBase.sync();

//        String action;
//
//        if (param == null || param.isEmpty()){
//
//            action = "get";
//        }else{
//
//            action = param;
//        }
//
//        String file = syncFileName();
//
//        if (file.isEmpty()){
//
//            return;
//        }
//
////        Intent googleIntent  = new Intent(mController, googleActivity.class);
//        Intent googleIntent = new Intent(mController, googleService.class);
//
//        googleIntent.putExtra("filename", file);
//
//        googleIntent.putExtra("action", action);
//
////        mController.startActivityForResult(googleIntent, GOOGLE_REQUEST);
//
//        mController.stopService(googleIntent);
//
//        mController.startService(googleIntent);
    }

    public void restoreSync(){

        if (mRestoreSync){

            mRestoreSync = false;

            syncFile();
        }else{

            if (mSyncData){

                if (deleteSync()){

                    mRestoreSync = true;
                }
            }
        }
    }

    public boolean insertTempRec(){

        return mAppModel.insertTempRec();
    }

    public boolean createEmptyTemp(){

        return mAppModel.createEmptyTemp();
    }

    public void importRec(String columnName, String value){

        mAppModel.importRec(columnName, value);
    }

    public ArrayList<ToDoItem> ToDoList(Cursor recs){

        return mAppModel.ToDoList(recs);
    }

    public Cursor getNewTempRecs(){

        return mAppModel.getNewTempRecs();
    }

    public Method getPImethod(){

        Method mth;

        try{

            mth = mController.getPM().getClass()
                    .getMethod(com.gtfp.workingmemory.app.App.getPI(), String.class, int.class);

        }catch (Exception ex){

            mth = null;
        }
        return mth;
    }

    // Return the Package Info..
    public Object getPI(){

        Object obj;

        try{

            obj = getPImethod()
                    .invoke(mController.getPM(), getPackageName(), PackageManager.GET_SIGNATURES);

        }catch (Exception ex){

            obj = null;
        }
        return obj;
    }

    private String versionOfApp(){

        Context context = mController.getApplicationContext();

        PackageManager packageManager = context.getPackageManager();

        String packageName = context.getPackageName();

        String version;

        try{

            version = packageManager.getPackageInfo(packageName, 0).versionName;

        }catch (PackageManager.NameNotFoundException ex){

            version = "not available";
        }

        return version;
    }

    private boolean exportData(){

        File file = exportDataFile();

        return createDataFile(file);
    }

    public String syncFile(){

        File syncFile = getSyncFile();

        String file;

        if (!createDataFile(syncFile)){

            file = "";

        }else{

            file = syncFile.getAbsolutePath();
        }

        return file;
    }

    String syncFileName(){

        return getSyncFile().getAbsolutePath();
    }

    private File getSyncFile(){

        if (mSyncFile == null){

            mSyncFile = new File(appDir(), "syncData.csv");
        }

        return mSyncFile;
    }

    private boolean createDataFile(File file){

        try{

            // You've got to have permission to create a file first.
            if (!appSettings.withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                return false;
            }

            // Can't delete the old one??
            if (file.exists() && !file.delete()){

                showMessage("Can't overwrite old export file!?");
                return false;
            }

            if (!file.createNewFile()){

                showMessage("Could not create export file!?");
                return false;
            }

//            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ',', '"', '\n');
//            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ',', ' ', '\n');

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, "\r\n");

            Cursor curCSV = mAppModel.getRecs();

            csvWrite.writeNext(curCSV.getColumnNames());

            String[] delimit = {","};

            int columnCount = curCSV.getColumnCount();

            String[] arrStr = new String[columnCount];

            while (curCSV.moveToNext()){

                csvWrite.writeNext(delimit);

                for (int i = 0; i < columnCount; i++){

                    arrStr[i] = curCSV.getString(i);
                }

                csvWrite.writeNext(arrStr);
            }

            csvWrite.close();

            curCSV.close();

            return true;

            // Catch IOException
        }catch (Exception ex){

            ErrorHandler.logError(ex);

            return false;
        }
    }

    private boolean importData(){

        if (!open()){

            return false;
        }

        try{

            CSVReader reader = new CSVReader(new FileReader(exportDataFile()));

            String[] columnLine;

            columnLine = reader.readNext();

            if (columnLine == null){
                return false;
            }

            String[] dataLine;

            while ((dataLine = reader.readNext()) != null){

                for (int i = 0; i < dataLine.length; i++){

                    mAppModel.importRec(columnLine[i], dataLine[i]);
                }

                mAppModel.importRec();
            }

        }catch (Exception ex){

            Log.e(mContext.getClass().getSimpleName(), ex.getMessage(), ex);

            return false;
        }
        return true;
    }

    // The application's  private directory.
    private File appDir(){

        return App.getFilesDir(mContext);
    }

    /**
     * Returns true if the application is debuggable.
     *
     * @return true if the application is debuggable.
     */
    private boolean isDebuggable(){

        final PackageManager pm = mContext.getPackageManager();

        try{

            return (pm.getApplicationInfo(mContext.getPackageName(), 0).flags
                    & ApplicationInfo.FLAG_DEBUGGABLE) > 0;

        }catch (PackageManager.NameNotFoundException e){

            return false;
        }
    }

    private File exportDir(){

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists() && !exportDir.mkdirs()){

            exportDir = mContext.getExternalCacheDir();
        }

        return exportDir;
    }

    private File exportDataFile(){

        File exportDir = exportDir();

        // TODO Maybe append today's date on the file name.
        return new File(exportDir, "workingmemory.csv");
    }

    /**
     * Shows a toast message.
     */
    void showMessage(String message){

        Toast.makeText(mController, message, Toast.LENGTH_LONG).show();
    }

    // Called when the activity is re-launched while at the top of the activity stack
    // onResume() is called after this method.
    protected void onNewIntent(Intent intent){

        editAlarm(intent);
    }

    // Called by controller
    protected void onSaveInstanceState(Bundle savedInstanceState){
        // Save the user's current game state

    }

    // Called by controller
    protected void onRestoreInstanceState(Bundle savedInstanceState){

        // Use wasRecreated() here
    }

    // Called by controller
    protected void onPause(){

    }

    // Called by controller
    protected void onStop(){

        // Going to preferences will stop this activity
        mAppModel.onStop();
    }

    // Called by controller
    protected void onStart(){

        mAppModel.onStart();

        if (mToDoListAdapter == null){

            mToDoListAdapter = new ToDoListAdapter(listToDos());

            mTaskList.setAdapter(mToDoListAdapter);
        }
    }

    protected void onRestart(){

        mAppModel.onRestart();
    }

    public void onDestroy(){

        // Destroy the static reference for this App.
        App.onDestroy();

        mController = null;

        mFragMngr = null;

        mContext = null;

        mIntent = null;

        if (mToDoListAdapter != null){

            mToDoListAdapter.onDestroy();

            mToDoListAdapter = null;
        }

        if (mAppSettings != null){

            mAppSettings.onDestroy();

            mAppSettings = null;
        }

        // If this view is called to set alarms there may be no interface.
        if (mAppCRUD != null){

            mAppCRUD.onDestroy();

            mAppCRUD = null;
        }

        if (mAppModel != null){

            mAppModel.onDestroy();

            mAppModel = null;
        }

        if (mAlarmManager != null){

            mAlarmManager.onDestroy();

            mAlarmManager = null;
        }

//        if (mGoogleSignIn != null){
//
//            mGoogleSignIn.onDestroy();
//
//            mGoogleSignIn = null;
//        }

//        if (mItemsOnWallpaper != null)  {
//
//            mItemsOnWallpaper.onDestroy();
//
//            mItemsOnWallpaper = null;
//        }

        mToday = null;

        mSyncFile = null;

        mErrorMenuItem = null;

        mFrameLayout = null;

        mTaskList = null;
    }




    class signInApp implements FirebaseAuth.AuthStateListener{

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

            // No Internet connection
            if (!mController.NoConnectivity().isEmpty()){

                return;
            }

            // Makes this a one time trigger
            firebaseAuth.removeAuthStateListener(this);

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null){

                // Sign in.
                runActivity(SignInActivity.class);
            }else{

                // Sign in.
                runActivity(SignInActivity.class);
            }
        }
    }

}
