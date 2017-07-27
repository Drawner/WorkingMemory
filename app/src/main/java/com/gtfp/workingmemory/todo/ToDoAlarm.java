package com.gtfp.workingmemory.todo;

import com.gtfp.workingmemory.BuildConfig;
import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.app.appController;
import com.gtfp.workingmemory.app.appView;
import com.gtfp.workingmemory.colorPicker.colorPickerView;
import com.gtfp.workingmemory.edit.AlarmActivity;
import com.gtfp.workingmemory.settings.appSettings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;


/**
 * Created by Drawn on 2015-02-27.
 */
public class ToDoAlarm extends BroadcastReceiver {

    private static final String TAG = ToDoAlarm.class.getSimpleName();

    private static final String RESET_ALARMS = "android.intent.action.BOOT_COMPLETED";

    private static final String INTENT_ACTION = "ToDoAlarm";

    private static final String TODOITEM = "item";

    private static final String SHOW_POPUP = "showpopup";

    private static final String CLEAR_NOTIFY = "clear_notification";

    private static final String ALARM_ID = "id";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equals(RESET_ALARMS)) {

            resetAlarms(context);

            return;
        }

        // If it's not the right intent, we're out!
        if (!action.equals(INTENT_ACTION)) {

            return;
        }

        Manager.Notification(context, intent);

        // TODO Maybe set this independently for each new item.
        if (intent.getBooleanExtra(SHOW_POPUP, true)) {

            showDialogue(context, intent);
        }
    }


    private void resetAlarms(Context context) {

        appView viewApp = new appView(context);

        if (viewApp.open()) {

            viewApp.resetAlarms();
        }

        viewApp.onDestroy();
    }


    private void showDialogue(Context context, Intent intent) {

        // Must use a final context in a new thread.
        final Context con = context;
        final Intent in = new Intent(con, AlarmActivity.class);

//        in.putExtra(ALARM_ITEM, intent.getStringExtra(ALARM_ITEM));
//
//        in.putExtra(ALARM_TIME, intent.getStringExtra(ALARM_TIME));
//
//        in.putExtra(ALARM_ID, intent.getIntExtra(ALARM_ID, 0));

        in.putExtra(TODOITEM, (ToDoItem) intent.getSerializableExtra(TODOITEM));

        in.putExtra(CLEAR_NOTIFY, intent.getBooleanExtra(CLEAR_NOTIFY, false));

//        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);

//        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

//        in.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //android:launchMode="singleTask" in AndroidManifest.xml
//        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NO_HISTORY);

        final BroadcastReceiver.PendingResult pending = goAsync();

        Thread thread = new Thread() {

            public void run() {

                con.startActivity(in);
                // TODO I don't know if this is the right place to call finish.
                pending.finish();
            }
        };

        thread.start();
    }


    public static class Manager implements colorPickerView.OnColorChangedListener {


        private static NotificationManager mNotificationManager;

        private appController mApp;

        private static Context mContext;

        private static AlarmManager mAlarmManager;

        static Intent mAlarmIntent;

        static int mLedArgb = 0xFF0000FF;

        static int mLedOnMs = 100;

        static int mLedOffMs = 2000;

        public Manager(Context context) {

            initAlarmManager(context);

            colorPickerView.setOnColorChangedListener(this);
        }


        static void initAlarmManager(Context context) {

            // This class is already running
            if (mContext != null) {
                return;
            }

            mContext = context;

            mAlarmManager = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);

            mAlarmIntent = new Intent(mContext, ToDoAlarm.class);

            mAlarmIntent.setAction(INTENT_ACTION);

            mNotificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }


        public void setAlarm(ToDoItem itemToDo) {

//            mAlarmIntent.putExtra(ALARM_ITEM, itemToDo.itemToDisplay());

//            mAlarmIntent.putExtra(ALARM_TIME, itemToDo.getDueDateToDisplay().replaceAll("\r\n", " "));

            int id = (int) itemToDo.getId();

//            mAlarmIntent.putExtra(ALARM_ID, id);

            // Copy over any notification information from an alarm intent to an item.
//            copyOver(itemToDo);

            mAlarmIntent.putExtra(TODOITEM, itemToDo);

            mAlarmIntent.putExtra(SHOW_POPUP, itemToDo.showDialogue(appView.showDialogue()));

            mAlarmIntent.putExtra(CLEAR_NOTIFY, itemToDo.clearNotification(
                    appView.clearNotification()));

            // Cancels any existing alarms on this item.
            PendingIntent pendingIntent = PendingIntent
                    .getBroadcast(mContext, id, mAlarmIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);

            long reminder;

            String time = "";

//            if (BuildConfig.DEBUG) {

                reminder = itemToDo.getReminderEpoch();

                time = ToDoItem.EpochToDateTime(reminder);

                time = null;
  //          } else {

                Calendar remDate = ToDoItem.EpochToCalendar(itemToDo.getReminderEpoch());

                // Take off a minute to be accurate???
                remDate.add(Calendar.MINUTE, -1);

                reminder = remDate.getTimeInMillis();
   //         }

            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder, pendingIntent);
        }


        static void repeatAlarm(Context context, ToDoItem itemToDo) {

            initAlarmManager(context);

//            mAlarmIntent.putExtra(ALARM_ITEM, itemToDo.itemToDisplay());

//            mAlarmIntent.putExtra(ALARM_TIME, itemToDo.getDueDateToDisplay());

            int id = (int) itemToDo.getId();

//            mAlarmIntent.putExtra(ALARM_ID, id);

            mAlarmIntent.putExtra(TODOITEM, itemToDo);

            PendingIntent pendingIntent = getPendingIntent(id);

            if (pendingIntent != null) {

                cancelAlarm(pendingIntent);
            }

            int interval = itemToDo.reminderInterval();

            if (interval == 0) {

                interval = 15;
            }

            Calendar cal = ToDoItem.EpochToCalendar(itemToDo.getReminderEpoch());

            cal.add(Calendar.MINUTE, interval);

            long alarmTime = cal.getTimeInMillis();

            String setTime = "";

            if (BuildConfig.DEBUG) {

                setTime = ToDoItem.EpochToDateTime(alarmTime);

            }

            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }


        public void cancelAlarm(ToDoItem itemToDo) {

            cancelAlarm(itemToDo.getId());
        }


        public void cancelAlarm(long id) {

            cancelAlarm((int) id);
        }


        void cancelAlarm(int id) {

            PendingIntent pendingIntent = getPendingIntent(id);

            if (pendingIntent != null) {

                cancelAlarm(pendingIntent);
            }

            // Cancel if there happens to be an notification for it displayed as well.
            cancelNotification(id);
        }


        static void cancelAlarm(PendingIntent pendingIntent) {

            mAlarmManager.cancel(pendingIntent);
        }


        static PendingIntent getPendingIntent(int id) {

            // It's possible at times.
            if (mContext == null) {

                return null;
            }

            //Do not create, just return it if exists
            return PendingIntent.getBroadcast(mContext, id, mAlarmIntent,
                    PendingIntent.FLAG_NO_CREATE);
        }


        static Intent getAlarmIntent(ToDoItem itemToDo) {

            return getAlarmIntent((int)itemToDo.getId());
        }



        /**
         * Return the Intent for PendingIntent.
         * Return null in case of some errors: see Android source.
         * @throws IllegalStateException in case of something goes wrong.
         * See {@link Throwable#getCause()} for more details.
         */
        static Intent getAlarmIntent(int id) {

            PendingIntent pending = getPendingIntent(id);

            if (pending == null) return null;

            Intent intent;

            try {

                Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");

                intent = (Intent) getIntent.invoke(pending);

            } catch (NoSuchMethodException ex){

                intent = null;

            } catch(InvocationTargetException ex){

                intent = null;

            } catch (IllegalAccessException ex) {

                intent = null;
            }

            return intent;
        }


        /*
        If the event item already exists in an alarm intent,
        copy over the saved notification settings from that existing event item.
         */
        public static boolean copyOver(ToDoItem itemToDo){

            Intent alarmIntent = getAlarmIntent(itemToDo);

            // If there's no alarm intent no need to continue.
            if (alarmIntent == null || !alarmIntent.hasExtra(TODOITEM)){

                return false;
            }

            ToDoItem prevItem = (ToDoItem) alarmIntent.getSerializableExtra(TODOITEM);

            itemToDo.setShowDialogue(prevItem.showDialogue(true));

            itemToDo.setUseVibrate(prevItem.useVibrate(false));

            itemToDo.setClearNotification(prevItem.clearNotification(true));

            itemToDo.setUseLED(prevItem.useLED(true));

            itemToDo.setSound(prevItem.Sound(Manager.getAlarmUri()));

            return true;
        }


        public void cancelNotification(ToDoItem itemToDo) {

            cancelNotification((int) itemToDo.getId());
        }


        public static void cancelNotification(int id) {

            if (mNotificationManager != null) {
                mNotificationManager.cancel(id);
            }
        }


        public static void cancelNotification(Context context, ToDoItem itemToDo) {

            if (mNotificationManager == null) {

                mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
            }

            cancelNotification((int) itemToDo.getId());
        }

        private static void Notification(Context context, Intent intent) {

            ToDoItem itemToDo = (ToDoItem) intent.getSerializableExtra(TODOITEM);

//            String alarmItem = intent.getStringExtra(ALARM_ITEM);

            String alarmItem = itemToDo.itemToDisplay();

            // Open the application itself if they tap on the notification.
            Intent notifyIntent = new Intent(context, appController.class);

            notifyIntent.putExtra(TODOITEM, itemToDo);

//            int itemId = intent.getIntExtra(ALARM_ID, 0);

            int itemId = (int) itemToDo.getId();

//            notifyIntent.putExtra(ALARM_ID, itemId);

//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Close any other activities on top of it
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent
                    .getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notify = new Notification.Builder(context)
                    .setContentTitle(alarmItem)
                    .setContentText(itemToDo.getDueDateToDisplay().replaceAll("\r\n", " "))
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setSound(itemToDo.Sound(appSettings.getString("sound_notification",
                            ToDoAlarm.Manager.getAlarmUri().toString())))
//                    .setLights(mLedArgb, mLedOnMs, mLedOffMs)
                    .build();

            SharedPreferences settings = appSettings.get(context);

            if (itemToDo.useLED(settings.getBoolean("use_LED", true))){

                    int LEDSetting = itemToDo.getLEDColor();

                    notify.ledARGB = LEDSetting != 0 ? LEDSetting : settings.getInt("mLedArgb", 0xFF0000FF);

                    LEDSetting = itemToDo.getLEDOn();

                    notify.ledOnMS = LEDSetting != 0 ? LEDSetting : settings.getInt("LedOnMs", 100);

                    LEDSetting = itemToDo.getLEDOff();

                    notify.ledOffMS = LEDSetting != 0 ? LEDSetting : settings.getInt("LedOffMs", 2000);

                    notify.flags |= Notification.FLAG_SHOW_LIGHTS;
            }

            if (itemToDo.useVibrate(settings.getBoolean("vib_notification", false))){

                notify.defaults |= Notification.DEFAULT_VIBRATE;
            }

            // This is a static call and so NotificationManager may be null
            if (mNotificationManager == null) {

                mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
            }

            mNotificationManager.notify(itemId, notify);
        }


        //Get an alarm sound. Try for an alarm, notification and then a ringtone.
        public static Uri getAlarmUri() {

            Uri sound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (sound == null) {

                sound = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_ALARM);

                if (sound == null) {

                    sound = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            return sound;
        }


        // Set the colour selected in the colorPickerActivity
        public void colorChanged(int color) {

            mLedArgb = color;
        }


        public void onDestroy() {

            mApp = null;

            mContext = null;

            mAlarmManager = null;

            mAlarmIntent = null;

            mNotificationManager = null;
        }
    }
}
