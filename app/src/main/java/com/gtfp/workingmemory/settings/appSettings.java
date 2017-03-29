package com.gtfp.workingmemory.settings;

import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.app.appView;
import com.gtfp.workingmemory.colorPicker.colorPickerView;
import com.gtfp.workingmemory.todo.ToDoAlarm;
import com.gtfp.workingmemory.todo.ToDoItem;
import com.gtfp.workingmemory.utils.Devices;
import com.gtfp.workingmemory.utils.dialog;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

/**
 * Created by Drawn on 2015-03-13.
 */
public class appSettings implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TODOITEM = "item";

    public static final int REQUEST_PERMISSION_CODE = 0x11;

    static private SharedPreferences mSettings;

    static private appView mApp;

    static private Resources mResources;

    // Important to KEEP up to date when access settings in STATIC environment.
    static final private String PACKAGE_NAME = "com.gtfp.workingmemory";

    public appSettings(appView app){

        mApp = app;

        mSettings = get(mApp.getActivity());

        // Run code the moment a preference is changed.
        mSettings.registerOnSharedPreferenceChangeListener(this);
    }


    // Here is the preference listener.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){

// Example to dynamically change the preference text summary.
//        if (key.equals(KEY_PREF_SYNC_CONN)) {
//            Preference connectionPref = findPreference(key);
//            // Set summary to be the user-description for the selected value
//            connectionPref.setSummary(sharedPreferences.getString(key, ""));
//        }

        // Some external activities may call this.
        if(mApp != null){

            mApp.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }


    public static SharedPreferences get(Context context){

        if(mSettings == null){

            mSettings = context
                    .getSharedPreferences(PACKAGE_NAME + "_preferences", Context.MODE_PRIVATE);

            if (mResources == null){

                mResources = context.getResources();
            }
        }
        return mSettings;
    }

    // Hopefully the other get() has already been called.
    public static SharedPreferences get(){

        return mSettings;
    }


    static SharedPreferences.Editor getEditor(){

        return mSettings.edit();
    }


    static Preference findPreference(CharSequence key){

        if (appPreferences.mPreferenceManager == null){
            return null;
        }

        return appPreferences.mPreferenceManager.findPreference(key);
    }


    public static String getString(String key, String defaultValue){

        try{

            return mSettings.getString(key, defaultValue);

        }catch (RuntimeException ex){

            return defaultValue;
        }
    }


    // Value from the resource, Strings.xml
    public static String getResourceString(int key, String defaultValue){

        if (mResources == null){
            return defaultValue;
        }

        String string;

        try{

            string = mResources.getString(key);

        }catch (Resources.NotFoundException ex){

            string = defaultValue;
        }

        return string;
    }


    public static int getInt(String key, int defaultValue){

        try{

            return mSettings.getInt(key, defaultValue);

        }catch (RuntimeException ex){

            return defaultValue;
        }
    }


    static long getLong(String key, long defaultValue){

        try{

            return mSettings.getLong(key, defaultValue);

        }catch (RuntimeException ex){

            return defaultValue;
        }
    }


    static float getFloat(String key, float defaultValue){
        try{

            return mSettings.getFloat(key, defaultValue);

        }catch (RuntimeException ex){

            return defaultValue;
        }
    }


    public static boolean getBoolean(String key, boolean defaultValue){

        try{

            return mSettings.getBoolean(key, defaultValue);

        }catch (RuntimeException ex){

            return defaultValue;
        }
    }

    public static boolean withPermission(@NonNull String permission){

        Activity activity = mApp.getActivity();

        boolean withPermission = ActivityCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;

        if (!withPermission){

            String[] perms = {permission};

            int REQUEST_PERMISSION_CODE = 0x11;

            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, perms, REQUEST_PERMISSION_CODE);
        }

        return withPermission;
    }


    public void onDestroy(){

        mApp = null;

        mSettings = null;
    }


    public static class appPreferences extends PreferenceFragment
            implements Preference.OnPreferenceClickListener
            , Preference.OnPreferenceChangeListener
            , SharedPreferences.OnSharedPreferenceChangeListener
            , dialog.DialogBoxListener{
// Not used right now
//        static SharedPreferences mSettings;

        static PreferenceManager mPreferenceManager;

        static PreferenceScreen mPreferenceScreen;

        static int mPosition;

        Preference mLEDPreference, mSoundPreference;

        ListPreference mMinsPreference;

        Preference mPreferenceClicked;

        private int mREQUEST_CODE;

        private CharSequence mSummary;

        private dialog mDialogue;

        private Intent mIntent;

        private ToDoItem mItemToDo;


        @Override
        public void onCreate(Bundle savedInstanceState){

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            mPreferenceManager = getPreferenceManager();

            hidePreferences();

            mPreferenceScreen = getPreferenceScreen();

            mLEDPreference = findPreference("mLedArgb");

            // Looks like there was a problem in retrieving a specific preference.
            if (mLEDPreference != null){

                // Circumvent the usual process involving an intent
                mLEDPreference.setOnPreferenceClickListener(this);
            }

            mMinsPreference = (ListPreference) findPreference("advance_minutes");

            if (mMinsPreference.getValue() == null){

                mMinsPreference.setValueIndex(mMinsPreference
                        .findIndexOfValue(appSettings.getString("advance_minutes", "20")));
            }

            mSummary = mMinsPreference.getSummary();

            setMinutesSummary();

//            mSoundPreference = mPreferenceManager.findPreference("sound_notification");
//
//            mSoundPreference.setOnPreferenceClickListener(this);

            findPreference("remove_deleted")
                    .setOnPreferenceClickListener(this);

            findPreference("remove_pastdue")
                    .setOnPreferenceClickListener(this);

            findPreference("export_records")
                    .setOnPreferenceClickListener(this);

            findPreference("import_records")
                    .setOnPreferenceClickListener(this);

            // Setup an number of listeners and update the preferences with appropriate default values
            mIntent = ((Activity) mPreferenceScreen.getContext()).getIntent();

            if (mIntent.hasExtra("item")){

                mItemToDo = (ToDoItem) mIntent.getSerializableExtra("item");

                setupToDoItemPreferences("popup_notification",
                        mItemToDo.showDialogue(appSettings.getBoolean("popup_notification", true)));

                setupToDoItemPreferences("vib_notification",
                        mItemToDo.useVibrate(appSettings.getBoolean("vib_notification", false)));

                setupToDoItemPreferences("clear_notification", mItemToDo
                        .clearNotification(appSettings.getBoolean("clear_notification", false)));

                setupToDoItemPreferences("use_LED",
                        mItemToDo.useLED(appSettings.getBoolean("use_LED", true)));

                setupToDoItemPreferences("sound_notification",
                        mItemToDo.Sound(appSettings.getString("sound_notification",
                                ToDoAlarm.Manager.getAlarmUri().toString())));
            }

            // Run code the moment a preference is changed.
            mSettings.registerOnSharedPreferenceChangeListener(this);

            mDialogue = new dialog(getActivity());

            mDialogue.setDialogBoxListener(this);

            int id = appPreferences.class.hashCode();

            mREQUEST_CODE = (id < 0) ? -id : id;
        }



        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);

            getView().setBackgroundColor(Color.BLACK);
            getView().setClickable(true);
        }

