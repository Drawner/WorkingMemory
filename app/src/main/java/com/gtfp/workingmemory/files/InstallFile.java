package com.gtfp.workingmemory.files;

import android.app.Activity;
import android.provider.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
/**
 * Created by Drawn on 3/17/2017.
 */

public class InstallFile{

    private static final String FILE_NAME = ".install";

    private static String sID = null;

    private static boolean mJustInstalled = false;




    public synchronized static String id(Activity activity){

        if (sID != null){ return sID; }

        File installFile = new File(activity.getFilesDir(), FILE_NAME);

        try{

            if (!installFile.exists()){

                mJustInstalled = true;

                sID =  writeInstallationFile(installFile, activity);
            }else{

                sID = readInstallationFile(installFile);
            }

        }catch (Exception e){

            sID = "";
        }

        return sID;
    }




    private static String readInstallationFile(File installFile) throws IOException{

        RandomAccessFile file = new RandomAccessFile(installFile, "r");

        byte[] bytes = new byte[(int) file.length()];

        file.readFully(bytes);

        file.close();

        return new String(bytes);
    }




    private static String writeInstallationFile(File installFile, Activity activity) throws IOException{

        FileOutputStream out = new FileOutputStream(installFile);

        String id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (id == null){

            id = UUID.randomUUID().toString();
        }

        out.write(id.getBytes());

        out.close();

        return id;
    }




    public static boolean justInstalled(){

        return mJustInstalled;
    }
}
