package com.gtfp.workingmemory.google;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import com.gtfp.workingmemory.app.App;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Drawn on 2016-01-22.
 */
public class googleAPI {

    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    private static final String MIME_TYPE_PLAINTEXT = "text/plain";

    /* Object builds up the Google Api Client    */
    private GoogleApiClient.Builder mBuilder;

    private GoogleApiClient mGoogleApiClient;

    private googleAPI.crud mCRUDapi;

    private File mFile;

    private DriveFolder mAppFolder;


    public googleAPI(Context context) {

        mBuilder = new GoogleApiClient.Builder(context);
    }


    public googleAPI addGoogleDriveAPI() {

        // Access to files and the 'App Folder'
        mBuilder.addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER);

        return this;
    }


    public googleAPI addConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callObj) {

        mBuilder.addConnectionCallbacks(callObj);

        return this;
    }


    public googleAPI addOnConnectionFailedListener(
            GoogleApiClient.OnConnectionFailedListener listenerObj) {

        mBuilder.addOnConnectionFailedListener(listenerObj);

        return this;
    }


    public GoogleApiClient build() {

        if (mGoogleApiClient == null)
             mGoogleApiClient = mBuilder.build();

        return mGoogleApiClient;
    }


    public googleAPI setResultCallback(googleAPI.crud resultCallback) {

        mCRUDapi = resultCallback;

        return this;
    }


    public GoogleApiClient getGoogleApiClient() {

        return mGoogleApiClient;
    }


    public GoogleApiClient buildGoogleApiClient() {

        return  mBuilder.build();
    }


    public GoogleApiClient.Builder getBuilder(){

        return mBuilder;
    }


    public interface crud {

        void onQuery(DriveApi.MetadataBufferResult result);

        void onCreateFile(OutputStream outputStream);

        void onFolderCreated(DriveFolder.DriveFolderResult result);

        void onFileCreated(DriveFolder.DriveFileResult result);
    }


    public void queryDrive(Query query) {

        // Invoke the query asynchronously with a callback method
        Drive.DriveApi.query(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult result) {

                        if (mCRUDapi != null) {
                            mCRUDapi.onQuery(result);
                        }
                    }
                });
    }


    public void queryDrive(Query query, ResultCallback<DriveApi.MetadataBufferResult> onQueryResult) {

        // Invoke the query asynchronously with a callback method
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(onQueryResult);
     }


    //TODO Must test for valid parameters
    public void createFile(String name, String type) {

        final String fileName = name;

        final String fileType = type;

        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {

                        if (!result.getStatus().isSuccess()) {

                            return;
                        }

                        final DriveContents driveContents = result.getDriveContents();

                        // Perform I/O off the UI thread.
                        new Thread() {

                            @Override
                            public void run() {

                                if (mCRUDapi != null) {
                                    // write content to DriveContents
                                    mCRUDapi.onCreateFile(driveContents.getOutputStream());
                                }

                                createFile(fileName, fileType, driveContents);
                            }
                        }.start();


                    }
                });
    }


    public void createFolder(String title) {

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(title)
                .build();

        // create a file in the app folder
        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                .createFolder(mGoogleApiClient, changeSet)
                .setResultCallback(new
                                           ResultCallback<DriveFolder.DriveFolderResult>() {
                                               @Override
                                               public void onResult(
                                                       DriveFolder.DriveFolderResult result) {

                                                   if (mCRUDapi != null) {

                                                       mCRUDapi.onFolderCreated(result);
                                                   }
                                               }
                                           });

    }


    protected void createFile(String title, String fileType, DriveContents driveContents) {

        File file = new File(title);

        String fileName = file.getName();

        DriveId parentId = createFolder(file);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .setMimeType(fileType)
                .build();

        // create under that parent id.
        parentId.asDriveFolder()
                .createFile(mGoogleApiClient, changeSet, driveContents)
                .setResultCallback(new
                                           ResultCallback<DriveFolder.DriveFileResult>() {
                                               @Override
                                               public void onResult(
                                                       DriveFolder.DriveFileResult result) {

                                                   if (mCRUDapi != null) {

                                                       mCRUDapi.onFileCreated(result);
                                                   }
                                               }
                                           });

    }


    // Recursive
    // Creates the folder if it doesn't exist.
    public DriveId createFolder(File file) {

        DriveId folderID = null;

        File path = file.getParentFile();

        if (path == null) {

            folderID = getAppFolder().getDriveId();
        } else {

            folderID = createFolder(path);

            folderID = createFolderID(folderID, path.getName());

            if (folderID == null) {
                folderID = getAppFolder().getDriveId();
            }
        }

        return folderID;
    }


    public void getFile(String title, String type,
            ResultCallback<DriveApi.MetadataBufferResult> onQueryResult) {

        final File file = new File(title);

        final String fileName = file.getName();

        final String fileType = type;

        final ResultCallback<DriveApi.MetadataBufferResult> resultOfQuery = onQueryResult;

        final GoogleApiClient googleAPI = mGoogleApiClient;

        // Perform I/O off the UI thread.
        new Thread() {

            @Override
            public void run() {

                // Returns null if the folder is not found.
                DriveId parentId = getFolderID(file, googleAPI);

                // This returns the id of the file itself. We don't found to do that.
//                if (parentId != null)
//                    parentId = getID(parentId, fileName, fileType, googleAPI);

                // Let the Callback handle a 'not found' file.
//                if (parentId == null) {
//                    return;
//                }

                Filter filter = Filters.and(
                        Filters.in(SearchableField.PARENTS, parentId)
                        , Filters.eq(SearchableField.TRASHED, false)
                        , Filters.eq(SearchableField.TITLE, fileName));

                // Create a query for a specific filename in Drive.
                final Query query = new Query.Builder()
                        .addFilter(filter)
                        .build();

                Drive.DriveApi.query(googleAPI, query)
                        .setResultCallback(resultOfQuery);
            }
        }.start();
    }


    // Recursive
    // Creates the folder if it doesn't exist.
    // Return null if not found.
    public DriveId getFolderID(File file, GoogleApiClient googleAPI) {

        DriveId id = null;

        File path = file.getParentFile();

        if (path == null) {

            id = getAppFolder(googleAPI).getDriveId();
        } else {

            id = getFolderID(path, googleAPI);

            if (id != null) {
                id = getFolderID(id, path.getName(), googleAPI);
            }

        }

        return id;
    }


    // Returns null if not found.
    public DriveId getID(DriveId parentId, String title, String type, GoogleApiClient googleAPI) {

        DriveId id = null;

        try {

            Filter filters = Filters.and(
                    Filters.in(SearchableField.PARENTS, parentId)
                    , Filters.eq(SearchableField.TITLE, title)
                    , Filters.eq(SearchableField.MIME_TYPE,
                            type));

            Query qry = new Query.Builder().addFilter(filters).build();

            DriveApi.MetadataBufferResult queryResult = Drive.DriveApi.query(googleAPI, qry)
                    .await();

            if (queryResult.getStatus().isSuccess()) {

                MetadataBuffer mdb = queryResult.getMetadataBuffer();

                if (mdb.getCount() > 0) {

                    id = mdb.get(0).getDriveId();
                }

                mdb.release();
            }

        } catch (Exception ex) {

            Log.e(App.getPackageName(), ex.getMessage());
        }
        return id;
    }


   public void fetchDriveFile(DriveId id, final ApiClientTask obj) {

       // Build a separate google client.
       GoogleApiClient client = buildGoogleApiClient();

       RetrieveDriveFileContentsAsyncTask callBackAsyncTask = new RetrieveDriveFileContentsAsyncTask(client, obj);

       callBackAsyncTask.execute(id);

//       Drive.DriveApi.fetchDriveId(mGoogleApiClient, id.toString())
//               .setResultCallback(new ResultCallback<DriveApi.DriveIdResult>() {
//                   @Override
//                   public void onResult(DriveApi.DriveIdResult result) {
//
//                       // Build a separate google client.
//                       GoogleApiClient client = buildGoogleApiClient();
//
//                       RetrieveDriveFileContentsAsyncTask callBackAsyncTask = new RetrieveDriveFileContentsAsyncTask(client, obj);
//
//                       callBackAsyncTask.execute(result.getDriveId());
//                   }
//               });
   }


    public DriveId createFolderID(DriveId parentId, String title) {

        DriveId id = getFolderID(parentId, title);

        if (id == null) {

            DriveFolder folder = createFolder(parentId, title);

            if (folder != null) {

                DriveResource.MetadataResult result = folder.getMetadata(mGoogleApiClient)
                        .await();

                if (result.getStatus().isSuccess()) {

                    id = result.getMetadata().getDriveId();
                }
            }
        }
        return id;
    }


    public DriveId getFolderID(DriveId parentId, String title) {

        return getFolderID(parentId, title, mGoogleApiClient);
    }


    public DriveId getFolderID(DriveId parentId, String title, GoogleApiClient googleAPI) {

        Query qry = getFolderQuery(parentId, title);

        return getFolderID(qry, googleAPI);
    }


    public Query getFolderQuery(DriveId parentId, String title) {

        Query qry = null;

        try {

            Filter filters = Filters.and(
                    Filters.eq(SearchableField.TITLE, title)
                    , Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE_FOLDER)
                    , Filters.in(SearchableField.PARENTS, parentId));

            qry = new Query.Builder().addFilter(filters).build();

        } catch (Exception ex) {

            Log.e(App.getPackageName(), ex.getMessage());
        }
        return qry;
    }


    public DriveId getFolderID(Query qry, GoogleApiClient googleAPI) {

        DriveId dId = null;

        MetadataBuffer mdb = null;

        DriveApi.MetadataBufferResult queryResult;

        queryResult = Drive.DriveApi.query(googleAPI, qry).await();

        if (queryResult.getStatus().isSuccess()) {

            mdb = queryResult.getMetadataBuffer();

            if (mdb.getCount() > 0) {

                dId = mdb.get(0).getDriveId();
            }

            mdb.release();
        }

        return dId;
    }


    public void getFolderID(DriveId parentId, String title,
            ResultCallback<DriveApi.MetadataBufferResult> onQueryResult) {

        getFolderID(parentId, title, mGoogleApiClient, onQueryResult);
    }


    public void getFolderID(DriveId parentId, String title, GoogleApiClient googleAPI,
            ResultCallback<DriveApi.MetadataBufferResult> onQueryResult) {

        Query qry = getFolderQuery(parentId, title);

        getFolderID(qry, googleAPI, onQueryResult);
    }


    public void getFolderID(Query qry, GoogleApiClient googleAPI,
            ResultCallback<DriveApi.MetadataBufferResult> onQueryResult) {

        Drive.DriveApi.query(googleAPI, qry).setResultCallback(onQueryResult);
    }


    public DriveFolder createFolder(DriveId parentId, String title) {

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(title)
                .build();

        DriveFolder.DriveFolderResult folderResult = parentId.asDriveFolder()
                .createFolder(mGoogleApiClient, changeSet).await();

        return folderResult.getStatus().isSuccess() ? folderResult.getDriveFolder() : null;
    }


    public DriveFolder getAppFolder() {

        return getAppFolder(mGoogleApiClient);
    }


    // Return the root directory if no access to the 'app folder'.
    public DriveFolder getAppFolder(GoogleApiClient googleApiClient) {

        if (mAppFolder == null) {

            mAppFolder = Drive.DriveApi.getAppFolder(googleApiClient);

            // Return the root directory if no access to the 'app folder'.
            if (mAppFolder == null) {
                mAppFolder = Drive.DriveApi.getRootFolder(googleApiClient);
            }
        }
        return mAppFolder;
    }


    public void onDestroy() {

        mBuilder = null;

        mGoogleApiClient = null;

        mCRUDapi = null;

        mFile = null;

        mAppFolder = null;
    }


    public  RetrieveDriveFileContentsAsyncTask getApiClientTask(ApiClientTask obj) {

        // Build a separate google client.
        GoogleApiClient client = buildGoogleApiClient();

        return new RetrieveDriveFileContentsAsyncTask(client, obj);
    }


    interface ApiClientTask{

        String onExecute(DriveId... params);

        void onPostExecute(String result);
    }


    final private class RetrieveDriveFileContentsAsyncTask  extends ApiClientAsyncTask<DriveId, Boolean, String> {

        ApiClientTask mDoInBackground;

         RetrieveDriveFileContentsAsyncTask(GoogleApiClient  apiClient, ApiClientTask obj) {
            super(apiClient);

            mDoInBackground = obj;
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {

           return mDoInBackground.onExecute(params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mDoInBackground.onPostExecute(result);
        }
    }


    /**
     * An AsyncTask that maintains a connected client.
     */
    public abstract class ApiClientAsyncTask<Params, Progress, Result>
            extends AsyncTask<Params, Progress, Result> {

        private GoogleApiClient mClient;


        ApiClientAsyncTask(GoogleApiClient  apiClient) {

            mClient = apiClient;
        }


        @Override
        protected final Result doInBackground(Params... params) {

            final CountDownLatch latch = new CountDownLatch(1);

            boolean notConnected =  !mClient.isConnected();

            if (notConnected) {

                mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }

                    @Override
                    public void onConnected(Bundle arg0) {
                        latch.countDown();
                    }
                });

                mClient.registerConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult arg0) {
                                latch.countDown();
                            }
                        });

                mClient.connect();
            }

            try {

                latch.await();

            } catch (InterruptedException e) {

                return null;
            }

            if (!mClient.isConnected()) {

                return null;
            }

            try {

                return doInBackgroundConnected(params);

            } finally {

                if (notConnected)
                    mClient.disconnect();
            }
        }

        /**
         * Override this method to perform a computation on a background thread, while the client is
         * connected.
         */
        protected abstract  Result doInBackgroundConnected(Params... params);

        /**
         * Gets the GoogleApliClient owned by this async task.
         */
        protected GoogleApiClient getGoogleApiClient() {
            return mClient;
        }
    }
}

