package com.xiuxin.app.model;

import org.json.JSONObject;
import java.io.Serializable;

/**
 * 评论数据模型
 */
public class Comment implements Serializable {
    
    public int id;
    public int blessingId;
    public String userId;
    public String userName;
    public String content;
    public Integer parentId;
    public int likeCount;
    public String createdAt;
    
    public Comment() {}
    
    /**
     * 从 JSON 对象创建 Comment
     */
    public static Comment fromJson(JSONObject json) {
        try {
            Comment comment = new Comment();
            comment.id = json.optInt("id", 0);
            comment.blessingId = json.optInt("blessing_id", 0);
            comment.userId = json.optString("user_id", "");
            comment.userName = json.optString("user_name", "");
            comment.content = json.optString("content", "");
            comment.parentId = json.isNull("parent_id") ? null : json.optInt("parent_id");
            comment.likeCount = json.optInt("like_count", 0);
            comment.createdAt = json.optString("created_at", "");
            return comment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