//        void initOnPreferenceChangeListeners(){
//
//            Intent intent = ((Activity) mPreferenceScreen.getContext()).getIntent();
//
//            if (!intent.hasExtra("item")) return;
//
//            ToDoItem itemToDo = (ToDoItem) intent.getSerializableExtra("item");
//
////            mLEDPreference.setOnPreferenceChangeListener(this);
//
////            mLEDPreference.setDefaultValue(appSettings.getInt("mLedArgb", 0xFF0000FF));
//
////            mLEDPreference.setPersistent(false);
//
//            setupToDoItemPreferences("popup_notification", itemToDo.showDialogue(appSettings.getBoolean("popup_notification", true)));
//
//            setupToDoItemPreferences("clear_notification", itemToDo.clearNotification(appSettings.getBoolean("clear_notification", false)));
//
//            setupToDoItemPreferences("use_LED", itemToDo.useLED(appSettings.getBoolean("use_LED", true)));
//
////            PreferenceManager.setDefaultValues(mPreferenceScreen.getContext(), R.xml.preferences, true);
//        }


        void setupToDoItemPreferences(CharSequence key, Object itemValue){

            Preference pref = findPreference(key);

            if (pref == null){

                return;
            }

            // OnPreferenceChange()
            pref.setOnPreferenceChangeListener(this);

            pref.setPersistent(false);

            if (pref instanceof CheckBoxPreference){

                ((CheckBoxPreference) pref).onSetInitialValue(false, itemValue);

            }else if (pref instanceof RingtonePreference){

                ((RingtonePreference) pref).onSetInitialValue(false, itemValue.toString());
            }
        }

        // Determine if certain preferences should be hidden or not
        void hidePreferences(){

            PreferenceScreen NotificationPreference = (PreferenceScreen) findPreference(
                    "notification_preferences");

            String msg = Devices.getDeviceName();

            if (msg.contains("Samsung Galaxy Mini")){

                msg = msg + ":\r\n" + getResourceString(R.string.no_LED,
                        "Your phone does not use LED Notification.\r\nHence, this option is disabled.");

                Preference useLED = NotificationPreference.findPreference("use_LED");

                useLED.setSummary(msg);

                useLED.setEnabled(false);

                NotificationPreference
                        .removePreference(NotificationPreference.findPreference("mLedArgb"));
            }

        }

        // Here is the preference listener.
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){

            if (key.equals("advance_minutes")){

                setMinutesSummary();
            }
        }

        // Set the preference summary
        void setMinutesSummary(){

            if (mMinsPreference.getValue() != null && !mMinsPreference.getValue().isEmpty()){

                mMinsPreference
                        .setSummary(mSummary + " (" + mMinsPreference.getValue() + " minutes)");
            }
        }


        @Override
        public boolean onPreferenceClick(Preference preference){

            String key = preference.getKey();

            if (key.equals("mLedArgb")){

                SharedPreferences saveSettings = preference.getSharedPreferences();

                Intent intent = preference.getIntent();

                if (mItemToDo == null){

                    colorPickerView
                            .setColor(saveSettings.getInt("mLedArgb", 0xFF0000FF));

                    intent.putExtra("LedOnMs", saveSettings.getInt("LedOnMs", 100));

                    intent.putExtra("LedOffMs", saveSettings.getInt("LedOffMs", 2000));
                }else{

                    // Merely used as a flag.
                    intent.putExtra("item", mItemToDo);

                    int color = mItemToDo.getLEDColor();

                    color = color == 0 ? saveSettings.getInt("mLedArgb", 0xFF0000FF) : color;

                    colorPickerView
                            .setColor(color);

                    color = mItemToDo.getLEDOn();

                    color = color == 0 ? saveSettings.getInt("LedOnMs", 100) : color;

                    intent.putExtra("LedOnMs", color);

                    color = mItemToDo.getLEDOff();

                    color = color == 0 ? saveSettings.getInt("LedOffMs", 100) : color;

                    intent.putExtra("LedOffMs", color);
                }

                // Implement the onActivityResult() to save the selected colour.
                startActivityForResult(intent, mREQUEST_CODE);

                return true;

            }else if (key.equals("sound_notification")){

//                Intent ringIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//
//                ringIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
//                        RingtoneManager.TYPE_NOTIFICATION);
//
//                ringIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
//
//                ringIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
//                        ToDoAlarm.Manager.getAlarmUri());
//
//                startActivityForResult(ringIntent, mREQUEST_CODE + 5);

                // Important to get out now else the app below will have a turn too.
                return true;

            }else if (key.equals("remove_deleted")){

                // If not null, then go to the mApp.onPreferenceClick()
                if (mPreferenceClicked == null){

                    mPreferenceClicked = preference;

                    mDialogue.show("Permanently delete all already deleted records?");

                    return true;
                }
            }else if (key.equals("remove_pastdue")){

                // If not null, then go to the mApp.onPreferenceClick()
                if (mPreferenceClicked == null){

                    mPreferenceClicked = preference;

                    mDialogue.show("Delete all past due records?");

                    return true;
                }
            }else if (key.equals("export_records")){

                // If not null, then go to the mApp.onPreferenceClick()
                if (mPreferenceClicked == null){

                    mPreferenceClicked = preference;

                    mDialogue.show("Export records to a file?");

                    return true;
                }
            }else if (key.equals("import_records")){

                // If not null, then go to the mApp.onPreferenceClick()
                if (mPreferenceClicked == null){

                    mPreferenceClicked = preference;

                    mDialogue.show("Import records?");

                    return true;
                }
            }

            return mApp.onPreferenceClick(preference);
        }

        // Set by setOnPreferenceChangeListener
        public boolean onPreferenceChange(Preference preference, Object newValue){

            String key = preference.getKey();

            if (!key.equals("mLedArgb")){

                Intent intent = ((Activity) preference.getContext()).getIntent();

                if (intent.hasExtra(TODOITEM)){

                    return setItemPreferences(intent, preference, newValue);
                }
            }

            return true;
        }


        boolean setItemPreferences(Intent intent, Preference preference, Object newValue){

            ToDoItem itemToDo = (ToDoItem) intent.getSerializableExtra(TODOITEM);

            // You actually don't want to save it to the SharedPreference.
            preference.setPersistent(false);

            String key = preference.getKey();

            boolean continueClick = true;

            itemToDo.settingsChanged(true);

            if (key.equals("popup_notification")){

                itemToDo.setShowDialogue((Boolean) newValue);

            }else if (key.equals("vib_notification")){

                itemToDo.setUseVibrate((Boolean) newValue);

            }else if (key.equals("clear_notification")){

                itemToDo.setClearNotification((Boolean) newValue);

            }else if (key.equals("use_LED")){

                itemToDo.setUseLED((Boolean) newValue);

            }else if (key.equals("sound_notification")){

                itemToDo.setSound((String) newValue);

                // Setting the sound doesn't need to continue and checkoff the newValue.
                continueClick = false;
            }else{

                itemToDo.settingsChanged(false);
            }

            intent.putExtra(TODOITEM, itemToDo);

            return continueClick;
        }


        // Return the results from another Activity
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            // REQUEST_CODE is defined above
            if (resultCode != Activity.RESULT_OK){

            }else if (requestCode == mREQUEST_CODE){

                LEDSettingsResult(data);

//            } else if (requestCode == mREQUEST_CODE + 5) {
//
//                SoundSettingsResult(data);
            }
        }


        private void LEDSettingsResult(Intent data){

            boolean saveToItem = data.hasExtra(TODOITEM);

            ToDoItem itemToDo = null;

            Intent intent = null;

            if (saveToItem){

                // Get the Settings screen's intent. It's the itemToDo
                intent = ((Activity) mPreferenceScreen.getContext()).getIntent();

                itemToDo = (ToDoItem) intent.getSerializableExtra("item");

            }else if (!mLEDPreference.shouldCommit()){

                return;
            }

            SharedPreferences.Editor editor = mLEDPreference.getEditor();

            int colour = data.getIntExtra("colour", 0);

            if (colour != 0){

                if (saveToItem){

                    itemToDo.setLEDColor(colour);
                }else{

                    editor.putInt(mLEDPreference.getKey(), colour);

                    editor.apply();
                }
            }

            int LEDSetting = data.getIntExtra("LedOnMs", 0);

            if (LEDSetting != 0){

                if (saveToItem){

                    itemToDo.setLEDOn(LEDSetting);
                }else{

                    editor.putInt("LedOnMs", LEDSetting);

                    editor.apply();
                }
            }

            LEDSetting = data.getIntExtra("LedOffMs", 0);

            if (LEDSetting != 0){

                if (saveToItem){

                    itemToDo.setLEDOff(LEDSetting);
                }else{

                    editor.putInt("LedOffMs", LEDSetting);

                    editor.apply();
                }
            }

            if (intent != null){

                intent.putExtra(TODOITEM, itemToDo);

//                ((Activity) mPreferenceScreen.getContext()).setIntent(intent);
            }
        }

