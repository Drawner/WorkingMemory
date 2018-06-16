package com.gtfp.workingmemory.google;



import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

import com.gtfp.workingmemory.R;

import android.content.Context;
import android.os.Bundle;


/**
 * Copyright (C) 2016  Greg T. F. Perry
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
 *
 * Use this class to login with google account using the OpenId oauth method.
 */
public class GoogleLogin_THROWOUT{

    private static final String TAG = GoogleLogin_THROWOUT.class.getName();

    private static final String SERVER_CLIENT_ID
            = "761477102089-qihk55871jg3ck3pqbff5c3jgeut2jk9.apps.googleusercontent.com";

    private GoogleApiClient mGoogleApiClient;

    private Context mContext;


    private GoogleLogin_THROWOUT(Context appContext){

        this.mContext = appContext;

        createGoogleClient();
    }

    /**
     * Performs a silent sign in and fetch a token.
     *
     * @param appContext Application context
     */
    public static void silent(Context appContext){

        new GoogleLogin_THROWOUT(appContext).silent();
    }


    private void createGoogleClient(){

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestIdToken(mContext.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener(){

                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult){

                        onSilentLoginFinished(null);
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks(){
                    @Override
                    public void onConnected(Bundle bundle){

                        onSilentLoginFinished(null);
                    }

                    @Override
                    public void onConnectionSuspended(int i){

                        onSilentLoginFinished(null);
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void silent(){

        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi
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

        if (signInAccount != null){

            String emailAddress = signInAccount.getEmail();

            String token = signInAccount.getIdToken();
        }
    }
}
