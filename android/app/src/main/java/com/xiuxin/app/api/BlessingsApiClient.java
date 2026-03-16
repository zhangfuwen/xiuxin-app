package com.xiuxin.app.api;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.xiuxin.app.model.Blessing;
import com.xiuxin.app.model.Comment;

/**
 * Blessings API 客户端
 * 
 * API 端点：http://bot.xjbcode.fun/api/blessings
 */
public class BlessingsApiClient {
    
    private static final String TAG = "BlessingsApiClient";
    // Use public IP directly to avoid DNS issues, fallback to domain if needed
    private static final String BASE_URL = "http://47.254.68.82/api/blessings";
    // private static final String BASE_URL = "http://bot.xjbcode.fun/api/blessings";
    
    private static BlessingsApiClient instance;
    private final ExecutorService executor;
    private String currentUserId;
    private String currentUserName;
    
    /**
     * API 响应回调接口
     */
    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
    
    private BlessingsApiClient() {
        executor = Executors.newSingleThreadExecutor();
        // 生成临时用户 ID（实际应该使用登录系统）
        currentUserId = "android_user_" + System.currentTimeMillis();
        currentUserName = "修心用户";
    }
    
    public static synchronized BlessingsApiClient getInstance() {
        if (instance == null) {
            instance = new BlessingsApiClient();
        }
        return instance;
    }
    
    /**
     * 设置当前用户信息
     */
    public void setCurrentUser(String userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
    }
    
