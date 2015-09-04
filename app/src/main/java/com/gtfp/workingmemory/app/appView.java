package com.gtfp.workingmemory.app;
/**
 * Created by Drawn on 2015-02-07.
 */

import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.edit.appCRUD;
import com.gtfp.workingmemory.edit.rowView;
import com.gtfp.workingmemory.settings.SettingsActivity;
import com.gtfp.workingmemory.settings.appSettings;
import com.gtfp.workingmemory.todo.ToDoAlarm;
import com.gtfp.workingmemory.todo.ToDoItem;
import com.gtfp.workingmemory.todo.ToDoItemsList;
import com.gtfp.workingmemory.todo.ToDoListAdapter;
import com.gtfp.workingmemory.utils.dialog;
import com.gtfp.workingmemory.wallpaper.Wallpaper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class appView implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener, dialog.DialogBoxListener {

    public appView(Activity activity) {
        this((Context) activity);

        mController = activity;

        mItemsOnWallpaper = new Wallpaper(activity);

        mAppCRUD = new appCRUD(this);

        // TODO Need a better way to read the Preferences.xml.
        mAppSettings = new appSettings(this);

        mClearNotification = appSettings.getBoolean("clear_notification", false);
    }


    // This is called when there is not activity available.
    public appView(Context context) {

        mContext = context;

        mAppModel = new appModel(this);

        // Set up the alarm system.
        mAlarmManager = new ToDoAlarm.Manager(mContext);

        mDialogue = new dialog(mContext);

        mDialogue.setDialogBoxListener(this);

        // Must initialize the Calendar here in this method.
        mToday = Calendar.getInstance();
    }


    // Called by the controller
    protected boolean onCreate(Bundle savedInstanceState) {

        if (!open()) {

            return false;
        }

        mToDoListAdapter = new ToDoListAdapter(listToDos());

        mController.setContentView(R.layout.activity_main);

        Button btnNewToDo = (Button) mController.findViewById(R.id.btnNewToDo);

        // The New button.
        btnNewToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAppCRUD.newToDoItem();
            }
        });

        ListView lvToDos = (ListView) mController.findViewById(R.id.lvToDos);

        // One click will edit that selected item.
        lvToDos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {

                mToDoListID = position;

                mAppCRUD.edit(position);
            }
        });

        lvToDos.setAdapter(mToDoListAdapter);

        // Let's not and see if they're saved by ToDoAlarm.BroadcastReceiver
        // Ensure any existing items have their alarms set.
//            setAlarms(mToDoListAdapter.ToDoList());

        // Context menu
        mController.registerForContextMenu(lvToDos);

