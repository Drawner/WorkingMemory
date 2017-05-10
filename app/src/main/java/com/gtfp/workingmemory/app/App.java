package com.gtfp.workingmemory.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.util.Base64;

import java.io.File;

//import org.acra.ReportField;
//import org.acra.ReportingInteractionMode;
//import org.acra.annotation.ReportsCrashes;

/**
 * Created by Drawn on 2016-03-09.
 */

//@ReportsCrashes(
//        mode = ReportingInteractionMode.SILENT
//        ,deleteOldUnsentReportsOnApplicationStart = true
//        ,mailTo = "gtfperry@gmail.com"
//        ,resDialogText = R.string.crash_dialog_text
//        ,customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
//                ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
//                ReportField.STACK_TRACE, ReportField.LOGCAT})

// A top-level Java class mimicking static class behavior
// ** Hid this old one ** public final class App {
public final class App extends Application{

    private static String PACKAGE_NAME = "package.name.unknown";

    private static File FILES_DIR;

    private static String LABEL;

    private static appView VIEW;

    private static appModel MODEL;




    @Override
    public void onCreate(){
        // The following line triggers the initialization of ACRA
        super.onCreate();

//        ACRA.init(this);
    }


    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);

        // Oh my it worked! Upgraded to Android 25.
        MultiDex.install(this);
    }



    public static String setPackageName(Activity activity){

        if (PACKAGE_NAME == "package.name.unknown"){

            PACKAGE_NAME = activity.getApplicationContext().getPackageName();
        }

        return PACKAGE_NAME;
    }


    public static String PackageName() {

        return PACKAGE_NAME;
    }


    public static File getFilesDir(Context context){

        if (FILES_DIR == null){

            FILES_DIR = context.getFilesDir();
        }

        return FILES_DIR;
    }


    public static File FilesDir(){

        if(FILES_DIR == null){

            return new File(Environment.getExternalStorageDirectory(), "");
        }else{

           return FILES_DIR;
        }
    }


    public static String setLabel(Context pContext){

        PackageManager lPackageManager = pContext.getPackageManager();

        ApplicationInfo lApplicationInfo = null;

        try{

            lApplicationInfo = lPackageManager
                    .getApplicationInfo(pContext.getApplicationInfo().packageName, 0);

        }catch (final PackageManager.NameNotFoundException e){
        }

        LABEL = (String) (lApplicationInfo != null ? lPackageManager
                .getApplicationLabel(lApplicationInfo) : "Unknown");

        return LABEL;
    }


    public static String getLabel(Context pContext){

        if (LABEL == null){

            LABEL = setLabel(pContext);
        }

        return LABEL;
    }


    public static String getLabel(){

        if (LABEL == null){

            return "Unknown";
        }

        return LABEL;
    }


    public static void setView(appView view){

        if (VIEW == null){

            VIEW = view;
        }
    }


    public static appView getView() throws NullPointerException{

        if (VIEW == null){

            throw new NullPointerException();
        }

        return VIEW;
    }


    public static void setModel(appModel model){

        if (MODEL == null){

            MODEL = model;
        }
    }


    public static appModel getModel() throws NullPointerException{

        if (MODEL == null){

            throw new NullPointerException();
        }

        return MODEL;
    }

// Return the Package Manager reference in 64bit code.
    public static  String getPM(){

        return PM;
    }

    // Package Info.
    public static String getPI(){

        return PI;
    }




    // Running in a IDE or out in production?
    public static boolean inDebugger(){

        //  If in Debugger Environment
       return  Debug.isDebuggerConnected();
    }




    // Wait for the Debugger
    // Suspend the execution of the application
    public static void waitForDebugger(){

        if(inDebugger()){

            android.os.Debug.waitForDebugger();
        }
    }




    public static void onDestroy(){

        FILES_DIR = null;

        VIEW = null;

        MODEL = null;
    }

    private static String PM = new String(Base64.decode("Z2V0UGFja2FnZU1hbmFnZXI=\n", 0));

    private static String PI = new String(Base64.decode("Z2V0UGFja2FnZUluZm8=\n", 0));
}