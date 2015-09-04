package com.gtfp.workingmemory.app;
/**
 * Created by Drawn on 2015-02-07.
 */

import com.gtfp.workingmemory.frmwrk.frmwrkActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class appController extends frmwrkActivity {

    static final String TAG = appController.class.getSimpleName();

    appView mAppView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppView = new appView(this);

        if (!mAppView.onCreate(savedInstanceState)) finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return mAppView.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return mAppView.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        mAppView.onCreateContextMenu(menu, v, menuInfo);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        return mAppView.onContextItemSelected(item);
    }

//    @Override
//    // When a date is selected from the date dialogue window
//    public void returnDate(int year, int month, int day) {
//
//        mAppView.returnDate(year, month , day);
//    }

    // Called when a startActivityForResult() is called from this Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mAppView.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    // Called when the activity is re-launched while at the top of the activity stack instead
    // of a new instance of the activity being started.
    protected void onNewIntent(Intent intent){

        mAppView.onNewIntent(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState){
        // Save the user's interface state

        mAppView.onSaveInstanceState(outState);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        mAppView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onContentChanged() {

    }

    @Override
    protected void onStop() {
        super.onStop();

        mAppView.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mAppView.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAppView.onDestroy();

        mAppView = null;
    }
}
