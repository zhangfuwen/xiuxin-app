package com.xiuxin.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.xiuxin.app.model.Blessing;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地禅语数据库 - 保存用户点赞和收藏的禅语
 */
public class LocalBlessingDb extends SQLiteOpenHelper {
    
    private static final String TAG = "LocalBlessingDb";
    private static final String DATABASE_NAME = "blessings_local.db";
    private static final int DATABASE_VERSION = 3; // Added bg_path column
    
    // Table name
    private static final String TABLE_NAME = "local_blessings";
    
    // Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "userId";
    private static final String COLUMN_USER_NAME = "userName";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_SOURCE = "source";
    private static final String COLUMN_PRACTICE = "practice";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LIKE_COUNT = "likeCount";
    private static final String COLUMN_FAVORITE_COUNT = "favoriteCount";
    private static final String COLUMN_CREATED_AT = "createdAt";
    private static final String COLUMN_UPDATED_AT = "updatedAt";
    private static final String COLUMN_IS_LIKED = "isLiked";
    private static final String COLUMN_IS_FAVORITED = "isFavorited";
    private static final String COLUMN_FONT_PATH = "fontPath";
    private static final String COLUMN_BG_PATH = "bgPath";
    
    private static LocalBlessingDb instance;
    
    public static synchronized LocalBlessingDb getInstance(Context context) {
        if (instance == null) {
            instance = new LocalBlessingDb(context.getApplicationContext());
        }
        return instance;
    }
    
