package com.xiuxin.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.xiuxin.app.MainActivity;
import com.xiuxin.app.R;
import com.xiuxin.app.api.ThirdPartyAuth;
import com.xiuxin.app.model.User;

/**
 * 登录页面
 * 支持：Google、微信登录
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    
    private Button btnGoogleLogin;
    private Button btnWeChatLogin;
    private Button btnEmailLogin;
    private Button btnSkipLogin;
    private TextView configWarning;
    
    private ThirdPartyAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        auth = ThirdPartyAuth.getInstance(this);
        
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnWeChatLogin = findViewById(R.id.btnWeChatLogin);
        btnEmailLogin = findViewById(R.id.btnEmailLogin);
        btnSkipLogin = findViewById(R.id.btnSkipLogin);
        configWarning = findViewById(R.id.configWarning);
        
        // 检查配置
        if (!auth.isConfigured()) {
            configWarning.setVisibility(View.VISIBLE);
            configWarning.setText("⚠️ 第三方登录尚未配置\n需要填写 Google Client ID 和微信 AppID");
        }
        
        // Google 登录按钮
        btnGoogleLogin.setOnClickListener(v -> {
            if (!auth.isConfigured()) {
                Toast.makeText(this, "请先配置 Google OAuth 凭证", Toast.LENGTH_LONG).show();
                return;
            }
            loginWithGoogle();
        });
        
        // 微信登录按钮
        btnWeChatLogin.setOnClickListener(v -> {
            if (!auth.isConfigured()) {
                Toast.makeText(this, "请先配置微信 OAuth 凭证", Toast.LENGTH_LONG).show();
                return;
            }
            loginWithWeChat();
        });
        
        // 邮箱登录按钮
        btnEmailLogin.setOnClickListener(v -> showEmailLoginDialog());
        
        // 跳过登录
        btnSkipLogin.setOnClickListener(v -> {
            // 以游客模式进入应用
            goToMain();
        });
    }
    
    private void loginWithGoogle() {
        // TODO: 集成 Google Sign-In SDK
        // 实际实现：
        // 1. 添加依赖：com.google.android.gms:play-services-auth
        // 2. 配置 GoogleSignInOptions
        // 3. 启动 IntentSenderForResult
        
        Log.d(TAG, "Google login requested");
        
        // 模拟登录（实际应该调用 Google Sign-In）
        auth.loginWithGoogle(this, "mock_id_token", new ThirdPartyAuth.AuthCallback() {
            @Override
            public void onSuccess(User user, String token) {
                Toast.makeText(LoginActivity.this, 
                    "Google 登录成功：欢迎 " + user.name, Toast.LENGTH_SHORT).show();
                goToMain();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void loginWithWeChat() {
        Log.d(TAG, "WeChat login requested");
        
        auth.loginWithWeChatStep1(this, new ThirdPartyAuth.AuthCallback() {
            @Override
            public void onSuccess(User user, String token) {
                Toast.makeText(LoginActivity.this, 
                    "微信登录成功：欢迎 " + user.name, Toast.LENGTH_SHORT).show();
                goToMain();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * 显示邮箱登录对话框
     */
    private void showEmailLoginDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email_login, null);
        
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText codeInput = dialogView.findViewById(R.id.codeInput);
        LinearLayout codeInputLayout = dialogView.findViewById(R.id.codeInputLayout);
        Button btnSendCode = dialogView.findViewById(R.id.btnSendCode);
        Button btnEmailLogin = dialogView.findViewById(R.id.btnEmailLogin);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView codeHint = dialogView.findViewById(R.id.codeHint);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // 发送验证码
        btnSendCode.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }
            
            btnSendCode.setEnabled(false);
            btnSendCode.setText("发送中...");
            
            auth.sendEmailCode(email, new ThirdPartyAuth.AuthCallback() {
                @Override
                public void onSuccess(User user, String result) {
                    btnSendCode.setText("已发送 (30s)");
                    codeInputLayout.setVisibility(View.VISIBLE);
                    codeHint.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
                    
                    // 倒计时
                    new android.os.Handler().postDelayed(() -> {
                        btnSendCode.setEnabled(true);
                        btnSendCode.setText("重新发送");
                    }, 30000);
                }
                
                @Override
                public void onError(String error) {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText("发送验证码");
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        });
        
        // 邮箱登录
        btnEmailLogin.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String code = codeInput.getText().toString().trim();
            
            if (email.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "请输入邮箱和验证码", Toast.LENGTH_SHORT).show();
                return;
            }
            
            auth.loginWithEmail(email, code, new ThirdPartyAuth.AuthCallback() {
                @Override
                public void onSuccess(User user, String token) {
                    Toast.makeText(LoginActivity.this, 
                        "登录成功：欢迎 " + user.name, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    goToMain();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        });
        
        // 取消
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            // TODO: 处理 Google Sign-In 结果
            Log.d(TAG, "Google sign-in result: " + resultCode);
        }
    }
}
