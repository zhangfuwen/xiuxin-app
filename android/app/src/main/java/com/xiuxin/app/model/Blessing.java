package com.xiuxin.app.model;

import org.json.JSONObject;
import java.io.Serializable;

/**
 * 禅语/祝福数据模型
 */
public class Blessing implements Serializable {
    
    public int id;
    public String userId;
    public String userName;
    public String text;
    public String source;
    public String practice;
    public String category;
    public int likeCount;
    public int favoriteCount;
    public String createdAt;
    public String updatedAt;
    public boolean isLiked;
    public boolean isFavorited;
    
    public Blessing() {}
    
    /**
     * 从 JSON 对象创建 Blessing
     */
    public static Blessing fromJson(JSONObject json) {
        try {
            Blessing blessing = new Blessing();
            blessing.id = json.optInt("id", 0);
            blessing.userId = json.optString("user_id", "");
            blessing.userName = json.optString("user_name", "");
            blessing.text = json.optString("text", "");
            blessing.source = json.optString("source", "");
            blessing.practice = json.optString("practice", "");
            blessing.category = json.optString("category", "禅宗");
            blessing.likeCount = json.optInt("like_count", 0);
            blessing.favoriteCount = json.optInt("favorite_count", 0);
            blessing.createdAt = json.optString("created_at", "");
            blessing.updatedAt = json.optString("updated_at", "");
            blessing.isLiked = json.optBoolean("is_liked", false);
            blessing.isFavorited = json.optBoolean("is_favorited", false);
            return blessing;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "Blessing{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
