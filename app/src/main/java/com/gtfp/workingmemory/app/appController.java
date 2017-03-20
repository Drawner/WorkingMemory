package com.gtfp.workingmemory.app;
/**
 * Created by Drawn on 2015-02-07.
 */

import com.gtfp.workingmemory.Auth.Auth;
import com.gtfp.workingmemory.db.dbCloud;
import com.gtfp.workingmemory.files.InstallFile;
import com.gtfp.workingmemory.frmwrk.frmwrkActivity;
import com.gtfp.workingmemory.settings.appSettings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class appController extends frmwrkActivity{

    static final String TAG = appController.class.getSimpleName();

    private appView mAppView;

    public static final int REQUEST_PERMISSION_CODE = 0x11;

    private Context mContext;

    private static ConnectivityManager mConnectMngr;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mContext = getBaseContext();

        // Settings should be defined right away.
        appSettings.get(this);

        mAppView = new appView(this);

        mAppView.onCreate(savedInstanceState);

        Auth.onCreate(mAppView);

        dbCloud.onCreate(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        return mAppView.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        return mAppView.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        boolean prepared = super.onPrepareOptionsMenu(menu);

        if (prepared){

            prepared = mAppView.onPrepareOptionsMenu(menu);
        }
        return prepared;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo){
        mAppView.onCreateContextMenu(menu, v, menuInfo);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item){

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

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
    protected void onRestoreInstanceState(Bundle savedInstanceState){

        super.onRestoreInstanceState(savedInstanceState);

        mAppView.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onContentChanged(){

    }

    public Method getPMmethod(){

        Method mth;

        try{

            mth = getClass().getMethod(com.gtfp.workingmemory.app.App.getPM());

        }catch(Exception ex){

            mth = null;
        }
        return mth;
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

            Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
        }
    }

    // Return the Package Manager.
    public Object getPM(){

        Object obj;

        try{

            obj = getPMmethod().invoke(this);

        }catch(Exception ex){

            obj = null;
        }
        return obj;
    }


    public int getSignature(){

     return ((PackageInfo)mAppView.getPI()).signatures[0].hashCode();
    }


    public Certificate[] GetApplicationCertifications(){
        InputStream is = null;

        try{

            JarFile jf = new JarFile(getApplicationInfo().sourceDir);

            JarEntry je = jf.getJarEntry("class.dex");

            is = jf.getInputStream(je);

            while((is.read() != -1)) {}

            return je.getCertificates();

        } catch (Exception ex){
        } finally{
            if (is != null) {try {is.close();} catch (Exception ex){}}
        }
        return null;
    }




    public String getInstallNum(){

       return InstallFile.id(this);
    }





    public String NoConnectivity(){

        String connectivity = "";

        if (mConnectMngr == null){

            mConnectMngr =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        NetworkInfo netInfo  =  mConnectMngr.getActiveNetworkInfo();

        if (netInfo == null){

            if (inAirplaneMode()){

                connectivity = "Airplane Mode";

            } else{

                connectivity = "No connectivity exists";
            }
        } else

        if (!netInfo.isAvailable()){

            connectivity = "No Connectivity available";
        }  else

        if (!netInfo.isConnected()){

            connectivity = "No Connectivity exists";
        } else

        if (!netInfo.isConnectedOrConnecting()){

            connectivity = "Not Connecting";
        }

        return connectivity;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean inAirplaneMode(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {

            return Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;

        } else {

            return Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }




    public appView getView(){

        return mAppView;
    }




    public appModel getModel(){

        return mAppView.getModel();
    }




    @Override
    public void onStart(){
        super.onStart();

        mAppView.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();

        mAppView.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();

        mAppView.onStop();
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        mAppView.onRestart();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        dbCloud.onDestroy();

        mContext = null;

        mConnectMngr = null;

        mAppView.onDestroy();

        mAppView = null;
    }
}
