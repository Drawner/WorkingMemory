package com.gtfp.workingmemory.edit;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.gtfp.workingmemory.BuildConfig;
import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.colorPicker.colorPickerActivity;
import com.gtfp.workingmemory.colorPicker.colorPickerView;
import com.gtfp.workingmemory.settings.SettingsActivity;
import com.gtfp.workingmemory.settings.appSettings;
import com.gtfp.workingmemory.todo.ToDoAlarm;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


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
public class editToDoItem extends android.support.v4.app.FragmentActivity
        implements TimePicker.OnTimeChangedListener, DatePicker.OnDateChangedListener,
        colorPickerView.OnColorChangedListener, RadialTimePickerDialogFragment.OnTimeSetListener {

    public static final String FRAG_TAG_TIME_PICKER = "fragTimePicker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_edit_item);

        mToday = Calendar.getInstance();

        mIntent = getIntent();

        mToDoItem = (ToDoItem) mIntent.getSerializableExtra(TODOITEM);

        mEditItem = (EditedText) findViewById(R.id.txtToDoItem);

        mEditItem.setText(mToDoItem.getItemName());

        mDateTime = mToDoItem.getCalendar();

        editReminder = (RadioGroupTableLayout) findViewById(
                R.id.MinsRadioGroup);

        setupDateTimePicker();

        // Important to get the date after the setupDateTimePicker
        mOldDate = (Calendar) mDateTime.clone();

        mPastDue = mCanSnooze;

        editDueDate = (TextView) findViewById(R.id.txtToDoTime);

        editDueDate.setText(mToDoItem.displayDueDate());

        editDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog.show();
            }
        });

        mImageButton = (ImageButton) findViewById(R.id.imgSettings);

        if (!appSettings.getBoolean("use_LED", false)) {

            mImageButton.setVisibility(View.INVISIBLE);
        } else {

            int LEDColor = mToDoItem.getLEDColor();

            if (LEDColor != 0) {

                mImageButton.setBackgroundColor(LEDColor);
            }
        }

        // No need for reminders. Time has passed.
        if (mCanSnooze) {

            testPastDue(mDateTime, true);

            // Disable some buttons if past due
//            editReminder.enableButtons();
        } else {

            // Disable some buttons if past due
            editReminder.enableButtons();

            if (editReminder.check(mToDoItem.getReminderChk())) {

                Toast.makeText(this,
                        "Reminder:  " + ToDoItem.displayTime(mToDoItem.getReminderEpoch()),
                        Toast.LENGTH_SHORT).show();
//            } else {

//                // This is a 'new' item.
//                if (mToDoItem.getItemName().isEmpty()) {
//
//                    String advMins = appSettings.getString("advance_minutes", "20") + "m";
//
//                    ArrayList<RadioButton> radioButtons = editReminder.getRadioButtons();
//
//                    for (RadioButton radio : radioButtons) {
//
//                          if (advMins.equals(radio.getText())){
//
//                            editReminder.check(radio.getId());
//
//                            break;
//                        }
//                    }
//                }
            }
        }

//        int id = editToDoItem.class.hashCode();

