package com.gtfp.workingmemory.db;

import com.gtfp.workingmemory.todo.ToDoItem;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;


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
 */
public interface dbInterface{

    dbInterface open();

    boolean isOpen();

    void close();

    boolean createCurrentRecs();

 //** Not even used.
//    dbInterface getDatabase();

    void clearResultSet();

   Cursor getResultSet();

    boolean showDeleted(boolean showDeleted);

    Cursor runQuery(String sqlStmt);

    Cursor getRecs();

    Cursor getCurrentRecs();

    Cursor getDeletedRecs();

    <T> T getLastRowID();

    ArrayList<ToDoItem> ToDoList(ArrayList<HashMap<String, String>> recs);

    ArrayList<HashMap<String, String>> getDataArrayList();

    int deleteRec(Long rowID);

    boolean save(ToDoItem itemToDo);

    boolean markRec(Long rowID);

    int updateRec(ToDoItem itemToDo);

    int updateRec(ContentValues recValues, long id);

    long insertRec(ContentValues recValues);

    boolean createEmptyTemp();

    long insertTempRec();

    Cursor getNewTempRecs();

    boolean ifNewRec();

    void bindRecValues(String columnName, String value);

    boolean importRec();

    Cursor getRecs(String whereClause);

    Cursor getRecord(int rowId);

    void onDestroy();

}