    /**
     * 获取禅语列表
     * @param category 分类筛选（null 表示全部）
     * @param limit 数量限制
     * @param callback 回调
     */
    public void getBlessings(String category, int limit, final ApiCallback<List<Blessing>> callback) {
        Log.d(TAG, "=== getBlessings START ===");
        Log.d(TAG, "Category: " + category + ", Limit: " + limit);
        Log.d(TAG, "Current UserId: " + currentUserId);
        
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "?limit=" + limit;
                if (category != null && !category.equals("全部")) {
                    urlStr += "&category=" + java.net.URLEncoder.encode(category, "UTF-8");
                }
                // NOTE: Removed user_id from GET request - it was filtering out all blessings
                // user_id should only be used for POST/PUT/DELETE operations
                // if (currentUserId != null) {
                //     urlStr += "&user_id=" + currentUserId;
                // }
                
                Log.d(TAG, "Full URL: " + urlStr);
                Log.d(TAG, "Opening HTTP connection...");
                
                long startTime = System.currentTimeMillis();
                String response = httpGet(urlStr);
                long endTime = System.currentTimeMillis();
                
                Log.d(TAG, "HTTP request completed in " + (endTime - startTime) + "ms");
                Log.d(TAG, "Response length: " + response.length() + " bytes");
                Log.d(TAG, "Response preview: " + response.substring(0, Math.min(500, response.length())));
                
                JSONObject json = new JSONObject(response);
                Log.d(TAG, "JSON parsed successfully");
                
                if (json.optBoolean("success", false)) {
                    JSONArray data = json.optJSONArray("data");
                    List<Blessing> blessings = new ArrayList<>();
                    if (data != null) {
                        Log.d(TAG, "Data array length: " + data.length());
                        for (int i = 0; i < data.length(); i++) {
                            Blessing blessing = Blessing.fromJson(data.optJSONObject(i));
                            if (blessing != null) {
                                blessings.add(blessing);
                            }
                        }
                        Log.d(TAG, "Parsed " + blessings.size() + " blessings");
                    }
                    Log.d(TAG, "Calling onSuccess callback");
                    postSuccess(callback, blessings);
                } else {
                    String errorMsg = json.optString("error", "Unknown error");
                    Log.e(TAG, "API returned error: " + errorMsg);
                    postError(callback, errorMsg);
                }
            } catch (Exception e) {
                Log.e(TAG, "=== EXCEPTION in getBlessings ===", e);
                Log.e(TAG, "Exception type: " + e.getClass().getName());
                Log.e(TAG, "Exception message: " + e.getMessage());
                postError(callback, "网络错误：" + e.getMessage() + " (请检查网络连接)");
            } finally {
                Log.d(TAG, "=== getBlessings END ===");
            }
        });
    }
    
    /**
     * 发布新禅语
     */
    public void publishBlessing(String text, String source, String practice, String category, 
                                final ApiCallback<Blessing> callback) {
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("user_id", currentUserId);
                payload.put("user_name", currentUserName);
                payload.put("text", text);
                payload.put("source", source);
                payload.put("practice", practice);
                payload.put("category", category);
                
                String response = httpPost(BASE_URL, payload);
                JSONObject json = new JSONObject(response);
                
                if (json.optBoolean("success", false)) {
                    Blessing blessing = Blessing.fromJson(json.optJSONObject("data"));
                    postSuccess(callback, blessing);
                } else {
                    postError(callback, json.optString("error", "发布失败"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error publishing blessing", e);
                postError(callback, "发布失败：" + e.getMessage());
            }
        });
    }
    
    /**
     * 点赞/取消点赞
     */
    public void toggleLike(int blessingId, final ApiCallback<InteractionResult> callback) {
        toggleInteraction(blessingId, "like", callback);
    }
    
    /**
     * 收藏/取消收藏
     */
    public void toggleFavorite(int blessingId, final ApiCallback<InteractionResult> callback) {
        toggleInteraction(blessingId, "favorite", callback);
    }
    
    private void toggleInteraction(int blessingId, String type, final ApiCallback<InteractionResult> callback) {
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("user_id", currentUserId);
                
                String urlStr = BASE_URL + "/" + blessingId + "/" + type;
                String response = httpPost(urlStr, payload);
                JSONObject json = new JSONObject(response);
                
                if (json.optBoolean("success", false)) {
                    JSONObject data = json.optJSONObject("data");
                    InteractionResult result = new InteractionResult();
                    result.active = data.optBoolean("active", false);
                    result.count = data.optInt("count", 0);
                    postSuccess(callback, result);
                } else {
                    postError(callback, json.optString("error", "操作失败"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error toggling " + type, e);
                postError(callback, "操作失败：" + e.getMessage());
            }
        });
    }
    
    /**
     * 获取评论列表
     */
    public void getComments(int blessingId, final ApiCallback<List<Comment>> callback) {
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "/" + blessingId + "/comments";
                String response = httpGet(urlStr);
                JSONObject json = new JSONObject(response);
                
                if (json.optBoolean("success", false)) {
                    JSONArray data = json.optJSONArray("data");
                    List<Comment> comments = new ArrayList<>();
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            Comment comment = Comment.fromJson(data.optJSONObject(i));
                            if (comment != null) {
                                comments.add(comment);
                            }
                        }
                    }
                    postSuccess(callback, comments);
                } else {
                    postError(callback, json.optString("error", "获取评论失败"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting comments", e);
                postError(callback, "网络错误：" + e.getMessage());
            }
        });
    }
    
    /**
     * 添加评论
     */
    public void addComment(int blessingId, String content, final ApiCallback<Comment> callback) {
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("user_id", currentUserId);
                payload.put("user_name", currentUserName);
                payload.put("content", content);
                
                String urlStr = BASE_URL + "/" + blessingId + "/comments";
                String response = httpPost(urlStr, payload);
                JSONObject json = new JSONObject(response);
                
                if (json.optBoolean("success", false)) {
                    Comment comment = Comment.fromJson(json.optJSONObject("data"));
                    postSuccess(callback, comment);
                } else {
                    postError(callback, json.optString("error", "评论失败"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding comment", e);
                postError(callback, "评论失败：" + e.getMessage());
            }
        });
    }
    
    /**
     * 获取统计数据
     */
    public void getStatistics(final ApiCallback<Statistics> callback) {
        executor.execute(() -> {
            try {
                String urlStr = BASE_URL + "/stats";
                String response = httpGet(urlStr);
                JSONObject json = new JSONObject(response);
                
                if (json.optBoolean("success", false)) {
                    Statistics stats = Statistics.fromJson(json.optJSONObject("data"));
                    postSuccess(callback, stats);
                } else {
                    postError(callback, json.optString("error", "获取统计失败"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting statistics", e);
                postError(callback, "网络错误：" + e.getMessage());
            }
        });
    }
    
    // ============== HTTP 工具方法 ==============
    
    private String httpGet(String urlStr) throws Exception {
        Log.d(TAG, "[HTTP GET] URL: " + urlStr);
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Log.d(TAG, "[HTTP GET] Connection opened");
        
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");
        Log.d(TAG, "[HTTP GET] Request method and headers set");
        
        int responseCode = conn.getResponseCode();
        Log.d(TAG, "[HTTP GET] Response code: " + responseCode);
        
        if (responseCode != 200) {
            Log.e(TAG, "[HTTP GET] Non-200 response code: " + responseCode);
            throw new Exception("HTTP error: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();
        
        Log.d(TAG, "[HTTP GET] Response received, length: " + response.length());
        return response.toString();
    }
    
    private String httpPost(String urlStr, JSONObject payload) throws Exception {
        Log.d(TAG, "[HTTP POST] URL: " + urlStr);
        Log.d(TAG, "[HTTP POST] Payload: " + payload.toString());
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Log.d(TAG, "[HTTP POST] Connection opened");
        
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        Log.d(TAG, "[HTTP POST] Request method and headers set");
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
            Log.d(TAG, "[HTTP POST] Request body sent, " + input.length + " bytes");
        }
        
        int responseCode = conn.getResponseCode();
        Log.d(TAG, "[HTTP POST] Response code: " + responseCode);
        
        if (responseCode != 200 && responseCode != 201) {
            Log.e(TAG, "[HTTP POST] Non-200/201 response code: " + responseCode);
            throw new Exception("HTTP error: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();
        
        Log.d(TAG, "[HTTP POST] Response received, length: " + response.length());
        return response.toString();
    }
    
    // ============== 线程切换工具 ==============
    
    private <T> void postSuccess(final ApiCallback<T> callback, final T data) {
        if (callback != null) {
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onSuccess(data));
        }
    }
    
    private void postError(final ApiCallback<?> callback, final String error) {
        if (callback != null) {
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onError(error));
        }
    }
    
    // ============== 辅助数据类 ==============
    
    public static class InteractionResult {
        public boolean active;
        public int count;
    }
    
    public static class Statistics {
        public int totalBlessings;
        public int totalComments;
        public java.util.Map<String, Integer> byCategory;
        
        public static Statistics fromJson(JSONObject json) {
            try {
                Statistics stats = new Statistics();
                stats.totalBlessings = json.optInt("total_blessings", 0);
                stats.totalComments = json.optInt("total_comments", 0);
                return stats;
            } catch (Exception e) {
                return new Statistics();
            }
        }
    }
}
