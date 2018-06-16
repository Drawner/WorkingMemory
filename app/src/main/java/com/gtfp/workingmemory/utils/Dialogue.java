package com.gtfp.workingmemory.utils;

import com.gtfp.workingmemory.R;
import com.gtfp.workingmemory.app.appController;
import com.gtfp.workingmemory.app.appView;
import com.gtfp.workingmemory.todo.ToDoAlarm;
import com.gtfp.workingmemory.todo.ToDoItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;


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
public class Dialogue{

    private final String mMessage;

    private final int mId;

    private final ToDoItem mItemToDo;

    private Activity mActivity;

    private AlertDialog mDialog;

    private Intent mIntent;

    private ToDoAlarm.Manager mAlarmManager;



    public Dialogue(Activity activity){

        mActivity = activity;

        mIntent = mActivity.getIntent();

        mItemToDo = (ToDoItem) mIntent.getSerializableExtra("item");

        mMessage = mItemToDo.itemToDisplay() + "... \r\n" +
                mItemToDo.getDueDateToDisplay().replaceAll("\\r\\n", " ");
// Don't need this Now we're putting in a new line.
//            mMessage = mMessage.replaceAll("\\r\\n", " ");

//            mId = mIntent.getIntExtra(ALARM_ID, 0);

        mId = (int) mItemToDo.getId();

        mDialog = buildDialog();

        mDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
    }



    public void show(){

        mDialog.show();
    }



    private AlertDialog buildDialog(){

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder alertBuilt = new AlertDialog.Builder(mActivity);

        alertBuilt.setMessage(mMessage)
                .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener(){

                    public void onClick(DialogInterface dialog, int id){

                        Delete();

//                            dialog.cancel();

                        mActivity.finish();
                    }
                })
                .setNeutralButton(R.string.edit, new DialogInterface.OnClickListener(){

                    public void onClick(DialogInterface dialog, int id){

//                            dialog.cancel();

                        mActivity.finish();

                        Edit();
                    }
                })
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener(){

                    public void onClick(DialogInterface dialog, int id){

                        // Seems to be called by finish()
//                            dialog.cancel();

                        mActivity.finish();

                        Cancel();
                    }
                });

        // Create the AlertDialog object and return it
        return alertBuilt.create();
    }



    private void Delete(){

        // Called by 'external' applications.
        appView app = new appView(mActivity);

        if (app.open()){

            app.delete(mId);
        }

        app.close();
    }



    private void Edit(){

        // Turn off the notification if any.
        ToDoAlarm.Manager.cancelNotification(mActivity, mItemToDo);

        mIntent.setClass(mActivity, appController.class);

        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NO_HISTORY);

        mActivity.startActivity(mIntent);
    }



    private void Cancel(){

        if (mIntent.getBooleanExtra("clear_notification", false)){

            ToDoAlarm.Manager.cancelNotification(mActivity, mItemToDo);
        }

        //           ToDoAlarm.Manager.repeatAlarm(mActivity, mItemToDo);
    }



    public void onDestroy(){

        mActivity = null;

        mDialog = null;

        mIntent = null;

        mAlarmManager = null;
    }
}
