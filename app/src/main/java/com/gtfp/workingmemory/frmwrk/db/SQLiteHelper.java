package com.gtfp.workingmemory.frmwrk.db;

import com.gtfp.errorhandler.ErrorHandler;
import com.gtfp.workingmemory.db.dbSQLParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
/**
 * Created by Drawn on 3/5/2017.
 */


public class SQLiteHelper{


    private static final String SQL_DIR = "sql";

    private static final String UPGRADEFILE_PREFIX = "upgrade-";

    private static final String UPGRADEFILE_SUFFIX = ".sql";

    // ROWID is automatically added to all SQLite tables by default, and is a unique integer,
    private static final String DBKEY_FIELD = "rowid";

    private final String mDBName;

    private final String mSchema;

    private int mDBVersion;

    private SQLiteDatabase mDB;

    private Cursor mResultSet;

    private SQLHelper mSQLHelper;

    private Set<OnOpenListener> mOnOpenListeners = new HashSet<>();

    private Set<OnConfigureListener> mOnConfigureListeners = new HashSet<>();




    public SQLiteHelper(@NonNull Context context, @NonNull String dbName, @NonNull int dbVersion,
            @NonNull String tableDef){

        if (dbName == null || dbName.isEmpty()){

            mDBName = "table";
        }else{

            mDBName = dbName.trim();
        }

        if (dbVersion < 1){

            mDBVersion = 1;
        }else{

            mDBVersion = dbVersion;
        }

        if (tableDef == null || tableDef.isEmpty()){

            mSchema = "";
        }else{

            mSchema = tableDef.trim();
        }

        mSQLHelper = new SQLHelper(context);
    }




    public SQLiteHelper open(){

        mSQLHelper.open();

        return this;
    }




    public boolean isOpen(){

        return mSQLHelper.isOpen();
    }




    public SQLiteDatabase getDatabase(){

        return mSQLHelper.getDatabase();
    }




    public SQLiteHelper close(){

        mSQLHelper.close();

        return this;
    }




    public String getName(){

        return mDBName;
    }




    public int getVersion(){

        return mDBVersion;
    }




    public long insert(ContentValues recValues){

        return mSQLHelper.insert(recValues);
    }




    public int update(ContentValues recValues, long rowId){

        return  mSQLHelper.update(recValues, rowId);
    }




    public int delete(Long rowID){

        return mSQLHelper.delete(rowID);
    }




    public int delete(Integer rowID){

        return mSQLHelper.delete(rowID);
    }




    public int delete(String rowID){

        return mSQLHelper.delete(rowID);
    }


    public long getRowID(long recId){

        if(recId <= 0) return recId;

        return mSQLHelper.getRowID(recId);
    }




    public Cursor runQuery(String sqlStmt){

        open();

        return mSQLHelper.runQuery(sqlStmt);
    }




    public boolean drop(){

        return mSQLHelper.drop();
    }




    public boolean drop(SQLiteDatabase db){

        return mSQLHelper.drop(db);
    }




    public void setOnOpenListener(OnOpenListener listener){

        mOnOpenListeners.add(listener);
    }




    public void setOnConfigureListener(OnConfigureListener listener){

        mOnConfigureListeners.add(listener);
    }

    public interface OnOpenListener{

        void onOpen(SQLiteDatabase db);
    }

    public interface OnConfigureListener{

        void onConfigure(SQLiteDatabase db);
    }

    /**
     *
     *
     *
     *
     * Inner Class
     *
     *
     *
     *
     */
    private class SQLHelper extends SQLiteOpenHelper{


        private AssetManager mAssetManager;



        SQLHelper(Context context){

            super(context, mDBName + ".db", null, mDBVersion);

            mAssetManager = context.getAssets();
        }




        @Override
        public void onCreate(SQLiteDatabase db){

            try{

                db.execSQL("CREATE TABLE IF NOT EXISTS " + mDBName + mSchema);

            }catch (Exception ex){

                ErrorHandler.logError(ex);
            }
        }




        @Override
        public void onConfigure(SQLiteDatabase db){

            if (mOnConfigureListeners != null){

                for (OnConfigureListener listener : mOnConfigureListeners){

                    listener.onConfigure(db);
                }
            }
        }




        @Override
        public void onOpen(SQLiteDatabase db){

            if (mOnOpenListeners != null){

                for (OnOpenListener listener : mOnOpenListeners){

                    listener.onOpen(db);
                }
            }
        }




