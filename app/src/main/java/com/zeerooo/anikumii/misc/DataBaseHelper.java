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

    public DataBaseHelper(Context context) {
        super(context, "AnimeFLV.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "  (ID TEXT PRIMARY KEY, TITLE TEXT, TYPE TEXT, URL TEXT, GENRES TEXT, ANIMEID TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String title, String type, String url, String genres, String animeId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("TITLE", title);
        contentValues.put("TYPE", type);
        contentValues.put("URL", url);
        contentValues.put("GENRES", genres);
        contentValues.put("ANIMEID", animeId);
        contentValues.put("ID", url);
        long result = getWritableDatabase().insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

        return result != -1;
    }

    public Cursor getListContents() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
