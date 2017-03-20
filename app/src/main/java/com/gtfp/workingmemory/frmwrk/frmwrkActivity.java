package com.gtfp.workingmemory.frmwrk;

import com.gtfp.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.app.App;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;


public class frmwrkActivity extends Activity{

    protected static String PACKAGE_NAME = "";

    protected static String INTENT_PACKAGE_NAME = "";

    private boolean mJustCreated = true;

    private boolean mRecreated = false;

    private boolean mWasPaused = false;

    private boolean mWasStopped = false;

    private boolean mWasOutsideCall = false;

    private LayoutInflater mInflator;

    private String mPath;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (App.inDebugger()){

            ErrorHandler.toCatch(this);
        }

        // SPECIAL CASE: Most times savedInstanceState is null.
        // Check whether we're recreating a previously destroyed instance
        mRecreated = savedInstanceState != null;
        // Use wasRecreated() here

        if (PACKAGE_NAME.equals("")){

            PACKAGE_NAME = this.getPackageName();
        }

        if (INTENT_PACKAGE_NAME.equals("")){

            // Get the message from the intent
            Intent intent = getIntent();

            INTENT_PACKAGE_NAME = intent.getComponent().getPackageName();
        }

        // Has this been called by another app?
        mWasOutsideCall = !INTENT_PACKAGE_NAME.equals(PACKAGE_NAME);

// getActionBar() creates  mContentParent = generateLayout(mDecor) which is premature.
//        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//
//            // For the main activity, make sure the app icon in the action bar
//            // does not behave as a button
//            ActionBar actionBar = getActionBar();
//
//            if(actionBar != null){
//
//               actionBar.setHomeButtonEnabled(false);
//            }
//        }

        // Used in creating custom views.
        mInflator = LayoutInflater.from(this);

        mPath = this.getComponentName().getClassName();

        mPath = mPath.substring(0, mPath.lastIndexOf(".") + 1);
    }


    @Override
    protected void onStart(){
        super.onStart();

        // Use justCreated() here for that one special case
        // Use wasStopped() here
        // Use wasRecreated() here
    }

    /*
     * Called after onStart()
     * 
     */
    @Override
    protected void onResume(){
        super.onResume();

        // The Activity property is set: this.mResumed = true;
        // Use justCreated() here for that one special case
        // Use wasPaused() here
        // Use wasStopped() here
        // Use wasRecreated() here
    }

    /**
     * Called when activity resume is complete (after {@link #onResume} has
     * been called). Applications will generally not implement this method;
     * it is intended for system classes to do final setup after application
     * resume code has run.
     * /
     *
     *
     * /*
     * When paused, the Activity instance is still kept as a resident in memory.
     * Allows you to stop ongoing and 'resource hog' actions that should not continue while paused.
     * Release system resources.
     * Good time possibly to save any information that should be
     * but do avoid performing CPU-intensive work here such as writing to a database.
     */
    @Override
    protected void onPause(){
        super.onPause();

        mJustCreated = false;
        mWasStopped = false;
        mWasPaused = true;
    }

    /*
     * Only fired after a 'stopped' state.
     *
     */
    @Override
    protected void onRestart(){
        super.onRestart();

    }


    /*
     * The system retains the Activity instance in system memory when it is stopped,
     * Here, CPU-intensive work to save data to a database.
     * The system might destroy the instance if it needs to recover system memory.
     * The system might even kill your app process without calling onDestroy()
     * Even if the system destroys the activity, the state of the View objects is still retained in a Bundle 
     * 
     */
    @Override
    protected void onStop(){
        super.onStop();

        // Important to note it's no longer paused.
        mWasPaused = false;
        mWasStopped = true;
    }

    /*
     * Most apps don't need to implement this method because local class references are destroyed
     * with the activity and your activity should perform most cleanup during onPause() and onStop().
     * However, if your activity includes background threads that you created during onCreate()
     * or other long-running resources that could potentially leak memory if not properly closed,
     * you should kill them during onDestroy().
     *
     * You might call finish() from within onCreate() to destroy the activity.
     * In that case, the system immediately calls onDestroy() without calling
     * any of the other lifecycle methods.
     *
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();

        // May be coming straight from onCreate()
        // Use wasPaused() here
        // Use wasRecreated() here

        mInflator = null;

        mPath = null;
    }

    /*
    This allows for custom views to be used in XML files without specifying
    the class' full package name (i.e. directory path).
     */
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs){

        View view;

//        if (mInflator == null){
//
//            mInflator = LayoutInflater.from(context);
//
//            mPrefix = ((Activity) context).getComponentName().getClassName();
//
//            mPrefix = mPrefix.substring(0, mPrefix.lastIndexOf(".")+1);
//        }

        // Don't bother if 'a path' os already specified.
        if (name.indexOf('.') > -1){

            return null;
        }

        try{

            view = mInflator.createView(name, mPath, attrs);

        }catch (ClassNotFoundException e){

            view = null;

        }catch (InflateException e){

            view = null;
        }

        return view;
    }


    /*
     * Called after onPause()
     * Override to save additional data about your activity's state.
     * The system might destroy this activity while waiting in the back stack.
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

    }

    /*
     * Called after onStart() and before  onPostCreate(Bundle)
     *
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        // Use wasRecreated() here
    }

    /*
     * Called when activity start-up is complete (after onStart() and onRestoreInstanceState(Bundle) have been called).
     * Applications will generally not implement this method; it is intended for system classes to do
     * final initialization after application code has run.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);

    }


    protected final Boolean justCreated(){

        return mJustCreated;
    }

    protected final Boolean wasStopped(){

        return mWasStopped;
    }

    protected final Boolean wasPaused(){

        return mWasPaused;
    }

    protected final Boolean wasRecreated(){

        return mRecreated;
    }

    protected final Boolean wasCalledOutside(){

        return mWasOutsideCall;
    }

    // If the manifest has the debug set.
    public final boolean DebugFlagSet(){

        return 0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
    }
}
