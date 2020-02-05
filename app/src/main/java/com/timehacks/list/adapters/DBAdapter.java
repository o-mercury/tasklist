package com.timehacks.list.adapters;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Redixbit on 26-07-2017.
 */

public class DBAdapter extends SQLiteOpenHelper {
    private static String DB_PATH;
    public static String DB_NAME = "database.sqlite";

    SQLiteDatabase db;
    Context ctx;

    DBAdapter(Context c) {
        super(c, DB_NAME, null, 1);
        ctx = c;
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {

        } else {
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {
                Log.d("Error", "Copy Database");
                throw new Error("Error copying database" + e);

            }
        }
    }

    private boolean checkDataBase() {
        DB_PATH = "/data/data/" + ctx.getPackageName() + "/databases/";
        Log.d("DB_PATH ", "" + DB_PATH);
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.d("db", "" + dbFile);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        DB_PATH = "/data/data/" + ctx.getPackageName() + "/databases/";
        InputStream myInput = ctx.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }



    public void openDataBase() throws SQLException {
        DB_PATH = "/data/data/" + ctx.getPackageName() + "/databases/";
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    @Override
    public synchronized void close() {
        if (db != null)
            db.close();

        super.close();
    }
}
