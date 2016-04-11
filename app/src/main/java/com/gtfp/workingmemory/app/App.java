package com.gtfp.workingmemory.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

/**
 * Created by Drawn on 2016-03-09.
 */
// A top-level Java class mimicking static class behavior
public final class App {

    private static int myStaticMember;

    private static String PACKAGE_NAME = "package.name.unknown";

    private static File FILES_DIR;

    private static String LABEL;

     private static appModel MODEL;



    private App () { // private constructor

        myStaticMember = 1;
    }


    public static String setPackageName(Activity activity) {

        if(PACKAGE_NAME == "package.name.unknown") PACKAGE_NAME = activity.getApplicationContext().getPackageName();

        return PACKAGE_NAME;
    }


    public static String getPackageName() {

        return PACKAGE_NAME;
    }


    public static File getFilesDir(Context context){

       if(FILES_DIR == null)  FILES_DIR = context.getFilesDir();

        return FILES_DIR;
    }


    public static File getFilesDir(){

        if(FILES_DIR == null){

            return new File(Environment.getExternalStorageDirectory(), "");
        }else{

           return FILES_DIR;
        }
    }


    public static String setLabel(Context pContext) {

        PackageManager lPackageManager = pContext.getPackageManager();

        ApplicationInfo lApplicationInfo = null;

        try {

            lApplicationInfo = lPackageManager.getApplicationInfo(pContext.getApplicationInfo().packageName, 0);

        } catch (final PackageManager.NameNotFoundException e) {
        }

        LABEL =  (String) (lApplicationInfo != null ? lPackageManager.getApplicationLabel(lApplicationInfo) : "Unknown");

        return LABEL;
    }


    public static String getLabel(Context pContext) {

        if (LABEL == null) LABEL = setLabel(pContext);

        return LABEL;
    }


    public static String getLabel(){

        if (LABEL == null) return "Unknown";

        return LABEL;
    }


    public static void setModel(appModel model){

           if (MODEL == null) MODEL = model;
    }


    public static appModel getModel() throws NullPointerException{

        if (MODEL == null) throw new NullPointerException();

        return MODEL;
    }

    public static void onDestroy(){

        FILES_DIR = null;

        MODEL = null;
    }
}