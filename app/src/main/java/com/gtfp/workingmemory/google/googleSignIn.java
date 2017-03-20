package com.gtfp.workingmemory.google;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Drawn on 11/27/2016.
 */

public class googleSignIn implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



    public googleSignIn(Context context){

        mContext = context;

        mGoogleAPI = new googleAPI(mContext);

        mGoogleApiClient = mGoogleAPI.addGoogleSignInAPI()
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void signIn(){

        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()){

            mGoogleApiClient.connect();
        }
    }


    public void signInSilent(){

        OptionalPendingResult<GoogleSignInResult>
                pendingResult = com.google.android.gms.auth.api.Auth.GoogleSignInApi
                .silentSignIn(mGoogleApiClient);

        if (pendingResult != null){

            if (pendingResult.isDone()){

                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                GoogleSignInResult signInResult = pendingResult.get();

                onSilentLoginFinished(signInResult);
            }else{

                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>(){
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult){

                        onSilentLoginFinished(googleSignInResult);
                    }
                });
            }
        }
    }

    private void onSilentLoginFinished(GoogleSignInResult signInResult){

        if (signInResult == null){ return; }

        GoogleSignInAccount signInAccount = signInResult.getSignInAccount();

//        if (signInAccount != null){
//
//            String emailAddress = signInAccount.getEmail();
//
//            String token = signInAccount.getIdToken();
//        }

        mGoogleAccount = signInAccount;
    }



    public googleSignIn addOnConnectionListener(GoogleApiClient.ConnectionCallbacks listener){

        mConnectionListener = listener;

        return this;
    }



    public googleSignIn addOnConnectionFailedListener(GoogleApiClient.OnConnectionFailedListener listener){

        mOnConnectionFailedListener = listener;

         return this;
    }



    @Override
    public void onConnected(Bundle connectionHint){

        if (mConnected){ return; }

        mConnected = true;

        if(mConnectionListener !=null){

            mConnectionListener.onConnected(connectionHint);

            return;
        }
    }

    @Override
    public void onConnectionSuspended(int cause){

        if(mConnectionListener !=null){

            mConnectionListener.onConnectionSuspended(cause);

            return;
        }

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){

        if(mOnConnectionFailedListener !=null){

            mOnConnectionFailedListener.onConnectionFailed(result);

            return;
        }

        if (mResolvingError){
            // Already attempting to resolve an error.

        }else if (result.hasResolution()){

            Intent i = new Intent(mContext, googleActivity.class);

            i.putExtra(googleActivity.CONNECTION_RESULT, result);

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mContext.startActivity(i);
        }else{

            mResolvingError = true;
        }
    }


    public GoogleSignInAccount googleAccount(){

        return mGoogleAccount;
    }


    public void onDestroy(){

        mContext = null;

        mGoogleAPI.onDestroy();

        mGoogleAPI = null;

        mGoogleApiClient = null;

        mGoogleAccount = null;
    }


    private Context mContext;

    private googleAPI mGoogleAPI;

    private GoogleApiClient mGoogleApiClient;

    private boolean mConnected = false;

    // A flag indicating that an error is being resolved and prevents  from starting further intents.
    private boolean mResolvingError = false;

    private GoogleSignInAccount mGoogleAccount;

    private GoogleApiClient.ConnectionCallbacks mConnectionListener;

    private GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener;

}
