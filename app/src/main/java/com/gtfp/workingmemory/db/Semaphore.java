package com.gtfp.workingmemory.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import com.gtfp.workingmemory.Auth.Auth;

import static com.gtfp.workingmemory.db.dbFireBase.getDatabase;
/**
 * Created by Drawn on 5/4/2017.
 */

public class Semaphore{

    private static long mStamp;

    private static long mTimeStamp;




    static boolean got(){

        return mStamp == mTimeStamp;
    }




    static boolean got(DataSnapshot snapshot){

        if (snapshot.hasChild("semaphore")){

            for (DataSnapshot shot : snapshot.getChildren()){

                if(shot.getKey().equals("semaphore")){

                    mTimeStamp = (long) shot.getValue();

                    break;
                }
            }
        }

        return got();
    }




    public static void write(){

        mStamp = System.currentTimeMillis() / 1000;

        DBRef().setValue(mStamp);
    }




    private static  DatabaseReference DBRef(){

        String id = Auth.getUid();

        DatabaseReference dbRef;

        if(id == null){

            dbRef =  getDatabase().getReference().child("tasks").child("dummy").child("semaphore");
        }else{

           dbRef =  getDatabase().getReference().child("tasks").child(id).child("semaphore");
        }

        return dbRef;
    }
}