    private LocalBlessingDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_USER_ID + " TEXT NOT NULL," +
                COLUMN_USER_NAME + " TEXT NOT NULL," +
                COLUMN_TEXT + " TEXT NOT NULL," +
                COLUMN_SOURCE + " TEXT," +
                COLUMN_PRACTICE + " TEXT," +
                COLUMN_CATEGORY + " TEXT DEFAULT '禅宗'," +
                COLUMN_LIKE_COUNT + " INTEGER DEFAULT 0," +
                COLUMN_FAVORITE_COUNT + " INTEGER DEFAULT 0," +
                COLUMN_CREATED_AT + " TEXT," +
                COLUMN_UPDATED_AT + " TEXT," +
                COLUMN_IS_LIKED + " INTEGER DEFAULT 0," +
                COLUMN_IS_FAVORITED + " INTEGER DEFAULT 0," +
                COLUMN_FONT_PATH + " TEXT," +
                COLUMN_BG_PATH + " TEXT" +
                ")";
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "Table created: " + TABLE_NAME);
        
        // Create indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_liked ON " + TABLE_NAME + "(" + COLUMN_IS_LIKED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_favorited ON " + TABLE_NAME + "(" + COLUMN_IS_FAVORITED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_category ON " + TABLE_NAME + "(" + COLUMN_CATEGORY + ")");
        Log.d(TAG, "Indexes created");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add font_path column for version 2
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_FONT_PATH + " TEXT");
                Log.d(TAG, "Database upgraded to version 2: added font_path column");
            } catch (Exception e) {
                Log.e(TAG, "Failed to add font_path column", e);
            }
        }
        if (oldVersion < 3) {
            // Add bg_path column for version 3
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_BG_PATH + " TEXT");
                Log.d(TAG, "Database upgraded to version 3: added bg_path column");
            } catch (Exception e) {
                Log.e(TAG, "Failed to add bg_path column", e);
            }
        }
    }
    
    /**
     * 保存或更新禅语到本地数据库
     */
    public void saveBlessing(Blessing blessing) {
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
        values.put(COLUMN_UPDATED_AT, blessing.updatedAt);
        values.put(COLUMN_IS_LIKED, blessing.isLiked ? 1 : 0);
        values.put(COLUMN_IS_FAVORITED, blessing.isFavorited ? 1 : 0);
        values.put(COLUMN_FONT_PATH, blessing.fontPath);
        values.put(COLUMN_BG_PATH, blessing.bgPath);
        
        // Insert or replace
        db.replace(TABLE_NAME, null, values);
        Log.d(TAG, "Saved blessing id=" + blessing.id + " locally");
        
        db.close();
    }
    
    /**
     * 批量保存禅语列表
     */
    public void saveBlessings(List<Blessing> blessings) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        
        try {
            for (Blessing blessing : blessings) {
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
                values.put(COLUMN_UPDATED_AT, blessing.updatedAt);
                values.put(COLUMN_IS_LIKED, blessing.isLiked ? 1 : 0);
                values.put(COLUMN_IS_FAVORITED, blessing.isFavorited ? 1 : 0);
                values.put(COLUMN_FONT_PATH, blessing.fontPath);
                values.put(COLUMN_BG_PATH, blessing.bgPath);
                
                db.replace(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Batch saved " + blessings.size() + " blessings");
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    
    /**
     * 更新点赞状态
     */
    public void updateLikeStatus(int blessingId, boolean isLiked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_LIKED, isLiked ? 1 : 0);
        
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(blessingId)});
        Log.d(TAG, "Updated like status for blessing id=" + blessingId + ": " + isLiked);
        
        db.close();
    }
    
    /**
     * 更新收藏状态
     */
    public void updateFavoriteStatus(int blessingId, boolean isFavorited) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_FAVORITED, isFavorited ? 1 : 0);
        
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(blessingId)});
        Log.d(TAG, "Updated favorite status for blessing id=" + blessingId + ": " + isFavorited);
        
        db.close();
    }
    
    /**
     * 获取所有点赞的禅语
     */
    public List<Blessing> getLikedBlessings() {
        return queryBlessings(COLUMN_IS_LIKED + " = 1", null);
    }
    
    /**
     * 获取所有收藏的禅语
     */
    public List<Blessing> getFavoritedBlessings() {
        return queryBlessings(COLUMN_IS_FAVORITED + " = 1", null);
    }
    
    /**
     * 根据分类获取禅语
     */
    public List<Blessing> getBlessingsByCategory(String category) {
        if (category == null || category.equals("全部")) {
            return getAllBlessings();
        }
        return queryBlessings(COLUMN_CATEGORY + " = ?", new String[]{category});
    }
    
    /**
     * 根据分类和过滤条件获取禅语
     */
    public List<Blessing> getBlessingsByFilter(String category, String filterType) {
        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();
        
        // Category filter
        if (category != null && !category.equals("全部")) {
            conditions.add(COLUMN_CATEGORY + " = ?");
            args.add(category);
        }
        
        // Filter type (liked/favorited)
        if ("liked".equals(filterType)) {
            conditions.add(COLUMN_IS_LIKED + " = 1");
        } else if ("favorited".equals(filterType)) {
            conditions.add(COLUMN_IS_FAVORITED + " = 1");
        }
        
        String selection = conditions.isEmpty() ? null : String.join(" AND ", conditions);
        String[] selectionArgs = args.isEmpty() ? null : args.toArray(new String[0]);
        
        return queryBlessings(selection, selectionArgs);
    }
    
    /**
     * 获取所有本地禅语
     */
    public List<Blessing> getAllBlessings() {
        return queryBlessings(null, null);
    }
    
    private List<Blessing> queryBlessings(String selection, String[] selectionArgs) {
        List<Blessing> blessings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String[] projection = {
                COLUMN_ID, COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_TEXT, COLUMN_SOURCE,
                COLUMN_PRACTICE, COLUMN_CATEGORY, COLUMN_LIKE_COUNT, COLUMN_FAVORITE_COUNT,
                COLUMN_CREATED_AT, COLUMN_UPDATED_AT, COLUMN_IS_LIKED, COLUMN_IS_FAVORITED
        };
        
        String sortOrder = COLUMN_CREATED_AT + " DESC";
        
        Cursor cursor = db.query(
                TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder
        );
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Blessing blessing = cursorToBlessing(cursor);
                blessings.add(blessing);
            }
            cursor.close();
        }
        
        db.close();
        Log.d(TAG, "Retrieved " + blessings.size() + " blessings from local database");
        return blessings;
    }
    
    private List<Blessing> getBlessingsByFilter(String selection, String[] selectionArgs) {
        return queryBlessings(selection, selectionArgs);
    }
    
    /**
     * 检查禅语是否在本地数据库中
     */
    public boolean exists(int blessingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_ID};
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(blessingId)};
        
        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        
        return count > 0;
    }
    
    /**
     * 删除禅语
     */
    public void deleteBlessing(int blessingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(blessingId)});
        db.close();
        Log.d(TAG, "Deleted blessing id=" + blessingId + " from local database");
    }
    
    /**
     * 清空所有数据
     */
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
        Log.d(TAG, "Cleared all local blessings");
    }
    
    /**
     * 获取本地禅语数量
     */
    public int getCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * 获取点赞的禅语数量
     */
    public int getLikedCount() {
        return getCountByFilter(COLUMN_IS_LIKED + " = 1");
    }
    
    /**
     * 获取收藏的禅语数量
     */
    public int getFavoritedCount() {
        return getCountByFilter(COLUMN_IS_FAVORITED + " = 1");
    }
    
    private int getCountByFilter(String selection) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + selection;
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * Cursor 转 Blessing 对象
     */
    private Blessing cursorToBlessing(Cursor cursor) {
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
        blessing.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT));
        blessing.isLiked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LIKED)) == 1;
        blessing.isFavorited = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITED)) == 1;
        
        // Get fontPath if column exists (for backward compatibility)
        int fontPathIndex = cursor.getColumnIndex(COLUMN_FONT_PATH);
        if (fontPathIndex >= 0) {
            blessing.fontPath = cursor.getString(fontPathIndex);
        }
        
        // Get bgPath if column exists (for backward compatibility)
        int bgPathIndex = cursor.getColumnIndex(COLUMN_BG_PATH);
        if (bgPathIndex >= 0) {
            blessing.bgPath = cursor.getString(bgPathIndex);
        }
        
        return blessing;
    }
}
