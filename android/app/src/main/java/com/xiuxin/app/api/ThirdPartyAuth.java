package com.xiuxin.app.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.xiuxin.app.model.User;

import org.json.JSONObject;

/**
 * 第三方登录认证
 * 支持：Google、微信、邮箱验证码
 */
public class ThirdPartyAuth {

    private static final String TAG = "ThirdPartyAuth";
    private static ThirdPartyAuth instance;
    private SharedPreferences prefs;
    
    // TODO: 在 gradle.properties 或本地配置中填写这些凭证
    // Google OAuth 2.0
    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";
    private static final String GOOGLE_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com";
    
    // 微信 OAuth 2.0
    private static final String WECHAT_APP_ID = "YOUR_WECHAT_APP_ID";
    private static final String WECHAT_APP_SECRET = "YOUR_WECHAT_APP_SECRET";
    
    // 邮箱验证码（需要后端服务）
    // 可以使用：SendGrid, Mailgun, 阿里云邮件推送等
    private static final String EMAIL_VERIFICATION_API = "https://your-backend.com/api/auth/send-code";

    public interface AuthCallback {
        void onSuccess(User user, String token);
        void onError(String error);
    }

    private ThirdPartyAuth(Context context) {
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
    }

    public static synchronized ThirdPartyAuth getInstance(Context context) {
        if (instance == null) {
            instance = new ThirdPartyAuth(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Google 登录
     * 需要使用 Google Sign-In SDK
     */
    public void loginWithGoogle(Context context, String idToken, AuthCallback callback) {
        // 验证 ID Token 并获取用户信息
        // 实际实现需要调用 GoogleSignInAccount.getIdToken()
        
        new Thread(() -> {
            try {
                // TODO: 调用后端验证 Google ID Token
                // POST /api/auth/google
                // Body: { "idToken": "xxx" }
                
                Log.d(TAG, "Google login with token: " + idToken.substring(0, 20) + "...");
                
                // 模拟成功（实际应该调用后端 API）
                User user = new User();
                user.id = "google_user_" + System.currentTimeMillis();
                user.name = "Google User";
                user.email = "user@gmail.com";
                user.avatar = "";
                user.provider = "google";
                
                String token = "mock_google_token_" + System.currentTimeMillis();
                
                // 保存登录状态
                saveLoginState(user, token);
                
                callback.onSuccess(user, token);
            } catch (Exception e) {
                Log.e(TAG, "Google login error", e);
                callback.onError("Google 登录失败：" + e.getMessage());
            }
        }).start();
    }

    /**
     * 微信登录 - 第一步：获取授权码
     * 需要使用微信 SDK 拉起微信授权页面
     */
    public void loginWithWeChatStep1(Context context, AuthCallback callback) {
        // TODO: 使用微信 SDK 发送登录请求
        // 实际实现需要：
        // 1. 集成微信 SDK (com.tencent.mm.opensdk:wechat-sdk-android)
        // 2. 调用 SendAuth.Req 拉起微信授权
        // 3. 在回调中获取 code
        
        Log.d(TAG, "WeChat login step 1: Request auth code");
        
        // 模拟：实际应该调用微信 SDK
        // IWXAPI api = WXAPIFactory.createWXAPI(context, WECHAT_APP_ID);
        // SendAuth.Req req = new SendAuth.Req();
        // req.scope = "snsapi_userinfo";
        // req.state = "xiuxin_app_login";
        // api.sendReq(req);
        
        callback.onError("微信登录需要先配置 AppID 并集成微信 SDK");
    }

    /**
     * 微信登录 - 第二步：用授权码换取 token
     */
    public void loginWithWeChatStep2(String code, AuthCallback callback) {
        new Thread(() -> {
            try {
                // TODO: 调用后端，后端用 code 换取 access_token
                // POST /api/auth/wechat
                // Body: { "code": "xxx" }
                
                Log.d(TAG, "WeChat login step 2: Exchange code for token");
                
                // 模拟成功
                User user = new User();
                user.id = "wechat_user_" + System.currentTimeMillis();
                user.name = "微信用户";
                user.email = "";
                user.avatar = "";
                user.provider = "wechat";
                
                String token = "mock_wechat_token_" + System.currentTimeMillis();
                
                saveLoginState(user, token);
                
                callback.onSuccess(user, token);
            } catch (Exception e) {
                Log.e(TAG, "WeChat login error", e);
                callback.onError("微信登录失败：" + e.getMessage());
            }
        }).start();
    }

    /**
     * 保存登录状态
     */
    private void saveLoginState(User user, String token) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_avatar", user.avatar)
            .putString("user_provider", user.provider)
            .putString("auth_token", token)
            .putBoolean("is_logged_in", true)
            .apply();
    }

    /**
     * 退出登录
     */
    public void logout() {
        prefs.edit()
            .clear()
            .apply();
        Log.d(TAG, "User logged out");
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        
        User user = new User();
        user.id = prefs.getString("user_id", "");
        user.name = prefs.getString("user_name", "");
        user.email = prefs.getString("user_email", "");
        user.avatar = prefs.getString("user_avatar", "");
        user.provider = prefs.getString("user_provider", "");
        return user;
    }

    /**
     * 发送邮箱验证码
     * 需要后端服务支持
     */
    public void sendEmailCode(String email, AuthCallback callback) {
        // 验证邮箱格式
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onError("请输入有效的邮箱地址");
            return;
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "Sending verification code to: " + email);
                
                // TODO: 调用后端 API 发送验证码
                // POST /api/auth/send-code
                // Body: { "email": "user@example.com" }
                
                // 模拟成功（实际应该调用后端 API）
                // 验证码应该发送到用户邮箱
                String mockCode = "123456";
                prefs.edit().putString("email_verify_code", mockCode).apply();
                
                callback.onSuccess(null, "验证码已发送到 " + email);
            } catch (Exception e) {
                Log.e(TAG, "Send email code error", e);
                callback.onError("发送失败：" + e.getMessage());
            }
        }).start();
    }

    /**
     * 邮箱验证码登录
     */
    public void loginWithEmail(String email, String code, AuthCallback callback) {
        // 验证邮箱格式
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onError("邮箱格式不正确");
            return;
        }
        
        // 验证验证码
        String savedCode = prefs.getString("email_verify_code", "");
        if (!savedCode.equals(code)) {
            callback.onError("验证码不正确，请重试");
            return;
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "Email login: " + email);
                
                // TODO: 调用后端验证并登录
                // POST /api/auth/email-login
                // Body: { "email": "user@example.com", "code": "123456" }
                
                // 创建用户
                User user = new User();
                user.id = "email_user_" + email.hashCode();
                user.name = email.split("@")[0]; // 使用邮箱前缀作为用户名
                user.email = email;
                user.avatar = "";
                user.provider = "email";
                
                String token = "mock_email_token_" + System.currentTimeMillis();
                
                saveLoginState(user, token);
                
                // 清除验证码
                prefs.edit().remove("email_verify_code").apply();
                
                callback.onSuccess(user, token);
            } catch (Exception e) {
                Log.e(TAG, "Email login error", e);
                callback.onError("登录失败：" + e.getMessage());
            }
        }).start();
    }

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        boolean googleConfigured = !GOOGLE_CLIENT_ID.equals("YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com");
        boolean wechatConfigured = !WECHAT_APP_ID.equals("YOUR_WECHAT_APP_ID");
        
        if (!googleConfigured) {
            Log.w(TAG, "Google OAuth not configured. Please set GOOGLE_CLIENT_ID");
        }
        if (!wechatConfigured) {
            Log.w(TAG, "WeChat OAuth not configured. Please set WECHAT_APP_ID");
        }
        
        // 邮箱登录不需要额外配置（需要后端支持）
        return true;
    }
}
