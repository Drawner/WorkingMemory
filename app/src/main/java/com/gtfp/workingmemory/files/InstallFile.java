package com.gtfp.workingmemory.files;

import android.app.Activity;
import android.provider.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;


/**
 * Copyright (C) 2017  Andrious Solutions Ltd.
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
