package com.gtfp.workingmemory.google;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import com.gtfp.workingmemory.app.App;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Drawn on 2016-03-15.
 */
public class googleService extends Service  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, googleAPI.crud, googleAPI.ApiClientTask {

    public static final String TAG = App.getPackageName();

    // Request code to use when launching the resolution activity
    public static final int REQUEST_RESOLVE_ERROR = 1111;

    public static final String SYNC_FILENAME  = "filename";

    private static final String MIME_TYPE_PLAINTEXT = "text/plain";

    // A flag indicating that an error is being resolved and prevents  from starting further intents.
    private boolean mResolvingError = false;

    private boolean mLoading = false;

    private boolean isPlaying=false;

    // Access the GoogleAPI object
    private googleAPI mGoogleAPI;

    private GoogleApiClient mGoogleApiClient;

    private File mSyncFile;

    private String mGoogleFile;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String syncFile = intent.getStringExtra(SYNC_FILENAME);

        // If no file is supplied.
        if (syncFile == null || syncFile.isEmpty()) {

            syncFile = "syncFile.csv";
        }

        mSyncFile = new File(syncFile);

        mGoogleFile = App.getLabel() + File.separator + mSyncFile.getName();

        startGoogleService();

        return(START_NOT_STICKY);
    }


    @Override
    public void onDestroy() {

        destroyGoogleService();
    }


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        if (!mLoading) {

            mLoading = true;

            loadFile();
        }
    }


    @Override
    public void onConnectionSuspended(int cause) {

        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {

        stopGoogleService();

        if (mResolvingError) {
            // Already attempting to resolve an error.

        } else if (result.hasResolution()) {


        } else {

            mResolvingError = true;
        }
    }


    void loadFile() {

        mGoogleAPI.getFile(mGoogleFile, MIME_TYPE_PLAINTEXT, mOnQueryResult);
    }

    ResultCallback<DriveApi.MetadataBufferResult> mOnQueryResult
            = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {

            if (result == null) {

                stopGoogleService();
                return;
            }

            if (!result.getStatus().isSuccess()) {

                result.release();

                // Subfolder may not exist.
                googleService.this.createFile();
                return;
            }

            MetadataBuffer mdb = result.getMetadataBuffer();

            if (mdb == null) {

                stopGoogleService();
                return;
            }

            if (mdb.getCount() == 0) {

                mdb.release();

                // File may not be found
                googleService.this.createFile();
                return;
            }

            for (Metadata md : mdb) {

                if (md == null || !md.isDataValid()) {

                    continue;
                }

                DriveId id = md.getDriveId();

                mGoogleAPI.fetchDriveFile(id, googleService.this);

                String title = md.getTitle();

                String alternateLink = md.getAlternateLink();

                //md.get.....();
            }

            mdb.release();
        }
    };


    @Override
    public void onQuery(DriveApi.MetadataBufferResult result) {

        if (result == null) {

            stopGoogleService();
            return;
        }

        if (!result.getStatus().isSuccess()) {

            result.release();
            stopGoogleService();
            return;
        }

        MetadataBuffer mdb = null;

        try {

            mdb = result.getMetadataBuffer();

            if (mdb == null) {

                return;
            }

            if (mdb.getCount() == 0) {

                // create new contents resource
                mGoogleAPI.createFile(mGoogleFile, "text/plain");
            } else {

                for (Metadata md : mdb) {

                    if (md == null || !md.isDataValid()) {

                        continue;
                    }

                    md.getTitle();

                    md.getDriveId();

                    md.getAlternateLink();

                    //md.get.....();
                }
            }

        } catch (Exception ex) {

        } finally {

            if (mdb != null) {

                mdb.release();

                stopGoogleService();
            }
        }
    }


    public void createFile() {

        // Perform I/O off the UI thread.
        new Thread() {

            @Override
            public void run() {

                // create new contents resource
                mGoogleAPI.createFile(mGoogleFile, MIME_TYPE_PLAINTEXT);
            }
        }.start();
    }


    @Override
    public void onFileCreated(DriveFolder.DriveFileResult result) {

        if (result.getStatus().isSuccess()) {
            //"Created a file with content: " + result.getDriveFile().getDriveId()
        }else {

            //"Error while trying to create the file"
        }
        stopGoogleService();
    }


    @Override
    public void onFolderCreated(DriveFolder.DriveFolderResult result) {

        if (result.getStatus().isSuccess()) {

            //"Created a folder: " + result.getDriveFolder().getDriveId()
        } else {

            //"Error while trying to create the folder"
        }
        stopGoogleService();
    }


    @Override
    public void onCreateFile(OutputStream output) {

        byte[] buffer = new byte[(int) mSyncFile.length()];

        try {

            FileInputStream input = new FileInputStream(mSyncFile);

            input.read(buffer);

            try {

                output.write(buffer);
            } finally {

                input.close();
            }
        } catch (IOException e) {

            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public String onExecute(DriveId... params) {

        if(params[0] == null){

            // Will go to onPostExecute()
            return null;
        }

        String contents = null;

        DriveFile file = params[0].asDriveFile();

        DriveApi.DriveContentsResult driveContentsResult =
                file.open(mGoogleAPI.getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();

        if (!driveContentsResult.getStatus().isSuccess()) {

            // Will go to onPostExecute()
            return null;
        }

        DriveContents driveContents = driveContentsResult.getDriveContents();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(driveContents.getInputStream()));

        StringBuilder builder = new StringBuilder();

        String line;

        try {

            while ((line = reader.readLine()) != null) {

                builder.append(line);
            }
            contents = builder.toString();

        } catch (IOException ex) {

            Log.e(TAG, "IOException while reading from the stream", ex);
        }

        driveContents.discard(mGoogleAPI.getGoogleApiClient());

        return contents;
    }


    @Override
    public void onPostExecute(String result) {

        if (result != null) {

            //"File contents: " + result
        }else{
            // "Error while reading from the file"
            // return;
        }

        stopGoogleService();
    }



    private void startGoogleService(){

        mGoogleAPI = new googleAPI(this);

        mGoogleApiClient = mGoogleAPI.addGoogleDriveAPI()
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .setResultCallback(this)
                .build();

        if (mGoogleApiClient != null && !mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }


    private void stopGoogleService(){

        if (mGoogleApiClient != null ) {

            if(mGoogleApiClient.isConnected())
                    mGoogleApiClient.disconnect();

            mGoogleApiClient = null;

            stopSelf();
        }
    }

    private void destroyGoogleService(){

        mGoogleAPI.onDestroy();

        mGoogleAPI = null;

        stopGoogleService();
    }
}
