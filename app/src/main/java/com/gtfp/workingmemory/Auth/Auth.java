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

import java.util.List;

/**
 * Created by Drawn on 9/26/2016.
 */

public class Auth{

    private static Activity mActivity;

    private static FirebaseAuth mAuth;

    private static Task<AuthResult> mAuthResult;

    private static FirebaseUser mUser;

    private static String mUserId;

    private static FirebaseUser mPrevUser;

    private static String mPrevUserId = "";

    private static String mToken;

    private static String mProviderId;

    private static String mUserName;

    private static String mUserEmail;

    private static Uri mPhotoUrl;

    // Add its own listener
    private static FirebaseAuth.AuthStateListener mAuthListener = new appAuthListener();


    public static void onCreate(Activity activity){

        mActivity = activity;

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

        // Add the authentication listener and invokes a login
        addAuthStateListener(listener);
    }



    public static void onStart(OnFailureListener listener){

        Auth.signIn(listener);

        Auth.onStart();
    }



    public static void onStart(OnSuccessListener<AuthResult> listener){

        Auth.signIn(listener);

        Auth.onStart();
    }



    public static void onStart(){

        if (mAuthListener != null){

            // Just in case it's there.
            mAuth.removeAuthStateListener(mAuthListener);

            mAuth.addAuthStateListener(mAuthListener);
        }
    }



    public static void onResume(Activity activity){

    }



    public static void onStop(){

        // mAuth may be null
        if (mAuthListener != null && mAuth != null){

            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



    public static void addAuthStateListener(FirebaseAuth.AuthStateListener listener){

        if (mAuth != null && listener != null){

            // Just in case it's there.
            mAuth.removeAuthStateListener(listener);

            mAuth.addAuthStateListener(listener);
        }
    }



    public static void removeAuthStateListener(FirebaseAuth.AuthStateListener listener){

        if (mAuth != null && listener != null){

            // Just in case it's there.
            mAuth.removeAuthStateListener(listener);
        }
    }



    public static void signIn(){

        if (mActivity != null && mAuth != null && mAuth.getCurrentUser() == null){

            mAuthResult = mAuth.signInAnonymously()
                    .addOnCompleteListener(mActivity, getCompleteListener());
        }
    }



    public static void signIn(OnSuccessListener<AuthResult> listener){

        if (mActivity != null &&mAuth != null && mAuth.getCurrentUser() == null){

            mAuthResult = mAuth.signInAnonymously()
                    .addOnCompleteListener(mActivity, getCompleteListener())
                    .addOnSuccessListener(mActivity, listener);
        }
    }



    public static void signIn(OnFailureListener listener){

        if (mActivity != null && mAuth != null && mAuth.getCurrentUser() == null){

            mAuthResult = mAuth.signInAnonymously()
                    .addOnCompleteListener(mActivity, getCompleteListener())
                    .addOnFailureListener(mActivity, listener);
        }
    }



    private static OnCompleteListener<AuthResult> getCompleteListener(){

        return new OnCompleteListener<AuthResult>(){

            @Override
            public void onComplete(@NonNull Task<AuthResult> task){

                int breakpoint;

                if (task.isSuccessful()){

                    breakpoint = 0;
                }else{

                    breakpoint = 0;
                }
            }
        };
    }



    public static boolean isSignedIn(){

        return mAuth != null && mAuth.getCurrentUser() != null;
    }



    public static boolean isLoggedIn(){

        String provider = userProfile("provider");

        return !provider.isEmpty() && !provider.equals("firebase");
    }



    public static void signOut(){

        mAuth.signOut();

        signIn();
    }



    public static void linkAccount(){

    }



    public static String getUid(){

//        if (mUserId == null){
//
//            FirebaseUser user = getUser();
//
//            if (user != null){
//
//                mUserId = user.getUid();
//            }
//        }
//        return mUserId;

        String userId;

        FirebaseUser user = getUser();

        if (user == null){

            userId = null;
        }else{

            userId = user.getUid();
        }

        return userId;
    }



    public static String getPrevUid(){

        return mPrevUserId;
    }



    public static FirebaseUser getUser(){

//        if (mUser == null && mAuth != null){
//
//            FirebaseUser user = mAuth.getCurrentUser();
//
//            if (user != null){
//
//                mUser = user;
//            }
//        }
//        return mUser;

        FirebaseUser user;

        if (mAuth == null){

            user = null;
        }else{

            user = mAuth.getCurrentUser();
        }

        return user;
    }



    public static FirebaseUser getPrevUser(){

        return mPrevUser;
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

        mActivity = null;

        mAuthResult = null;

        onStop();

        mAuthListener = null;

//        signOut();

        mAuth = null;

        mUser = null;

        mUserId = null;
    }


    private static class appAuthListener implements FirebaseAuth.AuthStateListener{

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null){

                if (mUser != null){

                    mPrevUser = mUser;

                    mPrevUserId = mUserId;
                }

                mUser = null;

                mUserId = null;
            }else{

                List<String> providers = user.getProviders();

                boolean collectUser = false;

                if (mUserId == null){

                    collectUser = true;

                }else if (!mUserId.equals(user.getUid())){

                    collectUser = true;

                    mPrevUser = mUser;

                    mPrevUserId = mUserId;
                }

                if (collectUser){

                    mUser = user;

                    mUserId = user.getUid();
                }
            }
        }
    }
}
