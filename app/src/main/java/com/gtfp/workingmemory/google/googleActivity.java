package com.gtfp.workingmemory.google;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Drawn on 2016-01-22.
 */
public class googleActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, googleAPI.crud, googleAPI.ApiClientTask {

    public static final String TAG = App.getPackageName();

    private Intent mIntent;

    private File mSyncFile;

    private String mGoogleFile;

    private static final int REQUEST_PERMISSION = 61125;

    private static final String[] PERMS = {Manifest.permission.INTERNET,
            Manifest.permission.MANAGE_DOCUMENTS};

    // If activity restart, must preserve the resolving error status.
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private static final String STATE_IN_PERMISSION = "inPermission";

    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    // Request code to use when launching the resolution activity
    public static final int REQUEST_RESOLVE_ERROR = 1111;

    // Access the GoogleAPI object
    private googleAPI mGoogleAPI;

    private GoogleApiClient mGoogleApiClient;

    private static final String MIME_TYPE_PLAINTEXT = "text/plain";

    /*
 * A flag indicating that an error is being resolved and prevents  from starting further intents.
 */
    private boolean mResolvingError = false;

    // A flag indicating this activity has been granted permission to is the Google Play Services.
    private boolean mHasPermission = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            mHasPermission = savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);

            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        }

//        if ( hasAllPermissions(getDesiredPermissions())) {
//
        mGoogleAPI = new googleAPI(this);

        mGoogleApiClient = mGoogleAPI.addGoogleDriveAPI()
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .setResultCallback(this)
                .build();
//
//        }else if (!mHasPermission) {
//
//            mHasPermission=true;
//
//            ActivityCompat
//                    .requestPermissions(this,
//                            netPermissions(getDesiredPermissions()),
//                            REQUEST_PERMISSION);
//        }

        //TODO Should this be retrieved in savedInstanceState??
        mIntent = getIntent();

        String syncFile = mIntent.getStringExtra("filename");

        // If no file is supplied.
        if (syncFile == null || syncFile.isEmpty()) {
            syncFile = "syncFile.csv";
        }

        mSyncFile = new File(syncFile);

        mGoogleFile = App.getLabel() + File.separator + mSyncFile.getName();
    }


    @Override
    protected void onStart() {

        super.onStart();

        if (!mResolvingError) {

            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onStop() {

        mGoogleApiClient.disconnect();

        super.onStop();
    }


    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onResume() {

        super.onResume();

        if (mGoogleApiClient != null) {

            if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }


    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {

        if (mGoogleApiClient != null) {

            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        loadFile();
    }


    @Override
    public void onConnectionSuspended(int cause) {

        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (mResolvingError) {
            // Already attempting to resolve an error.

        } else if (result.hasResolution()) {

            try {

                mResolvingError = true;

                // account picker dialog will appear
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);

            } catch (IntentSender.SendIntentException ex) {

                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {

            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());

            mResolvingError = true;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {

        super.onActivityResult(requestCode, resultCode, result);

        if (requestCode == REQUEST_RESOLVE_ERROR) {

            mResolvingError = false;

            if (resultCode == RESULT_OK) {

                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {

                    mGoogleApiClient.connect();
                }
            } else {

                finish();
            }
        }
    }


    void loadFile() {

//        String filename = mSyncFile.getName();
//
//        Filter filter = Filters.and(
//                Filters.eq(SearchableField.TRASHED, false)
//                , Filters.eq(SearchableField.TITLE, filename));
////                 ,Filters.in(SearchableField.PARENTS, Drive.DriveApi.getRootFolder(mGoogleAPI.getGoogleApiClient()).getDriveId())
//
//        // Create a query for a specific filename in Drive.
//        Query query = new Query.Builder()
//                .addFilter(filter)
//                .build();

//        mGoogleAPI.queryDrive(query);

        mGoogleAPI.getFile(mGoogleFile, MIME_TYPE_PLAINTEXT, mOnQueryResult);
    }

    ResultCallback<DriveApi.MetadataBufferResult> mOnQueryResult
            = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {

            if (result == null) {

                return;
            }

            if (!result.getStatus().isSuccess()) {

                result.release();

                // Subfolder may not exist.
                googleActivity.this.createFile();

                return;
            }

            MetadataBuffer mdb = result.getMetadataBuffer();

            if (mdb == null) {

                return;
            }

            if (mdb.getCount() == 0) {

                mdb.release();

                // File may not be found
                googleActivity.this.createFile();

                return;
            }

            for (Metadata md : mdb) {

                if (md == null || !md.isDataValid()) {

                    continue;
                }

                DriveId id = md.getDriveId();

                mGoogleAPI.fetchDriveFile(id, googleActivity.this);

                String title = md.getTitle();

                String alternateLink = md.getAlternateLink();

                //md.get.....();
            }

            mdb.release();
        }
    };


    public void createFile() {

        // Perform I/O off the UI thread.
        new Thread() {

            @Override
            public void run() {

                // create new contents resource
                mGoogleAPI.createFile(mGoogleFile, "text/plain");
            }
        }.start();
    }

    @Override
    public void onQuery(DriveApi.MetadataBufferResult result) {

        if (result == null) {

            return;
        }

        if (!result.getStatus().isSuccess()) {

            result.release();

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
            }
        }
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

//                    writer.write("Hello World!");
//                    writer.close();

        } catch (IOException e) {

            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public void onFolderCreated(DriveFolder.DriveFolderResult result) {

        if (!result.getStatus().isSuccess()) {

            showMessage("Error while trying to create the folder");
            return;
        }
        showMessage("Created a folder: " + result.getDriveFolder().getDriveId());
    }


    @Override
    public void onFileCreated(DriveFolder.DriveFileResult result) {

        if (!result.getStatus().isSuccess()) {

            showMessage("Error while trying to create the file");
            return;
        }
        showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
    }


    @Override
    public String onExecute(DriveId... params) {

        if(params[0] == null) return null;

        String contents = null;

        DriveFile file = params[0].asDriveFile();

        DriveApi.DriveContentsResult driveContentsResult =
                file.open(mGoogleAPI.getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();

        if (!driveContentsResult.getStatus().isSuccess()) {

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

        if (result == null) {

            showMessage("Error while reading from the file");
            return;
        }
        showMessage("File contents: " + result);
    }


    private boolean hasAllPermissions(String[] perms) {

        for (String perm : perms) {

            if (!hasPermission(perm)) {

                return (false);
            }
        }
        return true;
    }


    private boolean hasPermission(String perm) {

        return (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED);
    }


    private String[] getDesiredPermissions() {

        return PERMS;
    }


    private String[] netPermissions(String[] wanted) {

        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {

            if (!hasPermission(perm)) {

                result.add(perm);
            }
        }
        return (result.toArray(new String[result.size()]));
    }


    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {

        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();

        // Pass the error that should be displayed
        Bundle args = new Bundle();

        args.putInt(DIALOG_ERROR, errorCode);

        dialogFragment.setArguments(args);

        dialogFragment.show(getFragmentManager(), "errordialog");
    }


    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {

        mResolvingError = false;
    }


    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {

        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);

            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {

            ((googleActivity) getActivity()).onDialogDismissed();
        }
    }


    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIntent = null;

        mGoogleAPI.onDestroy();

        mGoogleAPI = null;

        mGoogleApiClient = null;
    }
}
