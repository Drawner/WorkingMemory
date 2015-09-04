package com.gtfp.workingmemory.todo;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.net.Uri;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


// TodoActivity
public class ToDoItem implements Serializable {

    public ToDoItem() {

        mDateTime = new ToDoDateTime();

//        if (mTempCal == null) {
//
//            mTempCal = Calendar.getInstance();
//            mTempCal.set(Calendar.SECOND, 0);
//            mTempCal.set(Calendar.MILLISECOND, 0);
//        }
    }


    public boolean setItemName(String name) {

        if (name == null) {
            return false;
        }

        mItemName = name;

        return true;
    }


    public String getItemName() {

        return mItemName;
    }


    public void setId(long id) {

        mId = id;
    }


    public void setId(String id) {

        mId = Integer.parseInt(id);
    }


    public long getId() {

        return mId;
    }

    public boolean newItem(boolean newItem) {

        mNewItem = newItem;

        return mNewItem;
    }

    public boolean newItem() {

        return mNewItem;
    }

    public boolean setAlarm(boolean setAlarm) {

        // Once false it can't be changed.
        if (mSetAlarm  && !setAlarm) {

            mSetAlarm = setAlarm;
        }

        return mSetAlarm;
    }

    public boolean setAlarm() {

        return mSetAlarm;
    }

    public int setListPosn(int position) {

        int current = mListPosn;

        mListPosn = position;

        return current;
    }

    public int getListPosn() {

        int current = mListPosn;

        // TODO Would it be best to reset once accessed? Would it be accessed again?
        mListPosn = -1;

        return current;
    }

//    public static String getNextYear() {
//
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.SECOND,0);
//        cal.set(Calendar.MILLISECOND,0);
//        cal.setTime(new Date());
//        cal.add(Calendar.YEAR, 1); // to get previous year add -1
//        Date nextYear = cal.getTime();
//        return formatDate(nextYear);
//    }

    /**
     * TODO These two functions are never used.
     * static String formatDate(Date dt) {
     *
     * return mDateFormat.format(dt);
     * }
     *
     * static String formatDate(long epoch) {
     *
     * return mDateFormat.format(new Date(epoch));
     * }
     */
    static String formatDateTime(long epoch) {

//        return mStoredFormat.format(new Date(epoch));

//        mTempDate.setTime(epoch);
//        return mStoredFormat.format(mTempDate);

        return mStoredFormat.print(epoch);

    }

    static String formatTime(long epoch) {

//        return m12TimeFormat.format(new Date(epoch));

//        mTempDate.setTime(epoch);
//        return m12TimeFormat.format(mTempDate);

        return m12TimeFormat.print(epoch);
    }

    static String formatDisplay(long epoch) {

//        Date date = new Date(epoch);

//        mTempDate.setTime(epoch);
//        return mDayFormat.format(mTempDate) + "\r\n" + m12TimeFormat.format(mTempDate);

        return mDayFormat.print(epoch) + "\r\n" + m12TimeFormat.print(epoch);
    }


    static String formatW_M_D(long epoch) {

//        Date date = new Date(epoch);

//        mTempDate.setTime(epoch);
//        return mDayFormat.format(mTempDate);

        return mDayFormat.print(epoch);
    }


    public static String formatW_M_D_12H(long epoch) {

//        Date date = new Date(epoch);

//        mTempDate.setTime(epoch);
//        return mDayFormat.format(mTempDate) + " " + m12TimeFormat.format(mTempDate);

        return mDayFormat.print(epoch) + " " + m12TimeFormat.print(epoch);
    }


    static String formatW_Y_M_D_12H(long epoch) {

//        mTempDate.setTime(epoch);
//        return mW_Y_M_D12HrFormat.format(mTempDate);

        return mW_Y_M_D12HrFormat.print(epoch);
    }


    public static String EpochToDateTime(long epoch) {

        // This is the format to store in the database not necessarily to display.
//        return mStoredFormat.format(new Date(epoch));

//        mTempDate.setTime(epoch);
//        return mStoredFormat.format(mTempDate);

        return mStoredFormat.print(epoch);
    }


    static Calendar EpochToCalendar(long epoch) {

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(epoch);
//        calendar.set(Calendar.SECOND,0);
//        calendar.set(Calendar.MILLISECOND,0);

        mTempCal.setTimeInMillis(epoch);
        return mTempCal;

//        return calendar;
    }


    Date DueDate() {

//        return justDate(mDueDateCalendar);
        return justDate(mDueDateInEpoch);
    }


    static Date TodayDate() {

        return justDate(null);
    }


    static Date TomorrowDate() {

        mTempCal.setTime(new Date());
        mTempCal.add(Calendar.DAY_OF_MONTH, 1);
        mTempCal.set(Calendar.SECOND, 0);
        mTempCal.set(Calendar.MILLISECOND, 0);
        return mTempCal.getTime();

//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.SECOND,0);
//        cal.set(Calendar.MILLISECOND,0);
//        cal.setTime(new Date());
//        cal.add(Calendar.DAY_OF_MONTH, 1);
//        return cal.getTime();
    }


    public static Long TodayInEpoch() {

//        Calendar today = Calendar.getInstance();
//        today.set(Calendar.SECOND,0);
//        today.set(Calendar.MILLISECOND,0);
//        return today.getTimeInMillis();

        mTempCal.setTimeInMillis(System.currentTimeMillis());
        mTempCal.set(Calendar.SECOND, 0);
        mTempCal.set(Calendar.MILLISECOND, 0);
        return mTempCal.getTimeInMillis();

//        DateTime now = DateTime.now();
//        return now.getMillis();
    }


    // Return only the first number of characters.
    public String itemToDisplay() {

        String item = mItemName.trim();

        return item.substring(0, item.length() >= 30 ? 30 : item.length());
    }

