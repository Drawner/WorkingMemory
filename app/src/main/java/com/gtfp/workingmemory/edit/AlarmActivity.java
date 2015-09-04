package com.gtfp.workingmemory.edit;

import com.gtfp.workingmemory.utils.Dialogue;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Drawn on 2015-03-01.
 */
public class AlarmActivity extends Activity {

    private static final String ALARM_ITEM = "item";

    private static final String ALARM_TIME = "time";

    private static final String ALARM_ID = "id";

    private Dialogue mDialWnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialWnd = new Dialogue(this);

        mDialWnd.show();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();

        mDialWnd.onDestroy();

        mDialWnd = null;
    }
}
