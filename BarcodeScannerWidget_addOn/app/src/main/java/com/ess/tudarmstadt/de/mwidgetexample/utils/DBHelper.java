package com.ess.tudarmstadt.de.mwidgetexample.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lukas on 16.06.15.
 */
public class DBHelper extends SQLiteOpenHelper {
    // Database name
    public static final String DATABASE_NAME = "BarcodeScanner.db";

    // Table names
    public static final String SURVEY_TABLE_NAME = "surveyDB";
    public static final String BARCODE_TABLE_NAME = "barcodeDB";

    // IDs
    public static final String SURVEY_COLUMN_ID = "id";
    public static final String BARCODE_COLUMN_ID = "barcodeId";

    // Values
    public static final String SURVEY_COLUMN_DATE = "date";
    public static final String SURVEY_COLUMN_SURVEY = "survey";
    public static final String SURVEY_COLUMN_QUESTION = "question";
    public static final String SURVEY_COLUMN_VALUE = "value";
    public static final String BARCODE_BARCODE = "barcode";
    public static final String BARCODE_NAME = "name";

    private static final String CREATE_TABLE_SURVEY = "CREATE TABLE "
            + SURVEY_TABLE_NAME + "(" + SURVEY_COLUMN_ID + " INTEGER PRIMARY KEY," + SURVEY_COLUMN_DATE
            + " TEXT," + SURVEY_COLUMN_SURVEY + " INTEGER," + SURVEY_COLUMN_QUESTION
            + " INTEGER," + SURVEY_COLUMN_VALUE + " INTEGER" + ")";

    private static final String CREATE_TABLE_BARCODE = "CREATE TABLE "
            + BARCODE_TABLE_NAME + "(" + BARCODE_COLUMN_ID + " INTEGER PRIMARY KEY," + BARCODE_BARCODE
            + " TEXT UNIQUE," + BARCODE_NAME + " TEXT" + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BARCODE);
        db.execSQL(CREATE_TABLE_SURVEY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertBarcode (String barcode, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("barcode", barcode);
        contentValues.put("name", name);
        db.replace("barcodeDB", null, contentValues);
        return true;
    }

    public boolean insertValue  (String date, int survey, int question, int value)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("survey", survey);
        contentValues.put("question", question);
        contentValues.put("value", value);
        db.insert("surveyDB", null, contentValues);
        return true;
    }

    // Getting single contact
    public String getTitle(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        String title = "";
        Cursor cursor = db.query(
                BARCODE_TABLE_NAME, new String[] {
                        BARCODE_COLUMN_ID, BARCODE_BARCODE, BARCODE_NAME},
                "barcode = ' " + barcode+ " ' ", null, null, null, null);
        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            title = cursor.getString(2);
        }
        return title;
    }

    public ArrayList<String> getAllDates()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from surveyDB", null );
        res.moveToFirst();

        String temp;
        while(!res.isAfterLast()){
            temp = res.getString(res.getColumnIndex(SURVEY_COLUMN_DATE));
            if(!array_list.contains(temp)) {
                array_list.add(temp);
            }
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getAllSurveys(String item)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from surveyDB", null );
        res.moveToFirst();

        String temp;
        while(!res.isAfterLast()){
            temp = res.getString(res.getColumnIndex(SURVEY_COLUMN_SURVEY));
            if (res.getString(res.getColumnIndex(SURVEY_COLUMN_DATE)).equals(item) && (!array_list.contains("Umfrage " + temp))) {
                array_list.add("Umfrage " + temp);
            }
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getAllResults(String date, String item)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from surveyDB", null );
        res.moveToFirst();

        String temp1;
        String temp2;
        while(!res.isAfterLast()){
            temp1 = res.getString(res.getColumnIndex(SURVEY_COLUMN_QUESTION));
            temp2 = res.getString(res.getColumnIndex(SURVEY_COLUMN_VALUE));
            if (res.getString(res.getColumnIndex(SURVEY_COLUMN_DATE)).equals(date) &&
                    res.getString(res.getColumnIndex(SURVEY_COLUMN_SURVEY)).equals(item.split(" ")[1])) {
                if (item.split(" ")[1].equals("5")) {
                    int temp3 = Integer.valueOf(temp2) * 10;
                    temp2 = String.valueOf(temp3);
                }
                array_list.add("Frage " + temp1 + ":" + temp2);
            }
            res.moveToNext();
        }
        return array_list;
    }
}