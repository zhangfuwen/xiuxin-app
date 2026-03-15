package com.xiuxin.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xiuxin.app.model.Blessing;

import java.util.ArrayList;
import java.util.List;

public class LocalBlessingDb extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "blessings_local.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_SOURCE = "source";
    private static final String COLUMN_PRACTICE = "practice";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LIKE_COUNT = "like_count";
    private static final String COLUMN_FAVORITE_COUNT = "favorite_count";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_FAVORITED_AT = "favorited_at";

    public LocalBlessingDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_USER_ID + " TEXT," +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_TEXT + " TEXT NOT NULL," +
                COLUMN_SOURCE + " TEXT," +
                COLUMN_PRACTICE + " TEXT," +
                COLUMN_CATEGORY + " TEXT," +
                COLUMN_LIKE_COUNT + " INTEGER DEFAULT 0," +
                COLUMN_FAVORITE_COUNT + " INTEGER DEFAULT 0," +
                COLUMN_CREATED_AT + " TEXT," +
                COLUMN_FAVORITED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public boolean addFavorite(Blessing blessing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, blessing.id);
        values.put(COLUMN_USER_ID, blessing.userId);
        values.put(COLUMN_USER_NAME, blessing.userName);
        values.put(COLUMN_TEXT, blessing.text);
        values.put(COLUMN_SOURCE, blessing.source);
        values.put(COLUMN_PRACTICE, blessing.practice);
        values.put(COLUMN_CATEGORY, blessing.category);
        values.put(COLUMN_LIKE_COUNT, blessing.likeCount);
        values.put(COLUMN_FAVORITE_COUNT, blessing.favoriteCount);
        values.put(COLUMN_CREATED_AT, blessing.createdAt);
        
        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result != -1;
    }

    public boolean removeFavorite(int blessingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FAVORITES, COLUMN_ID + " = ?", new String[]{String.valueOf(blessingId)});
        db.close();
        return rows > 0;
    }

    public boolean isFavorite(int blessingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID},
                COLUMN_ID + " = ?", new String[]{String.valueOf(blessingId)},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public List<Blessing> getAllFavorites() {
        List<Blessing> blessings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, null, null, null, null,
                COLUMN_FAVORITED_AT + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                Blessing blessing = new Blessing();
                blessing.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                blessing.userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                blessing.userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
                blessing.text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
                blessing.source = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE));
                blessing.practice = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRACTICE));
                blessing.category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                blessing.likeCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKE_COUNT));
                blessing.favoriteCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE_COUNT));
                blessing.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
                blessing.isFavorited = true;
                blessings.add(blessing);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return blessings;
    }

    public int getFavoritesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FAVORITES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
