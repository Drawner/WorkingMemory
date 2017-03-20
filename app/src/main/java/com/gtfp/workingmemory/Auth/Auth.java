package com.gtfp.workingmemory.Auth;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by Drawn on 9/26/2016.
 */

public class Auth{

    private static FirebaseAuth mAuth;

    private static FirebaseAuth.AuthStateListener mAuthListener;

    private static Task<AuthResult> mAuthResult;

    private static boolean mSignedIn;

    private static FirebaseUser mUser;

    private static String mUserId;

    private static String mToken;

    private static String mProviderId;

    private static String mUserName;

    private static String mUserEmail;

    private static Uri mPhotoUrl;

    public static void onCreate(){

        onCreate(null, null);
    }

    public static void onCreate(FirebaseAuth auth){

        onCreate(auth, null);
    }

    public static void onCreate(FirebaseAuth.AuthStateListener listener){

        onCreate(null, listener);
    }

    public static void onCreate(FirebaseAuth auth, FirebaseAuth.AuthStateListener listener){

        if (mAuth == null){

            if (auth == null){

                mAuth = FirebaseAuth.getInstance();
            }else{

                mAuth = auth;
            }
        }

        if (listener != null){

            // Just in case it's there.
            mAuth.removeAuthStateListener(listener);

            mAuth.addAuthStateListener(listener);
        }

        // Add its own listener
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){

                    // User is signed in
                    mSignedIn = true;

//                        if (mAuthResult != null){
//
//                            FirebaseUser authUser = mAuthResult.getResult().getUser();
//                        }

//                        Task<GetTokenResult> tokenObj = user.getToken(true);
//
//                        tokenObj.addOnCompleteListener(new OnCompleteListener<GetTokenResult>(){
//                            @Override
//                            public  void onComplete (Task<GetTokenResult> task){
//
//                                String token = task.getResult().getToken();
//
//                                if (mToken == null || !mToken.equals(token)){
//
//                                    mToken = token;
//                                }
//                            }
//                        });

                    if (mUserId == null || !mUserId.equals(user.getUid())){

                        mUser = user;

                        mUserId = user.getUid();
                    }
                }else{
                    // User is signed out

                    mSignedIn = false;
                }
            }
        };
    }

    public static void onStart(Activity activity, OnFailureListener listener){

        if (mAuth != null && mAuth.getCurrentUser() == null){

            Auth.signIn(activity, listener);
        }else{

            if (mAuthListener != null){

                // Just in case it's there.
                mAuth.removeAuthStateListener(mAuthListener);

                mAuth.addAuthStateListener(mAuthListener);
            }
        }
    }


    public static void onStart(Activity activity, OnSuccessListener<AuthResult> listener){

        if (mAuth != null && mAuth.getCurrentUser() == null){

            Auth.signIn(activity, listener);
        }else{

            if (mAuthListener != null){

                // Just in case it's there.
                mAuth.removeAuthStateListener(mAuthListener);

                mAuth.addAuthStateListener(mAuthListener);
            }
        }
    }

    public static void onResume(Activity activity){

    }

    public static void onStop(){

        if (mAuthListener != null){

            mAuth.removeAuthStateListener(mAuthListener);
        }
    }




    public static void signIn(Activity activity){

        mAuthResult = mAuth.signInAnonymously()
                .addOnCompleteListener(activity, getCompleteListener());
    }




    public static void signIn(Activity activity, OnSuccessListener<AuthResult> listener){

        mAuthResult = mAuth.signInAnonymously()
                .addOnCompleteListener(activity, getCompleteListener())
                .addOnSuccessListener(activity, listener);
    }




    public static void signIn(Activity activity, OnFailureListener listener){

        mAuthResult = mAuth.signInAnonymously()
                .addOnCompleteListener(activity, getCompleteListener())
                .addOnFailureListener(activity, listener);
    }




    private static OnCompleteListener<AuthResult> getCompleteListener(){

        return new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task){

                if (task.isSuccessful()){

                    mSignedIn = true;

                }else{

                    mSignedIn = false;
                }
            }
        };
    }




    public static boolean isSignedIn(){

        return mSignedIn || (mAuth != null && mAuth.getCurrentUser() != null);
    }




    public static void signOut(){

        mAuth.signOut();
    }




    public static void linkAccount(){

    }




    public static String getUid(){

        if (mUserId == null){

            FirebaseUser user = getUser();

            if (user != null){

                mUserId = user.getUid();
            }
        }
        return mUserId;
    }




    public static FirebaseUser getUser(){

        if (mUser == null && mAuth != null){

            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null){

                mUser = user;
            }
        }
        return mUser;
    }




    public static String userProfile(String type){

        return userProfile(mUser, type);
    }




    public static String userProfile(FirebaseUser user, String type){

        if (user == null){ return ""; }

        if (type == null){ return "";}

        String info = "";

        String holder = "";

        // Always return 'the last' available item.
        for (UserInfo profile : user.getProviderData()){

            switch (type.trim().toLowerCase()){
                case "provider":

                    holder = profile.getProviderId();

                    break;
                case "userid":

                    holder = profile.getUid();

                    break;
                case "name":

                    holder = profile.getDisplayName();

                    break;
                case "email":

                    holder = profile.getEmail();

                    break;
                case "photo":

                    try{

                        holder = profile.getPhotoUrl().toString();

                    }catch (Exception ex){

                        holder = "";
                    }

                    break;
                default:

                    holder = "";
            }

            if (holder != null && !holder.isEmpty()){

                info = holder;
            }
        }
        return info;
    }


    private static void userProfile(){

        if (mUser == null){ return; }

        if (mProviderId != null){ return; }

        for (UserInfo profile : mUser.getProviderData()){

            // Id of the provider (ex: google.com)
            mProviderId = profile.getProviderId();

            // Name, email address, and profile photo Url
            mUserName = profile.getDisplayName();

            mUserEmail = profile.getEmail();

            mPhotoUrl = profile.getPhotoUrl();
        }
    }

    public static String userProvider(){

        userProfile();

        return mProviderId;
    }

    public static String userEmail(){

        userProfile();

        return mUserEmail;
    }

    public static String userName(){

        userProfile();

        return mUserName;
    }

    public static Uri userPhoto(){

        userProfile();

        return mPhotoUrl;
    }


    public static void onDestroy(){

        mAuthResult = null;

        onStop();

        mAuthListener = null;

//        signOut();

        mAuth = null;

        mUser = null;

        mUserId = null;
    }
}