        public int update(ContentValues recValues, long id){

            int result;

            try{

                result = mDB.update(mDBName, recValues, DBKEY_FIELD + " = ?",
                        new String[]{String.valueOf(id)});

            }catch (RuntimeException ex){

                ErrorHandler.logError(ex);

                result = 0;
            }

            return result;
        }




        public long insert(ContentValues recValues){

            long result;

            try{

                result = mDB.insert(mDBName, null, recValues);

            }catch (RuntimeException ex){

                result = 0;
            }

            return result;
        }




        public int delete(Long rowID){

            return  delete(Long.toString(rowID));
        }




        public int delete(Integer rowID){

            return  delete(Integer.toString(rowID));
        }




        public int delete(String rowID){

            String where = DBKEY_FIELD + "=?";

            try{

                return mDB.delete(mDBName, where, new String[]{rowID});

            }catch (Exception ex){

                ErrorHandler.logError(ex);

                return -1;
            }
        }




        public long getRowID(long recId){

            Cursor records;

            final String SELECT_STMT = "SELECT " + DBKEY_FIELD + " AS _id FROM " + mDBName
                    + " WHERE id = " + recId
                    + " ORDER BY " + DBKEY_FIELD + " DESC LIMIT 1";

            records = runQuery(SELECT_STMT);

            long rowID = 0;

            // You've got to move the cursor's position pointer after the query.
            if (records.moveToFirst()){

                try{

                    rowID = records.getLong(0);

                }catch (Exception ex){

                    rowID = 0;
                }
            }

            return rowID;
        }




        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

            setTheGrade(db, oldVersion, newVersion);
        }




        public boolean currentRecs(){

            try{

                mDB.execSQL("SELECT  * FROM " + mDBName);

            }catch (Exception ex){

                return false;
            }

            return true;
        }




        public SQLHelper open(){

            try{

                if (!isOpen()){

                    mDB = getWritableDatabase();
                }

            }catch (Exception ex){

                if (mDB != null && mDB.isOpen()){

                    mDB.close();
                }

                if (mDB != null){

                    mDB = null;
                }
            }

            return this;
        }




        public boolean isOpen(){

            return mDB != null && mDB.isOpen();
        }




        public boolean drop(){

            return drop(mDB);
        }




        public boolean drop(SQLiteDatabase db){

            if(db == null) return false;

            boolean dropped;

            try{

                db.execSQL("DROP TABLE IF EXISTS " + mDBName);

                dropped = true;

            }catch (Exception ex){

                dropped = false;
            }

            return dropped;
        }




        public void close(){

            super.close();

            if (mDB != null){

                mDB.close();
            }

            // It's good to lose the reference here with the connection closed.
            mDB = null;

            if (mResultSet != null){

                mResultSet.close();
            }
            // This resource too. It could be huge!
            mResultSet = null;

            if (mAssetManager != null){

                mAssetManager = null;
            }
        }




        public SQLiteDatabase getDatabase(){

            return mDB;
        }




        public Cursor runQuery(String sqlStmt){

            Cursor records;

            try{

                records = mDB.rawQuery(sqlStmt, null);

            }catch (Exception ex){

                // If something goes wrong, return an empty cursor.
                records = new MatrixCursor(new String[]{"empty"});
            }

            return records;
        }




        private void setTheGrade(SQLiteDatabase db, int oldVersion, int newVersion){

            try{

                for (String sqlFile : dbSQLParser.list(SQL_DIR, mAssetManager)){

                    if (sqlFile.startsWith(UPGRADEFILE_PREFIX)){

                        int fileVersion = Integer.parseInt(
                                sqlFile.substring(UPGRADEFILE_PREFIX.length(),
                                        sqlFile.length() - UPGRADEFILE_SUFFIX.length()));

                        if (fileVersion > oldVersion && fileVersion <= newVersion){

                            execSqlFile(sqlFile, db);
                        }
                    }
                }
            }catch (IOException ex){

                ErrorHandler.logError(ex);

            }catch (RuntimeException ex){

                ErrorHandler.logError(ex);

                // Be sure to throw the exception back up to be handled there as well.
                throw ex;
            }
        }




        private void execSqlFile(String sqlFile, SQLiteDatabase db)
                throws SQLException, IOException{

            int line = 0;

            for (String sqlInstruction : dbSQLParser
                    .parseSqlFile(SQL_DIR + "/" + sqlFile, mAssetManager)){

                line = line + 1;

                if (line == 1 && sqlInstruction.substring(0, 7).equals("ATTACH ")){

                    db.endTransaction();
                }

                db.execSQL(sqlInstruction);
            }
        }

    }
}