//        mREQUEST_CODE = (id < 0) ? -id : id;

        // Valid range:  0 to 65535
        mREQUEST_CODE = 60000;
    }


    public void setupDateTimePicker() {

        mDialog = new Dialog(this);

        mDialog.setContentView(R.layout.date_time_picker);

        mDP = (DatePicker) mDialog.findViewById(R.id.datePicker);

        mTP = (TimePicker) mDialog.findViewById(R.id.timePicker);

        // This is a 'new' item.
        if (mToDoItem.newItem(mToDoItem.getItemName().isEmpty())) {

            // Advance the time by a number of minutes.
            mDateTime.add(Calendar.MINUTE,
                    Integer.valueOf(appSettings.getString("advance_minutes", "20")));

            mToDoItem.setDueDate(mDateTime);

            mToDoItem.setReminderEpoch(mDateTime);

        } else {

            // Test if this item has already passed.
            mCanSnooze = testPastDue(mDateTime, false);
        }

        if (BuildConfig.DEBUG) {

            String reminder = ToDoItem.EpochToDateTime(mToDoItem.getReminderEpoch());

            String duedate = ToDoItem.EpochToDateTime(mToDoItem.getDueDateInEpoch());

            String dateAgain = getDateString(mDateTime);

            // So to place a breakpoint.
            duedate = null;
        }

        // If an old item pastdue, advance the time to the present.
        if (mCanSnooze) {

            mToday.setTimeInMillis(System.currentTimeMillis());
            mToday.set(Calendar.SECOND, 0);
            mToday.set(Calendar.MILLISECOND, 0);

            // Advance the time by a number of minutes.
            mToday.add(Calendar.MINUTE,
                    Integer.valueOf(appSettings.getString("advance_minutes", "20")));

            mDP.init(mToday.get(Calendar.YEAR), mToday.get(Calendar.MONTH), mToday.get(Calendar.DATE),
                    this);

            mOldHour = mToday.get(Calendar.HOUR_OF_DAY);

            mTP.setCurrentHour(mOldHour);

            mOldMin = mToday.get(Calendar.MINUTE);

            mTP.setCurrentMinute(mOldMin);
        } else {

            mDP.init(mToDoItem.getYear(), mToDoItem.getMonth(), mToDoItem.getDay(), this);

            mOldHour = mToDoItem.getHour();

            mTP.setCurrentHour(mOldHour);

            mOldMin = mToDoItem.getMin();

            mTP.setCurrentMinute(mOldMin);
        }

        // Now determine if the item had already been triggered.
        if (!mCanSnooze){

            // Can snooze as this item has already went 'past due' before.
            mCanSnooze = mToDoItem.hasFired();
        }else {

            // Record the item has finally past due.
            mToDoItem.hasFired(mCanSnooze);
        }

        try {

            // This will update the Dialog's title
            // mDialog, mOldHour, mOldMin must be initialized before this is called.
            // NotifyChange() is called and, in turn, onDateChanged() is called.
            mDP.setMinDate(ToDoItem.TodayInEpoch());

        } catch (IllegalArgumentException ex) {

            setDialogTitle();
        }

        if (BuildConfig.DEBUG) {

            String reminder = ToDoItem.EpochToDateTime(mToDoItem.getReminderEpoch());

            String duedate = ToDoItem.EpochToDateTime(mToDoItem.getDueDateInEpoch());

            String dateAgain = getDateString(mDateTime);

            // So to place a breakpoint.
            duedate = null;
        }

        mTP.setOnTimeChangedListener(this);

 // Can't use it. May be incompatible.
        ImageButton clockDial = (ImageButton) mDialog.findViewById(R.id.btnClockDial);

        clockDial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
                        .setOnTimeSetListener(editToDoItem.this)
                        .setStartTime(mTP.getCurrentHour(), mTP.getCurrentMinute())
                        .setThemeDark();
//                    .setThemeCustom(2130772013);

                rtpd.show(getSupportFragmentManager(), FRAG_TAG_TIME_PICKER);
            }
        });

        Button saveDate = (Button) mDialog.findViewById(R.id.btnSave);

        // When you click to save your date and time in the DateTimePicker
        saveDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mDateTime.set(mDP.getYear(), mDP.getMonth(), mDP.getDayOfMonth(),
                        mTP.getCurrentHour(), mTP.getCurrentMinute());

                editDueDate.setText(ToDoItem.EpochToDateTime(mDateTime.getTimeInMillis()));

                mPastDue = testPastDue(mDateTime, true);

                boolean dateChanged = !mOldDate.equals(mDateTime);

                if (dateChanged) {

                    // Clear the radio button
                    editReminder
                            .setCheckedStateForView(editReminder.getCheckedRadioButtonId(), false);
                }

                mCanSnooze = mPastDue;

                if (!mCanSnooze) {

                    // In this case, we will 'reset' the item as if new.
                    mToDoItem.hasFired(false);

                    // Disable some buttons if past due
                    editReminder.enableButtons();
                }

                if (!mPastDue) {

                    mDialog.cancel();
                }
            }
        });
    }


    // Set the title of the dialogue window.
    private void setDialogTitle() {

        mDialog.setTitle(ToDoItem.formatW_M_D_12H(
                ToDoItem.toEpoch(mDP.getYear(), mDP.getMonth(), mDP.getDayOfMonth(), mOldHour,
                        mOldMin)));
    }

    public void saveToDoItem(View SaveBtn) {

        boolean changeMade = false;

        Calendar reminder = null;

        // The item has been changed.
        boolean textChanged = mEditItem.isChanged();

        if (textChanged) {

            String todoItem = mEditItem.getText().toString().trim();

            if (todoItem.isEmpty()) {

                Toast.makeText(this, "Empty Task Item.", Toast.LENGTH_SHORT).show();

                return;
            }

            mToDoItem.setItemName(todoItem);

            changeMade = true;
        }

        boolean pastDue = testPastDue(mDateTime, true);

        int radioChkBtn = editReminder.getCheckedRadioButtonId();

        boolean dateChanged = isDateChanged();

        boolean radioChanged = radioChkBtn > 0 && (dateChanged || radioChkBtn != mToDoItem
                .getReminderChk());

        if (mPastDue && pastDue && !radioChanged && !textChanged) {

            return;
        }

        // The date has been changed.
        if (dateChanged) {

            if (BuildConfig.DEBUG) {

                String duedate = getDateString(mDateTime);

                // So to place a breakpoint.
                duedate = null;
            }

            mToDoItem.setDueDate(mDateTime);

            reminder = (Calendar) mDateTime.clone();

            changeMade = true;
        }

        // If past due or a radio button changed.
        if ( radioChkBtn > 0 && (!mCanSnooze || radioChanged)) {

                RadioButton checkedBtn = (RadioButton) findViewById(radioChkBtn);

                String radio = checkedBtn.getText().toString();

                int step = -1 * Integer.parseInt(radio.replaceAll("[^\\d.]", ""));

                if (step == 0){ // && mToDoItem.newItem()) {

                    // We're not worried about past due.
                    reminder = null;

                    // We're making a reminder item that doesn't want an alarm.
                    mToDoItem.setAlarm(false);
                } else {

                    // If only the radio button is changed, a clone must be done.
                    if (!dateChanged) {

                        reminder = (Calendar) mDateTime.clone();
                        reminder.add(Calendar.MINUTE, step);
                    }
                }

                mToDoItem.setReminderChk(radioChkBtn);

                changeMade = true;
        }

        if (!changeMade) {

            changeMade = mToDoItem.LEDColorChanged();
        }
        if (!changeMade) {

            changeMade = mToDoItem.LEDOnChanged();
        }
        if (!changeMade) {

            changeMade = mToDoItem.LEDOffChanged();
        }
        if (!changeMade){

            changeMade = mToDoItem.settingsChanged();
        }


        if (reminder != null) {

            String remindstr = "";

            if (BuildConfig.DEBUG) {

               remindstr = getDateString(reminder);

                remindstr = "";
            }

            mToDoItem.setReminderEpoch(reminder);

            if (testPastDue(reminder, true)) {

                return;
            }
        }

        if (changeMade) {

            mIntent.putExtra("item", mToDoItem);

            // Activity finished ok, return the data
            // set result code and bundle data for response
            setResult(RESULT_OK, mIntent);
        } else {

            setResult(RESULT_CANCELED);
        }

        // closes the activity, pass data to
        finish();
    }


    public boolean testPastDue(Calendar cal, boolean toast) {

        mToday.setTimeInMillis(System.currentTimeMillis());
        mToday.set(Calendar.SECOND, 0);
        mToday.set(Calendar.MILLISECOND, 0);

        if (BuildConfig.DEBUG) {

            String theData = getDateString(cal);

            String now = getDateString(mToday);

            now = null;
        }

        boolean pastDue = cal.before(mToday);
        // getTimeInMillis() < dateTime;

        if (pastDue) {

            if (toast) {

                Toast.makeText(this, "This date has already passed.", Toast.LENGTH_SHORT).show();
            }

        } else if (ToDoItem.isSameDay(cal, mToday)) {

            pastDue = true;

            if (toast) {

                Toast.makeText(this, "That's right now! Re-enter.", Toast.LENGTH_SHORT).show();
            }
        }

        return pastDue;
    }


    // Used for debugging. Supply a readable date.
    private String getDateString(Calendar cal) {

        return ToDoItem.EpochToDateTime(cal.getTimeInMillis());
    }


    private boolean isDateChanged() {

        return mOldDate != null && !mOldDate.equals(mDateTime);
    }


    public void onTimeChanged(TimePicker timePicker, int hour, int minute) {

        if (mIgnoreCall) {

            mIgnoreCall = false;
            return;
        }

        int maxMin = 59;

        int minMin = 0;

        // Moving forward
        if (mOldMin == maxMin && minute == minMin) {

            if (mCanSnooze && hour == 0) {

                mCrossMidNight++;

                advanceDate(1);
            } else {

                // Setting the hour will call this method again.
                mIgnoreCall = true;

                timePicker.setCurrentHour(hour - 1);
            }

            // Falling back
        } else if (mOldMin == minMin && minute == maxMin) {

            if (mCanSnooze && hour == 23) {

                if (mCrossMidNight > 0) {

                    advanceDate(-1);

                    mCrossMidNight--;
                }
            } else {

                // Setting the hour will call this method again.
                mIgnoreCall = true;

                timePicker.setCurrentHour(hour + 1);
            }

            // Forward to Midnight
        } else if (mOldHour == 23 && hour == 0 && !mDateChanged) {

            mCrossMidNight++;

            advanceDate(1);

            // Back from Midnight
        } else if (mOldHour == 0 && hour == 23 && !mDateChanged) {

            if (mCrossMidNight > 0) {

                advanceDate(-1);

                mCrossMidNight--;
            }
        }

        mOldMin = minute;

        mOldHour = hour;

        setDialogTitle();
    }


   public void  onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute){

       mTP.setCurrentHour(hourOfDay);

       mTP.setCurrentMinute(minute);

       onTimeChanged(mTP, hourOfDay, minute);
    }


    private void advanceDate(int step) {

        if (step == 0) {

            return;
        }

        mDateTime.set(mDP.getYear(), mDP.getMonth(), mDP.getDayOfMonth());

        mDateTime.add(Calendar.DAY_OF_MONTH, step);

        mDP.updateDate(mDateTime.get(Calendar.YEAR), mDateTime.get(Calendar.MONTH),
                mDateTime.get(Calendar.DAY_OF_MONTH));
    }


    public void settings(View view){

        if (ToDoAlarm.Manager.copyOver(mToDoItem)) {

            mIntent.putExtra(TODOITEM, mToDoItem);
        }

        mIntent.putExtra("Preference", "notification_preferences");

        runActivity(SettingsActivity.class);

//        appSettings.appPreferences.openPreference("notification_preferences");
    }


    public void colorWheel(View colorWheelBtn) {

        colorPickerView.init(this, getLEDColor());

        int LEDOn = mToDoItem.getLEDOn();

        Intent intent;

        if (LEDOn == 0) {

            intent = colorPickerActivity.getLEDRates();
        } else {

            intent = new Intent();

            intent.putExtra("LedOnMs", LEDOn);

            intent.putExtra("LedOffMs", mToDoItem.getLEDOff());
        }

        intent.setClass(this, colorPickerActivity.class);

        this.startActivityForResult(intent, mREQUEST_CODE);
    }


    private int getLEDColor() {

        int color = mToDoItem.getLEDColor();

        if (color != 0) {

            return color;
        }

        SharedPreferences settings = appSettings.get(this);

        color = settings.getInt("mLedArgb", 0xFF0000FF);

        mToDoItem.setLEDColor(color);

        return color;
    }

    // Assigned ColorPicker selection.
    public void colorChanged(int color) {

        mToDoItem.setLEDColor(color);

        mImageButton.setBackgroundColor(color);
    }


    private boolean runActivity(Class<?> cls) {

        if (mIntent == null) {

            mIntent = new Intent(this, cls);

//             mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        } else {

            mIntent.setClass(this, cls);
        }

        try {

            this.startActivityForResult(mIntent, mREQUEST_CODE);

            return true;

        } catch (RuntimeException ex) {

            return false;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // REQUEST_CODE is defined above
        if (resultCode != Activity.RESULT_OK) {

            return;
        }

        if (requestCode != mREQUEST_CODE) {

            return;
        }

//        mToDoItem.setLEDOn(data.getIntExtra("LedOnMs", 0));
//
//        mToDoItem.setLEDOff(data.getIntExtra("LedOffMs", 0));

        if (data.hasExtra(TODOITEM)) {

            ToDoItem itemToDo = (ToDoItem) data.getSerializableExtra(TODOITEM);

            mToDoItem.setShowDialogue(itemToDo.showDialogue(true));

            mToDoItem.setUseVibrate(itemToDo.useVibrate(false));

            mToDoItem.setClearNotification(itemToDo.clearNotification(true));

            mToDoItem.setUseLED(itemToDo.useLED(true));

            mToDoItem.setLEDColor(itemToDo.getLEDColor());

            mToDoItem.setLEDOn(itemToDo.getLEDOn());

            mToDoItem.setLEDOff(itemToDo.getLEDOff());

            mToDoItem.setSound(itemToDo.Sound(ToDoAlarm.Manager.getAlarmUri()));
        }
    }


    public void onDateChanged(DatePicker datePicker, int year, int month, int day) {

        mDialog.setTitle(ToDoItem.formatW_M_D_12H(
                ToDoItem.toEpoch(year, month, day, mOldHour, mOldMin)));

        // Allow the date to be changed by the TimePicker.
        if (mCrossMidNight > 0) {

            return;
        }

        mDateTime.set(year, month, day);

        if (mOldDate != null) {

            // Changed if not the original date when datepicker was opened.
            mDateChanged = !mOldDate.equals(mDateTime);
        }
    }


    public void showSoftKeyboard(View view) {

        if (view.requestFocus()) {

            if (mInputMnger == null) {

                mInputMnger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            }

            mInputMnger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

// Placed in the frmwrkActivity instead! See how that works. :)
//    @Override
//    public View onCreateView(String name, Context context, AttributeSet attrs) {
//
//        View view;
//
//        if (mInflator == null){
//
//            mInflator = LayoutInflater.from(context);
//
//            mPrefix = ((Activity) context).getComponentName().getClassName();
//
//            mPrefix = mPrefix.substring(0, mPrefix.lastIndexOf(".")+1);
//        }
//
//        try{
//
//            view = mInflator.createView(name, mPrefix, attrs);
//
//        } catch (ClassNotFoundException e) {
//
//            view = null;
//
//        } catch (InflateException e) {
//
//            view = null;
//        }
//
//        return view;
//    }


    @Override
    protected void onResume() {
        super.onResume();

        showSoftKeyboard(mEditItem);

        // Set focus to the text field.
//        mEditItem.setFocusableInTouchMode(true);

//        final InputMethodManager inputMethodManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        inputMethodManager.showSoftInput(mEditItem, InputMethodManager.SHOW_IMPLICIT);

//        mEditItem.requestFocus();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIntent = null;

        mToDoItem = null;

        mDP = null;

        mTP = null;

        mDateTime = null;

        mToday = null;

//        mInflator = null;
    }

    private int mREQUEST_CODE;

    private static final String TODOITEM = "item";

    private Intent mIntent;

    private EditedText mEditItem;

    private ToDoItem mToDoItem;

    public TextView editDueDate;

    private RadioGroupTableLayout editReminder;

    private Calendar mToday;

    public Calendar mOldDate;

    public Calendar mDateTime;

    private DatePicker mDP;

    public TimePicker mTP;

    private Dialog mDialog;

    private boolean mIgnoreCall = false;

    private boolean mDateChanged = false;

    private int mOldMin = 0;

    private int mOldHour = 0;

    private int mCrossMidNight = 0;

    public boolean mCanSnooze = false;

    private boolean mPastDue = false;

    private InputMethodManager mInputMnger;

    private ImageButton mImageButton;

//    LayoutInflater mInflator;
//
//    String mPrefix;
}
