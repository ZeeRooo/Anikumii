package com.zeerooo.anikumii.misc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ZeeRooo on 02/02/18
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private final String TABLE_NAME = "AnimesDB";
    private ContentValues contentValues = new ContentValues();

    public DataBaseHelper(Context context) {
        super(context, "Anikumii.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "  (ID TEXT PRIMARY KEY, TITLE TEXT, TYPE TEXT, IMAGE TEXT, DATE TEXT, LASTEPISODE INT, POSITION INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String title, String type, String url, String imageUrl, String date, int lastEpisode, int position) {
        contentValues.clear();

        contentValues.put("TITLE", title);
        contentValues.put("TYPE", type);
        contentValues.put("IMAGE", imageUrl);
        contentValues.put("DATE", date);
        contentValues.put("LASTEPISODE", lastEpisode);
        contentValues.put("ID", url);
        contentValues.put("POSITION", position);

        return (getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)) != -1;
    }

    public int getDatabaseRows(Cursor cursor) {
        try {
            cursor = getReadableDatabase().rawQuery("SELECT TITLE FROM AnimesDB", null);
            cursor.moveToLast();
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }
}