//        void SoundSettingsResult(Intent data){
//
//            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
//
//            if (uri == null) return;
//
//            ToDoItem itemToDo = getItemToDo(data);
//
//            if (itemToDo != null) {
//
//                itemToDo.setSound(uri);
//
//                return;
//            }
//
//            if (!mSoundPreference.shouldCommit()) {
//
//               return;
//            }
//
//            SharedPreferences.Editor editor = mSoundPreference.getEditor();
//
//            editor.putString(mSoundPreference.getKey(), uri.toString());
//
//            editor.apply();
//        }


        private ToDoItem getItemToDo(Intent data){

            if (!data.hasExtra(TODOITEM)){
                return null;
            }

            // Access the Settings Activity's intent object.
            Intent intent = ((Activity) mPreferenceScreen.getContext()).getIntent();

            return (ToDoItem) intent.getSerializableExtra("item");
        }


        public void dialogResult(boolean result){

            if (result){

                onPreferenceClick(mPreferenceClicked);
            }

            // Important to null this now.
            mPreferenceClicked = null;
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode != REQUEST_PERMISSION_CODE){

                return;
            }

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // save file

            }else{

//                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState){

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference){
            super.onPreferenceTreeClick(preferenceScreen, preference);

            // If the user has clicked on a preference screen, set up the action bar
            if (preference instanceof PreferenceScreen){

                initializeActionBar(preferenceScreen, (PreferenceScreen) preference);
            }

            // Important to return false to continue the preferences.
            return false;
        }


        /** Sets up the action bar for an {@link PreferenceScreen} */
        public static void initializeActionBar(PreferenceScreen parentScreen,
                PreferenceScreen preferenceScreen){

            final Dialog dialog = preferenceScreen.getDialog();

            if (dialog == null){

                return;
            }

            // Inialize the action bar
            ActionBar bar = dialog.getActionBar();

            bar.setDisplayHomeAsUpEnabled(true);

            // Apply custom home button area click listener to close the PreferenceScreen because PreferenceScreens are dialogs which swallow
            // events instead of passing to the activity
            // Related Issue: https://code.google.com/p/android/issues/detail?id=4611
            View homeBtn = dialog.findViewById(android.R.id.home);

            if (homeBtn != null){

                View.OnClickListener dismissDialogClickListener = new View.OnClickListener(){

                    @Override
                    public void onClick(View v){

                        dialog.cancel();
                    }
                };

                // Prepare yourselves for some hacky programming
                ViewParent homeBtnContainer = homeBtn.getParent();

                // The home button is an ImageView inside a FrameLayout
                if (homeBtnContainer instanceof FrameLayout){

                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout){

                        View view = (View) containerParent.getParent();

//                        if (view bar.getCustomView() ){

//                        }else {

                        // This view also contains the title text, set the whole view as clickable
                        ((LinearLayout) containerParent)
                                .setOnClickListener(dismissDialogClickListener);
//                        }
                    }else{

                        // Just set it on the home button
                        ((FrameLayout) homeBtnContainer)
                                .setOnClickListener(dismissDialogClickListener);
                    }
                }else{

                    // The 'If all else fails' default case
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
        }


        public static Preference openPreference(String prefKey){

//            Preference pref = findPreference(mPreferenceScreen, prefKey);
//
//            if (pref != null) {
//
//                mPreferenceScreen.onItemClick(null, null, mPosition, 0);
//            }

//            if(mPreferenceScreen == null){
//
//                mPreferenceScreen = getPreferenceScreen();
//            }

            final ListAdapter listAdapter = mPreferenceScreen.getRootAdapter();

            String key;

            Preference pref = null;

            // Note, this just goes through the top level of preferences
            for (int cnt = 0; cnt < listAdapter.getCount(); ++cnt){

                Object item = listAdapter.getItem(cnt);

                if (!(item instanceof Preference)){

                    continue;
                }

                pref = ((Preference) item);

                key = pref.getKey();

                if (key != null && key.equals(prefKey)){

                    // Preference has a *hidden* performClick()
                    mPreferenceScreen.onItemClick(null, null, cnt, 0);
                    break;
                }
            }

            return pref;
        }

        static Preference findPreference(PreferenceGroup group, String key){

            mPosition = -1;

            return PreferenceFind(group, key);
        }


        private static Preference PreferenceFind(PreferenceGroup group, String key){

            final int preferenceCount = group.getPreferenceCount();

            for (int i = 0; i < preferenceCount; i++){

                final Preference preference = group.getPreference(i);

                final String curKey = preference.getKey();

                if (curKey != null && curKey.equals(key)){

                    mPosition++;

                    return preference;
                }

                if (preference instanceof PreferenceGroup){

                    mPosition++;

                    Preference returnedPreference = PreferenceFind(
                            (PreferenceGroup) preference, key);

                    if (returnedPreference != null){

                        return returnedPreference;
                    }
                }
            }

            return null;
        }

//        @Override
//        public boolean performOptionsItemSelected(MenuItem item) {
//
//            switch (item.getItemId()) {
//                case android.R.id.home:
//
//                    Intent upIntent = new Intent(this, MainActivity.class);
//
//                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
//
//                        NavUtils.navigateUpTo(this, upIntent);
//
//                        finish();
//                    } else {
//
//                        finish();
//                    }
//
//                    return true;
//
//                default: return super.onOptionsItemSelected(item);
//            }
//        }


        @Override
        public void onPause(){

            super.onPause();
        }


        @Override
        public void onDestroy(){

            super.onDestroy();

            mDialogue.onDestroy();

            mDialogue = null;

            mPreferenceClicked = null;

            mPreferenceManager = null;

            mPreferenceScreen = null;

            mLEDPreference = null;

            mMinsPreference = null;

            mSummary = null;

            mIntent = null;

            mItemToDo = null;
        }
    }

}