//    public void setPriority(PriorityLevels priority) {
//        mPriority = priority;
//    }
//
//
//    public PriorityLevels getPriority() {
//        return mPriority;
//    }


    public boolean setDueDate(String duedate) {

        if (duedate == null) {
            return false;
        }

        mDueDate = duedate;

        mDueDateInEpoch = this.convertToEpoch(duedate);

        return mDateTime.set(mDueDateInEpoch);
    }


    public boolean setDueDate(Calendar cal) {

        if (cal == null) {
            return false;
        }

//        mDueDateCalendar = cal;
// Don't need it.
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);

        mDueDateInEpoch = cal.getTimeInMillis();

        mDueDate = EpochToDateTime(mDueDateInEpoch);

        return mDateTime.set(mDueDateInEpoch);
    }


    public boolean setDueDate(long epoch) {

        if (epoch < 0L) {
            return false;
        }

//        mDueDateCalendar = EpochToCalendar(epoch);

        mTempCal.setTimeInMillis(epoch);
        mTempCal.set(Calendar.SECOND, 0);
        mTempCal.set(Calendar.MILLISECOND, 0);

        mDueDateInEpoch = mTempCal.getTimeInMillis();

//        mDueDateInEpoch = epoch;

        mDueDate = EpochToDateTime(mDueDateInEpoch);

        return mDateTime.set(mDueDateInEpoch);
    }


    public String getDueDate() {

        return mDueDate;
    }


    public Calendar getCalendar() {

//       return mDueDateCalendar;

        mTempCal.setTimeInMillis(mDueDateInEpoch);

        return (Calendar) mTempCal.clone();
    }


    /**
     * Give input time in epoch
     */
    public boolean setReminderEpoch(long milliseconds) {

        if (milliseconds < 1L) {
            return false;
        }

        mTempCal.setTimeInMillis(milliseconds);
        mTempCal.set(Calendar.SECOND, 0);
        mTempCal.set(Calendar.MILLISECOND, 0);

        mReminder = mTempCal.getTimeInMillis();

        return true;
    }


    public boolean setReminderEpoch(Calendar dtReminder) {

        if (dtReminder == null) {
            return false;
        }

//        dtReminder.set(Calendar.SECOND, 0);
//        dtReminder.set(Calendar.MILLISECOND, 0);

        mReminder = dtReminder.getTimeInMillis();

        return true;
    }


    public long getReminderEpoch() {

        return mReminder;
    }


    public boolean setReminderChk(int chkPosn) {

        if (chkPosn < 0) {
            return false;
        }

        mReminderChk = chkPosn;

        return true;
    }

    public int getReminderChk() {
        return mReminderChk;
    }


    public int setLEDColor(int color) {

        mOldColor = mLEDColor;

        mLEDColor = color;

        return mOldColor;
    }


    public int getLEDColor() {

        mOldColor = mLEDColor;

        return mLEDColor;
    }


    public boolean LEDColorChanged() {

        return mOldColor != mLEDColor;
    }


    public int setLEDOn(int rateOn) {

        mOldLEDOn = mLEDOn;

        mLEDOn = rateOn;

        return mOldLEDOn;
    }


    public int getLEDOn() {

        mOldLEDOn = mLEDOn;

        return mLEDOn;
    }


    public boolean LEDOnChanged() {

        return mOldLEDOn != mLEDOn;
    }


    public int setLEDOff(int rateOff) {

        mOldLEDOff = mLEDOff;

        mLEDOff = rateOff;

        return mOldLEDOff;
    }


    public int getLEDOff() {

        mOldLEDOff = mLEDOff;

        return mLEDOff;
    }


    public boolean LEDOffChanged() {

        return mOldLEDOff != mLEDOff;
    }


    public void hasFired(boolean fired) {

        mHasFired = fired;
    }


    public boolean hasFired() {

        return mHasFired;
    }


    public boolean settingsChanged() {

        return mSettingsChanged;
    }

    public boolean settingsChanged(boolean changed) {

        boolean hasChanged = changed;

        mSettingsChanged = changed;

        return hasChanged;
    }

    public void setShowDialogue(boolean setting) {

        mShowPopup = setting ? 1 : 2;
    }

    public boolean showDialogue(boolean setting) {

        // if not set use the parameter value;
        if (mShowPopup == 0) {

            mShowPopup = setting ? 1 : 2;
        }

        // 1 is true 2 is false
        return mShowPopup == 1;
    }


    public void setUseVibrate(boolean setting) {

        mUseVibrate = setting ? 1 : 2;
    }


    public boolean useVibrate(boolean setting) {

        // if not set use the parameter value;
        if (mUseVibrate == 0) {

            mUseVibrate = setting ? 1 : 2;
        }

        // 1 is true 2 is false
        return mUseVibrate == 1;
    }


    public void setClearNotification(boolean setting) {

        mClearNotification = setting ? 1 : 2;
    }


    public boolean clearNotification(boolean setting) {

        // if not set use the app setting
        if (mClearNotification == 0) {

            mClearNotification = setting ? 1 : 2;
        }

        // 1 is true 2 is false
        return mClearNotification == 1;
    }


    public void setUseLED(boolean setting) {

        mUseLED = setting ? 1 : 2;
    }


    public boolean useLED(boolean setting) {

        // if not set use the parameter value
        if (mUseLED == 0) {

            mUseLED = setting ? 1 : 2;
        }

        // 1 is true 2 is false
        return mUseLED == 1;
    }


    public void setSound(Uri sound) {

        if (sound == null) {
            return;
        }

        setSound(sound.toString());
    }


    public void setSound(String sound) {

        if (sound == null) {
            return;
        }

        mUriSound = sound;
    }


    public Uri Sound(Uri sound) {

        if (sound == null) {
            return null;
        }

        return Sound(sound.toString());
    }


    public Uri Sound(String sound) {

        if (mUriSound == null) {

            setSound(sound);
        }

        return Uri.parse(mUriSound);
    }


    public int reminderInterval() {

        long diff = 0;

        if (mReminderChk == 0) {

            return 0;

        } else if (mReminder > mDueDateInEpoch) {

            diff = mReminder - mDueDateInEpoch;

        } else if (mDueDateInEpoch > mReminder) {

            diff = mDueDateInEpoch - mReminder;
        }

        return (((int) diff / 1000) / 60);
    }

    public boolean isDeleted() {

        return mDeleted;
    }


    public boolean isDeleted(boolean deleted) {

        mDeleted = deleted;

        return mDeleted;
    }


    public int getYear() {
        return mDateTime.getYear();
    }

    public int getMonth() {
        return mDateTime.getMonth();
    }

    public int getDay() {
        return mDateTime.getDay();
    }

    public int getHour() {
        return mDateTime.getHour();
    }

    public int getMin() {
        return mDateTime.getMin();
    }

    public void setTimestamp() {
        mTimestamp = System.currentTimeMillis() / 1000;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    private long convertToEpoch(String dt) {

        long epoch = 0;

        if (dt == null) {
            return epoch;
        }

        try {

            // Use this format to convert to Epoch.
//            epoch = mStoredFormat.parse(dt, new ParsePosition(0)).getTime();
            epoch = mStoredFormat.parseMillis(dt);

        } catch (RuntimeException e) {

            epoch = TodayInEpoch();
        }
        return epoch;
    }


    public long getDueDateInEpoch() {

        if (mDueDateInEpoch < 1) {
            mDueDateInEpoch = convertToEpoch(mDueDate);
        }

        return mDueDateInEpoch;
    }


    public String getDueDateToDisplay() {

        String displayStr = "";

        Date dateDue = DueDate();

        //TODO Today gets called a lot! Maybe place in a static and reset at some time?
        if (mDueDateInEpoch < TodayInEpoch())

        {
            displayStr = "Past Due";
        } else if (dateDue == TodayDate())

        {
            displayStr = "Today";
        } else if (dateDue == TomorrowDate())

        {
            displayStr = "Tomorrow";
        }

        displayStr = displayStr + "\r\n" + formatDisplay(mDueDateInEpoch);

        return displayStr;
    }


    public String displayDueDate() {

        return displayDueDate(mDueDateInEpoch);
    }


    public static String displayDueDate(long epoch) {

        return formatW_Y_M_D_12H(epoch);
    }

    public static String displayTime(long epoch) {

        String str = displayDueDate(epoch);

        return str.substring(str.indexOf("  ") + 1);
    }


    public static Long toEpoch(int year, int month, int day) {

        mTempCal.set(year, month, day);

        return mTempCal.getTimeInMillis();
    }


    public static Long toEpoch(int year, int month, int day, int hour, int min) {

        mTempCal.set(year, month, day, hour, min);

        return mTempCal.getTimeInMillis();
    }


    public static Date justDate(Calendar cal) {

        if (cal == null) {

            // What time is it right now?
            mTempCal.setTimeInMillis(System.currentTimeMillis());
        } else {

            mTempCal = (Calendar) cal.clone();
        }

        mTempCal.set(Calendar.HOUR_OF_DAY, 0);
        mTempCal.set(Calendar.MINUTE, 0);

        return justDate(mTempCal.getTimeInMillis());

//        Calendar day;
//
//        if ( cal == null){
//
//            day = Calendar.getInstance();
//
//        }else{
//
//            day = (Calendar) cal.clone();
//        }
//
//        day.set(Calendar.HOUR_OF_DAY, 0);
//        day.set(Calendar.MINUTE, 0);
//        day.set(Calendar.SECOND,0);
//        day.set(Calendar.MILLISECOND,0);
//
//        return day.getTime();
    }


    public static Date justDate(long dateTime) {

        mTempCal.setTimeInMillis(dateTime);

        mTempCal.set(Calendar.HOUR_OF_DAY, 0);

        mTempCal.set(Calendar.MINUTE, 0);

        return mTempCal.getTime();
    }

    public static boolean isSameDay(Calendar c1, Calendar c2) {

        return (mEpochFormat.format(c1.getTime()).equals(mEpochFormat.format(c2.getTime())));
    }


    public boolean isPastDue() {

        mTempCal.setTimeInMillis(System.currentTimeMillis());
        mTempCal.set(Calendar.SECOND, 0);
        mTempCal.set(Calendar.MILLISECOND, 0);

        boolean pastDue = mDueDateInEpoch < mTempCal.getTimeInMillis();

        return pastDue;
    }


    public String toString() {

        StringBuffer itemStr = new StringBuffer();
        itemStr.append(getItemName() + ",");
//        itemStr.append(getPriority() + ",");
        itemStr.append(getDueDate() + ",");
        itemStr.append(getReminderEpoch() + ",");
        itemStr.append(getTimestamp() + ",");
        itemStr.append(getId() + ",");
        return itemStr.toString();


    }

    protected void onDestroy() {
        // TODO Can't nullify a static variable.
        //mDateFormat = null;
    }


    private static final long serialVersionUID = -7507171562852085782L;

    private String mItemName = "";

    private boolean mNewItem = false;

    private boolean mSetAlarm = true;
//    private PriorityLevels mPriority = PriorityLevels.LOW;

    private String mDueDate = "None"; // Format yyyy-mm-dd

    private long mDueDateInEpoch = 0;

    private Calendar mDueDateCalendar = null;

    private long mReminder = -1;

    private int mReminderChk = 0;

    private int mOldColor, mLEDColor = 0;

    private int mOldLEDOn, mLEDOn = 0;

    private int mOldLEDOff, mLEDOff = 0;

    private boolean mSettingsChanged = false;

    private boolean mHasFired = false;

    int mShowPopup = 0;

    int mUseVibrate = 0;

    int mClearNotification = 0;

    int mUseLED = 0;

    private String mUriSound = null;

    private long mTimestamp; // in epoch

    private int mListPosn = -1;

    private boolean mDeleted = false;

    private static Date mTempDate = new Date();

    private static Calendar mTempCal = Calendar.getInstance();

    static {
        mTempCal.set(Calendar.SECOND, 0);
        mTempCal.set(Calendar.MILLISECOND, 0);
    }

    private static final String Y_M_D = "yyyy-MM-dd ";

    private static final String W_M_D = "EEE, MMM d ";

    private static final String W_Y_M_D = "EEE, yyyy-MM-dd ";

    private static final String HOURS_12 = " h:mm a";

    private static final String HOURS_24 = " HH:mm";

    //    private static final DateFormat mDateFormat = new SimpleDateFormat(Y_M_D);
//    private static final DateFormat mDayFormat = new SimpleDateFormat(W_M_D);
    private static final DateTimeFormatter mDayFormat = DateTimeFormat.forPattern(W_M_D);

    //    private static final DateFormat mW_Y_M_D12HrFormat = new SimpleDateFormat(W_Y_M_D + HOURS_12);
    private static final DateTimeFormatter mW_Y_M_D12HrFormat = DateTimeFormat
            .forPattern(W_Y_M_D + HOURS_12);

    private static final DateFormat m12DateTimeFormat = new SimpleDateFormat(W_M_D + HOURS_12);

    //    private static final DateFormat m24DateTimeFormat = new SimpleDateFormat(W_M_D + HOURS_24);
//    private static final DateFormat m12TimeFormat = new SimpleDateFormat(HOURS_12);
    private static final DateTimeFormatter m12TimeFormat = DateTimeFormat.forPattern(HOURS_12);

    //    private static final DateFormat m24TimeFormat = new SimpleDateFormat(HOURS_24);
    // This is the format to store in the database not necessarily to display.
//    private static final DateFormat mStoredFormat = new SimpleDateFormat(Y_M_D + HOURS_12);
    private static final DateTimeFormatter mStoredFormat = DateTimeFormat
            .forPattern(Y_M_D + HOURS_12);

    // Used to convert strings to Epoch
    private static final DateFormat mEpochFormat = new SimpleDateFormat(Y_M_D + HOURS_24);

    private ToDoDateTime mDateTime;

//    public static enum PriorityLevels {
//        LOW, MEDIUM, HIGH
//    };

    private long mId = -1; // unique ID for the alarm, only one alarm can be set
    // for each item.


    // Stores the duedate by integers Year, Month, Day, Hour, Minute
    class ToDoDateTime implements Serializable {

        private static final long serialVersionUID = -6506171562852065742L;

        private HashMap<String, Integer> mDateTime;

//        private Calendar mCal = Calendar.getInstance();

        ToDoDateTime() {

            mDateTime = new HashMap<String, Integer>();
        }

        ToDoDateTime(long epoch) {

            this();

            set(epoch);
        }

        protected boolean set(long epoch) {

//            Calendar cal;

            // If empty supply today's date and time.
            if (epoch < 1L) {

                // Get the time right now!
                mTempCal.setTimeInMillis(System.currentTimeMillis());
                mTempCal.set(Calendar.SECOND, 0);
                mTempCal.set(Calendar.MILLISECOND, 0);
            } else {

                mTempCal.setTimeInMillis(epoch);
            }

            mDateTime.put("YEAR", mTempCal.get(Calendar.YEAR));
            // Month is 0-based, so if you wish to display it accurately, add 1.
            mDateTime.put("MONTH", mTempCal.get(Calendar.MONTH));
            mDateTime.put("DAY", mTempCal.get(Calendar.DATE));
            mDateTime.put("HOUR", mTempCal.get(Calendar.HOUR_OF_DAY));
            mDateTime.put("MIN", mTempCal.get(Calendar.MINUTE));

            return true;
        }

        protected int get(String key) {

            int dt = -1;

            try {

                dt = mDateTime.get(key);

            } catch (RuntimeException ex) {

                // What time is it right now!
                mTempCal.setTimeInMillis(System.currentTimeMillis());
                mTempCal.set(Calendar.SECOND, 0);
                mTempCal.set(Calendar.MILLISECOND, 0);

                if (key.equals("YEAR")) {
                    dt = mTempCal.get(Calendar.YEAR);
                } else if (key.equals("MONTH")) {
                    dt = mTempCal.get(Calendar.MONTH);
                } else if (key.equals("DAY")) {
                    dt = mTempCal.get(Calendar.DATE);
                } else if (key.equals("HOUR")) {
                    dt = mTempCal.get(Calendar.HOUR_OF_DAY);
                } else if (key.equals("MIN")) {
                    dt = mTempCal.get(Calendar.MINUTE);
                }
            }
            return dt;
        }

        public int getYear() {
            return this.get("YEAR");
        }

        public int getMonth() {
            return this.get("MONTH");
        }

        public int getDay() {
            return this.get("DAY");
        }

        public int getHour() {
            return this.get("HOUR");
        }

        public int getMin() {
            return this.get("MIN");
        }

        protected void onDestroy() {

            mDateTime = null;
//            mCal = null;
        }
    }

}
