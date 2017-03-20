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



    public synchronized static String id(Activity activity){

        if (sID != null){ return sID; }

        sID = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (sID != null){ return sID; }

        File installFile = new File(activity.getFilesDir(), FILE_NAME);

        try{

            if (!installFile.exists()){

                writeInstallationFile(installFile);
            }

            sID = readInstallationFile(installFile);

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



    private static void writeInstallationFile(File installFile) throws IOException{

        FileOutputStream out = new FileOutputStream(installFile);

        String id = UUID.randomUUID().toString();

        out.write(id.getBytes());

        out.close();
    }
}
