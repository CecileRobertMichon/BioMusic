package com.dp.cecile.biomusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by cecilerobertm on 2017-03-03.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";
    public static final String TABLE_NAME = "SESSIONS";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_EMOTION = "EMOTION";
    public static final String COLUMN_COMMENT = "COMMENT";
    public static final String COLUMN_DATE = "DATE";

    private static SQLiteHelper sInstance;

    private SQLiteDatabase database;

    public static synchronized SQLiteHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new SQLiteHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_EMOTION + " VARCHAR, " + COLUMN_COMMENT + " VARCHAR, " + COLUMN_DATE + " VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertRecord(SessionModel session) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EMOTION, session.getEmotion());
        contentValues.put(COLUMN_COMMENT, session.getComment());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        contentValues.put(COLUMN_DATE, dateFormat.format(date));
        database.insert(TABLE_NAME, null, contentValues);
        Log.d("DB", "inserted record at time " + dateFormat.format(date));
        database.close();
    }

    public void insertRecordAlternate(SessionModel session) {
        database = this.getReadableDatabase();
        database.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_EMOTION + "," + COLUMN_COMMENT + "," + COLUMN_DATE + ") VALUES('" + session.getEmotion() + "','" + session.getComment() + "','" + session.getDate() + "')");
        database.close();
    }

    public ArrayList<SessionModel> getAllRecords() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<SessionModel> sessions = new ArrayList<>();
        SessionModel sessionModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();

                sessionModel = new SessionModel();
                sessionModel.setID(cursor.getString(0));
                sessionModel.setEmotion(cursor.getString(1));
                sessionModel.setComment(cursor.getString(2));
                sessionModel.setDate(cursor.getString(3));

                sessions.add(sessionModel);
            }
        }
        cursor.close();
        database.close();

        return sessions;
    }

    public ArrayList<SessionModel> getAllRecordsAlternate() {
        database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<SessionModel> sessions = new ArrayList<>();
        SessionModel sessionModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();

                sessionModel = new SessionModel();
                sessionModel.setID(cursor.getString(0));
                sessionModel.setEmotion(cursor.getString(1));
                sessionModel.setComment(cursor.getString(2));
                sessionModel.setDate(cursor.getString(3));

                sessions.add(sessionModel);
            }
        }
        cursor.close();
        database.close();

        return sessions;
    }

    public void deleteAllRecords() {
        database = this.getReadableDatabase();
        database.delete(TABLE_NAME, null, null);
        database.close();
    }

    public void deleteAllRecordsAlternate() {
        database = this.getReadableDatabase();
        database.execSQL("delete from " + TABLE_NAME);
        database.close();
    }

    public void deleteRecord(SessionModel session) {
        database = this.getReadableDatabase();
        database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{session.getID()});
        database.close();
    }

    public void deleteRecordAlternate(SessionModel session) {
        database = this.getReadableDatabase();
        database.execSQL("delete from " + TABLE_NAME + " where " + COLUMN_ID + " = '" + session.getID() + "'");
        database.close();
    }

}