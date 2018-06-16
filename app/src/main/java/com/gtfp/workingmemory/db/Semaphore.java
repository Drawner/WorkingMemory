package com.gtfp.workingmemory.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import com.gtfp.workingmemory.Auth.Auth;

import static com.gtfp.workingmemory.db.dbFireBase.getDatabase;


/**
 * Copyright (C) 2017  Greg T. F. Perry
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

