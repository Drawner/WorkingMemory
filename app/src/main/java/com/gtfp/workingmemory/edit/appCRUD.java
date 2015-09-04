package com.gtfp.workingmemory.edit;
/**
 * Created by Drawn on 2015-02-07.
 */

import com.gtfp.workingmemory.app.appView;
import com.gtfp.workingmemory.settings.appSettings;
import com.gtfp.workingmemory.todo.ToDoItem;
import com.gtfp.workingmemory.utils.dialog;

import android.content.Intent;

/**
 * Created by Drawn on 2015-03-07.
 */
public class appCRUD implements dialog.DialogBoxListener {

    appView mAppView;

    private Intent mEditIntent;

    private dialog mDialogue;

    private boolean mTrulyDelete = false;

    private int mToDoListID;

    public appCRUD(appView mVc) {

        mAppView = mVc;

        mDialogue = new dialog(mAppView.mController);

        mDialogue.setDialogBoxListener(this);
    }


    public void newToDoItem() {

        edit(-1);
    }


    public void edit(int position) {

        final int REQUEST_CODE = 1;

        // Lazy initialization.
        if (mEditIntent == null) {

            mEditIntent = new Intent(mAppView.mController, editToDoItem.class);
        }

// No need I think despite the interface delay.
//        mEditIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mEditIntent.putExtra("position", position);

        ToDoItem todoItem = (ToDoItem) mAppView.mToDoListAdapter.getItem(position);

        todoItem.setListPosn(position);

        mEditIntent.putExtra("item", todoItem);

        mAppView.mController.startActivityForResult(mEditIntent, REQUEST_CODE);
    }


    public boolean save(ToDoItem itemToDo) {

        // In case it WAS marked as deleted.
        itemToDo.isDeleted(false);

        boolean saved = mAppView.mAppModel.save(itemToDo);

        if (!saved) {

            return saved;
        }

        // It's a new item
        if (itemToDo.getId() < 1) {

            itemToDo.setId(mAppView.mAppModel.getLastRowID());

            mAppView.mToDoListAdapter.add(itemToDo);

        } else {

            mAppView.mToDoListAdapter.save(itemToDo);

            // Remove any associated notification.
            mAppView.mAlarmManager.cancelNotification(itemToDo);
        }

        // In some circumstances, an alarm is not set.
        if (itemToDo.setAlarm()) {

            // Establish the alarm for this item.
            mAppView.mAlarmManager.setAlarm(itemToDo);
        }

        if (appSettings.getBoolean("set_as_wallpaper", false)) {

            mAppView.mItemsOnWallpaper.set();
        }

        return saved;
    }


    public boolean delete(int todoListID) {

        // Flag to permanently delete a record.
        boolean trueDelete = false;

        boolean deleted = false;

        // Retrieve the currently 'selected' item.
        ToDoItem todoItem = (ToDoItem) mAppView.mToDoListAdapter.getItem(todoListID);

        todoItem.setListPosn(todoListID);

        boolean alreadyDeleted = todoItem.isDeleted();

        if (alreadyDeleted) {

            mToDoListID = todoListID;

            mDialogue.show("Permanently delete this already deleted record?");
        } else {

            if (mTrulyDelete) {

                // Important to reset in case of failure.
                mTrulyDelete = false;

                trueDelete = true;

                deleted = mAppView.mAppModel.trueDelete(todoItem);
            } else {

                deleted = mAppView.mAppModel.delete(todoItem);
            }
        }

        if (deleted) {

            // Remove from listing if NOT to show deleted records
            if (!trueDelete && !alreadyDeleted && mAppView.showDeleted()) {

                mAppView.mToDoListAdapter.save(todoItem);
            } else {

                mAppView.mToDoListAdapter.remove(todoItem);
            }

            mAppView.mAlarmManager.cancelAlarm(todoItem);
        }

        return deleted;
    }


    // Called if there is possibly no interface. (i.e. Notifictions will call this at times.)
    public boolean delete(long id) {

        boolean deleted = mAppView.mAppModel.delete(id);

        if (deleted) {

            // Cancels any alarm and clears any notification.
            mAppView.mAlarmManager.cancelAlarm(id);
        }

        return deleted;
    }


    public void dialogResult(boolean result) {

        if (!result) {
            return;
        }

        ToDoItem todoItem = (ToDoItem) mAppView.mToDoListAdapter.getItem(mToDoListID);

        // Allows you to truly delete the record.
        todoItem.isDeleted(false);

        todoItem = null;

        // Important to now indicate a true delete is to be performed.
        mTrulyDelete = true;

        delete(mToDoListID);
    }


    public void onDestroy() {

        mAppView = null;

        mEditIntent = null;

        if (mDialogue != null) {

            mDialogue.onDestroy();

            mDialogue = null;
        }
    }
}
