package com.gtfp.workingmemory.google;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.gtfp.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import java.util.List;
/**
 * Created by Drawn on 3/25/2017.
 */
public class SignInActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog mProgressDialog;

    private CallbackManager mFacebookCallbackManager;



    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_layout);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        SignInButton signInButton = (SignInButton) findViewById(R.id.google_button);

        signInButton.setSize(SignInButton.SIZE_STANDARD);

//        signInButton.setScopes(gso.getScopeArray());

        setGooglePlusButtonText(signInButton, "Sign in with Google");

        findViewById(R.id.google_button).setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){

                switch (v.getId()){
                    case R.id.google_button:

                        Intent signInIntent =
                                Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

                        startActivityForResult(signInIntent, RC_SIGN_IN);

                        break;
                }
            }
        });

        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginButton facebookButton = (LoginButton) findViewById(R.id.facebook_button);

        facebookButton
                .registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>(){

                    public void onSuccess(LoginResult loginResult){

                        authFireBase(loginResult.getAccessToken());

                        // Close this activity.
                        finish();
                    }



                    public void onCancel(){

                    }



                    public void onError(FacebookException error){

                    }
                });
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN){

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()){

                // Google Sign In was successful, authenticate with FireBase
                GoogleSignInAccount account = result.getSignInAccount();

                authFireBase(account);

                // Close this activity.
//                finish();
            }else{

            }
        }else{

            //Handle Facebook result
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }



    private void authFireBase(GoogleSignInAccount acct){

        if (acct == null){ return; }

//        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        signIn(credential);
    }



    private void authFireBase(AccessToken token){

        if (token == null){ return; }

//        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        signIn(credential);
    }



    private void signIn(AuthCredential credential){

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task){

                        if (task.isSuccessful()){

                            AuthResult result = task.getResult();

                            FirebaseUser user = result.getUser();

                            List<String> providers = user.getProviders();

                            String id = user.getUid();

                            // User is signed in
                            finish();

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                        }else{

                            // User is not signed in

                            ErrorHandler.logError(task.getException());
//                            Log.w(TAG, "signInWithCredential", task.getException());
//                            Toast.makeText(this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }



    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText){

        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++){

            View v = signInButton.getChildAt(i);

            if (v instanceof TextView){

                TextView tv = (TextView) v;

                tv.setText(buttonText);

                return;
            }
        }
    }



    private void showProgressDialog(){

        if (mProgressDialog == null){

            mProgressDialog = new ProgressDialog(this);

            mProgressDialog.setMessage(getString(R.string.loading));

            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }



    private void hideProgressDialog(){

        if (mProgressDialog != null && mProgressDialog.isShowing()){

            mProgressDialog.hide();
        }
    }

    //    @Override
//    public void onStart(){
//
//        super.onStart();
//
//        OptionalPendingResult<GoogleSignInResult> opr =
//                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
//
//        if (opr.isDone()){
//            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
//            // and the GoogleSignInResult will be available instantly.
//
//            GoogleSignInResult result = opr.get();
//
//            handleSignInResult(result);
//        }else{
//            // If the user has not previously signed in on this device or the sign-in has expired,
//            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
//            // single sign-on will occur in this branch.
//            showProgressDialog();
//
//            opr.setResultCallback(new ResultCallback<GoogleSignInResult>(){
//
//                @Override
//                public void onResult(GoogleSignInResult googleSignInResult){
//
//                    hideProgressDialog();
//
//                    handleSignInResult(googleSignInResult);
//                }
//            });
//        }
//    }
//
//
//
//
//    private void handleSignInResult(GoogleSignInResult result){
//
//        if (result.isSuccess()){
//
//            // Signed in successfully, show authenticated UI.
//            GoogleSignInAccount acct = result.getSignInAccount();
//
//            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
//            //updateUI(true);
//            Intent intent = new Intent(SignInActivity.this, appController.class);
//
//            startActivity(intent);
//
//            String idToken = acct.getIdToken();
//
//            if (idToken != null){
//
//                //sendEmail(idToken);
//            }else{
//
//            }
//        }else{
//
//            // Signed out, show unauthenticated UI.
//            //updateUI(false);
//        }
//    }
}