//        // Possibly called by an alarm to edit an existing item
        editAlarm(mController.getIntent());

        return true;
    }


    // Called by the controller.
    protected boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = mController.getMenuInflater();

        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }


    // Called by the Controller.
    protected boolean onOptionsItemSelected(MenuItem item) {

        String text;

        switch (item.getItemId()) {
            case R.id.settings:

//                getFragmentManager().beginTransaction().replace(R.id.overlay_fragment, new appSettings()).commit();
                runActivity(SettingsActivity.class);

                return true;

            case R.id.help:

                text = "Help Context coming soon...";

                mDialogue = null;

                mDialogue.showBox(text);

                return true;

            case R.id.about:

                text = "Working Memory\n"
                        + "\nCopyright (c) 2015, G Perry, Winnipeg, MB., Canada"
                        + "\n\nDistributed \"AS IS\","
                        + "\nWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND."
                        + "\n\nVersion " + versionOfApp();

                mDialogue.showBox(text);

                return true;

            default:

                return false;
        }
    }


    // Return the results from another Activity
    // Called by the controller
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // REQUEST_CODE is defined above
        if (resultCode != Activity.RESULT_OK) {

            return;
        }

        if (requestCode == REQUEST_CODE) {

            ToDoItem todoItem = (ToDoItem) data.getSerializableExtra("item");

            mAppCRUD.save(todoItem);
        }
    }

    // Called by the controller
    protected void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == R.id.lvToDos) {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            mToDoListID = info.position;

            menu.clearHeader();

            menu.setHeaderTitle(getRowTitle(info));

            mController.getMenuInflater().inflate(R.menu.context_menu, menu);
        }
    }

    // Called by the controller
    protected boolean onContextItemSelected(MenuItem item) {

        // Select the menu
        switch (item.getItemId()) {
            case R.id.edit:

                mAppCRUD.edit(mToDoListID);

                return true;

            case R.id.delete:

                mAppCRUD.delete(mToDoListID);

                return true;
            default:

                return mController.getParent().onContextItemSelected(item);
        }
    }


    public boolean open() {

        return mAppModel.open();
    }


    public void close() {

        mAppModel.close();
    }


    ArrayList<ToDoItem> listToDos() {

        return mAppModel.ToDoList();
    }


    private String getRowTitle(AdapterView.AdapterContextMenuInfo info) {

        rowView row = (rowView) info.targetView.getTag();

        String title = row.mTextView.getText().toString();

        title = title.substring(0, title.length() >= 20 ? 20 : title.length());

        return title + "...";
    }


    // Traditionally called by a BroadcastReceiver to reset alarms when a phone is rebooted
    public void resetAlarms() {

        // Goes directly to the database to set the alarms.
        setAlarms(mAppModel.ToDoList());
    }


    private void setAlarms(List<ToDoItem> todoList) {

        Calendar today = getThreshold();

        for (ToDoItem todoItem : todoList) {

            if (todoItem.getCalendar().after(today)) {

                mAlarmManager.setAlarm(todoItem);
            }
        }
    }


    // Returns the date less a certain # of hours.
    private Calendar getThreshold() {

        mToday.setTimeInMillis(System.currentTimeMillis());
        mToday.set(Calendar.SECOND, 0);
        mToday.set(Calendar.MILLISECOND, 0);

        // Goes back a # of hours.
        int minus = -8;

        mToday.add(Calendar.HOUR_OF_DAY, minus);

        return mToday;
    }


    private boolean editAlarm(Intent intent) {

        if (!intent.hasExtra("item")) {
            return false;
        }

        ToDoItem itemToDo = (ToDoItem) intent.getSerializableExtra("item");

        if (itemToDo == null) {
            return false;
        }

        // itemToDo.getListPosn() is not good! Array may be sorted!
        int position = mToDoListAdapter.getListId(itemToDo);

        if (position < 0) {
            return false;
        }

        mAppCRUD.edit(position);

        return true;
    }

    // Traditionally called by a Notification to delete an item.
    public boolean delete(long id) {

        return mAppCRUD.delete(id);
    }

    // Show 'deleted' records or not.
    public boolean showDeleted() {

        return mAppModel.showDeleted();
    }


    // TODO Put this in a framework as a standard method.
    private boolean hideActionBar() {

        ActionBar bar = mController.getActionBar();

        if (bar != null) {

            bar.hide();
        }

        return bar != null;
    }


    boolean runActivity(Class<?> cls) {

        if (mIntent == null) {

            mIntent = new Intent(mController, cls);

//             mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        } else {

            mIntent = mIntent.setClass(mController, cls);
        }

        try {

            mController.startActivity(mIntent);

            return true;

        } catch (RuntimeException ex) {

            return false;
        }
    }

    // Called the moment when an applications preference setting is changed.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("order_of_items")) {

            // This method could be called even if this app is destroyed. android:launchMode="singleTask" or  Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (mToDoListAdapter != null) {

                mToDoListAdapter.sort();
            }

        } else if (key.equals("clear_notification")) {

            mClearNotification = appSettings.getBoolean("clear_notification", false);

        } else if (key.equals("show_deleted")) {

            if (mAppModel.open()) {

                mToDoListAdapter.refresh(listToDos());
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        String key = preference.getKey();

        if (key.equals("remove_pastdue")) {

            // Still return true; it's been handled even unsuccessfully.
            if (!mAppModel.open()) {
                return true;
            }

            mToDoListAdapter.Iterate(new ToDoItemsList.ToDoIterator() {

                public boolean Iterate(ToDoItem itemToDo) {

                    if (itemToDo.isPastDue()) {

                        mAppModel.delete(itemToDo);
                    }

                    return true;
                }
            });

            mToDoListAdapter.refresh(listToDos());

        } else if (key.equals("remove_deleted")) {

            // Still return true; it's been handled even unsuccessfully.
            if (!mAppModel.open()) {

                return true;
            }

            mToDoListAdapter.Iterate(new ToDoItemsList.ToDoIterator() {

                public boolean Iterate(ToDoItem itemToDo) {

                    if (itemToDo.isDeleted()) {

                        mAppModel.trueDelete(itemToDo);
                    }

                    return true;
                }
            });

            mToDoListAdapter.refresh(listToDos());

        } else if (key.equals("export_records")) {

            exportData();

            mToDoListAdapter.refresh(listToDos());

        } else if (key.equals("import_records")) {

            importData();

            mToDoListAdapter.refresh(listToDos());
        }

        // Very important to return true
        return true;
    }

    public void dialogResult(boolean result) {

        // Which dialog window is open?
        if (mMenuItem != null) {

            if (result) {

                onOptionsItemSelected(mMenuItem);
            }

            // Important to reset now.
            mMenuItem = null;
        }
    }


    public Activity getActivity() {

        return mController;
    }


    Context getContext() {

        return mContext;
    }

    // Used by external components.
    public static boolean showDialogue() {

        return appSettings.getBoolean("popup_notification", true);
    }


    // Used by external components.
    public static boolean clearNotification() {

        return mClearNotification;
    }


    String versionOfApp() {

        Context context = mController.getApplicationContext();

        PackageManager packageManager = context.getPackageManager();

        String packageName = context.getPackageName();

        String version;

        try {

            version = packageManager.getPackageInfo(packageName, 0).versionName;

        } catch (PackageManager.NameNotFoundException ex) {

            version = "not available";
        }

        return version;
    }


    boolean exportData() {

        File file = exportDataFile();

        try {

            file.createNewFile();

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ',', '"', '\n');

            Cursor curCSV = mAppModel.getRecs();

            csvWrite.writeNext(curCSV.getColumnNames());

            int colunmCount = curCSV.getColumnCount();

            String[] arrStr = new String[colunmCount];

            while (curCSV.moveToNext()) {

                for (int i = 0; i < colunmCount; i++) {

                    arrStr[i] = curCSV.getString(i);
                }

                csvWrite.writeNext(arrStr);
            }

            csvWrite.close();

            curCSV.close();

            return true;

        } catch (Exception ex) {

            Log.e(mContext.getClass().getSimpleName(), ex.getMessage(), ex);

            return false;
        }
    }


    boolean importData() {

        if (!open()) {
            return false;
        }

        try {

            CSVReader reader = new CSVReader(new FileReader(exportDataFile()));

            String[] columnLine;

            columnLine = reader.readNext();

            if (columnLine == null) {
                return false;
            }

            String[] dataLine;

            while ((dataLine = reader.readNext()) != null) {

                for (int i = 0; i < dataLine.length; i++) {

                    mAppModel.importRec(columnLine[i], dataLine[i]);
                }

                mAppModel.importRec();
            }

        } catch (Exception ex) {

            Log.e(mContext.getClass().getSimpleName(), ex.getMessage(), ex);

            return false;
        }
        return true;
    }


    File exportDataFile() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");

        if (!exportDir.exists()) {

            exportDir.mkdirs();
        }

        // TODO Maybe append today's date on the file name.
        return new File(exportDir, "workingmemory.csv");
    }


    // Called when the activity is re-launched while at the top of the activity stack
    // onResume() is called after this method.
    protected void onNewIntent(Intent intent) {

        editAlarm(intent);
    }


    // Called by controller
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

    }

    // Called by controller
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        // Use wasRecreated() here
    }

    // Called by controller
    protected void onStop() {

        // Going to preferences will stop this activity
        mAppModel.onStop();
    }


    protected void onRestart() {

        mAppModel.onRestart();
    }


    public void onDestroy() {

        mController = null;

        mContext = null;

        mIntent = null;

        if (mToDoListAdapter != null) {

            mToDoListAdapter.onDestroy();

            mToDoListAdapter = null;
        }

        if (mAppSettings != null) {

            mAppSettings.onDestroy();

            mAppSettings = null;
        }

        // If this view is called to set alarms there may be no interface.
        if (mAppCRUD != null) {

            mAppCRUD.onDestroy();

            mAppCRUD = null;
        }

        if (mAppModel != null) {

            mAppModel.onDestroy();

            mAppModel = null;
        }

        if (mAlarmManager != null) {

            mAlarmManager.onDestroy();

            mAlarmManager = null;
        }

//        if (mItemsOnWallpaper != null)  {
//
//            mItemsOnWallpaper.onDestroy();
//
//            mItemsOnWallpaper = null;
//        }

        mToday = null;
    }


    private static final String ALARM_ID = "id";

    public Activity mController;

    Context mContext;

    public appModel mAppModel;

    public ToDoListAdapter mToDoListAdapter;

    public ToDoAlarm.Manager mAlarmManager;

    public Wallpaper mItemsOnWallpaper;

    appCRUD mAppCRUD;

    private int mToDoListID = -1;

    private Intent mIntent;

    private final int REQUEST_CODE = 1;

    private static boolean mClearNotification;

    private appSettings mAppSettings;

    private dialog mDialogue;

    private Calendar mToday;

    private MenuItem mMenuItem;
}
