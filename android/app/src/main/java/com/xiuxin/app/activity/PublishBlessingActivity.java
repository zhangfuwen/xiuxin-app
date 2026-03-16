package com.xiuxin.app.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xiuxin.app.R;
import com.xiuxin.app.api.BlessingsApiClient;
import com.xiuxin.app.model.Blessing;

/**
 * 发布禅语 Activity - 简洁优雅的发布界面
 */
public class PublishBlessingActivity extends AppCompatActivity {
    
    private static final String TAG = "PublishBlessingActivity";
    
    // Main input
    private EditText editText;
    
    // Advanced fields (hidden by default)
    private LinearLayout advancedFieldsLayout;
    private EditText sourceEdit;
    private EditText practiceEdit;
    private EditText categoryEdit;
    private EditText nameEdit;
    
    // Buttons
    private Button moreBtn;
    private Button publishBtn;
    private Button cancelBtn;
    
    // Toggle state
    private boolean isAdvancedExpanded = false;
    
    private final String[] categories = {"禅宗", "儒家", "道家", "佛经"};
    private int currentCategoryIndex = 0;
    
    private BlessingsApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_blessing);
        
        Log.d(TAG, "=== PublishBlessingActivity created ===");
        
        apiClient = BlessingsApiClient.getInstance();
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        // Main input
        editText = findViewById(R.id.editText);
        
        // Advanced fields
        advancedFieldsLayout = findViewById(R.id.advancedFieldsLayout);
        sourceEdit = findViewById(R.id.sourceEdit);
        practiceEdit = findViewById(R.id.practiceEdit);
        categoryEdit = findViewById(R.id.categoryEdit);
        nameEdit = findViewById(R.id.nameEdit);
        
        // Set default category
        categoryEdit.setText(categories[0]);
        categoryEdit.setEnabled(false); // Read-only, changed via button
        
        // Buttons
        moreBtn = findViewById(R.id.moreBtn);
        publishBtn = findViewById(R.id.publishBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        
        // Hide advanced fields initially
        advancedFieldsLayout.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        // More button - toggle advanced fields
        moreBtn.setOnClickListener(v -> toggleAdvancedFields());
        
        // Category button - cycle through categories
        categoryEdit.setOnClickListener(v -> cycleCategory());
        
        // Cancel button
        cancelBtn.setOnClickListener(v -> finish());
        
        // Publish button
        publishBtn.setOnClickListener(v -> validateAndPublish());
    }
    
    private void toggleAdvancedFields() {
        isAdvancedExpanded = !isAdvancedExpanded;
        
        if (isAdvancedExpanded) {
            advancedFieldsLayout.setVisibility(View.VISIBLE);
            moreBtn.setText("收起");
            Log.d(TAG, "Advanced fields expanded");
        } else {
            advancedFieldsLayout.setVisibility(View.GONE);
            moreBtn.setText("更多");
            Log.d(TAG, "Advanced fields collapsed");
        }
    }
    
    private void cycleCategory() {
        currentCategoryIndex = (currentCategoryIndex + 1) % categories.length;
        categoryEdit.setText(categories[currentCategoryIndex]);
        Log.d(TAG, "Category changed to: " + categories[currentCategoryIndex]);
    }
    
    private void validateAndPublish() {
        String text = editText.getText().toString().trim();
        
        if (text.isEmpty()) {
            editText.setError("请输入禅语内容");
            editText.requestFocus();
            return;
        }
        
        String source = sourceEdit.getText().toString().trim();
        String practice = practiceEdit.getText().toString().trim();
        String category = categoryEdit.getText().toString().trim();
        String userName = nameEdit.getText().toString().trim();
        
        if (userName.isEmpty()) {
            userName = "匿名";
        }
        
        Log.d(TAG, "Publishing blessing: " + text.substring(0, Math.min(20, text.length())) + "...");
        publishBlessing(text, source, practice, category, userName);
    }
    
    private void publishBlessing(String text, String source, String practice, String category, String userName) {
        // Disable publish button during request
        publishBtn.setEnabled(false);
        publishBtn.setText("发布中...");
        
        apiClient.publishBlessing(text, source, practice, category, 
            new BlessingsApiClient.ApiCallback<Blessing>() {
                @Override
                public void onSuccess(Blessing blessing) {
                    publishBtn.setEnabled(true);
                    publishBtn.setText("发布");
                    
                    Toast.makeText(PublishBlessingActivity.this, "发布成功！", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Publish success: " + blessing.id);
                    finish();
                }
                
                @Override
                public void onError(String error) {
                    publishBtn.setEnabled(true);
                    publishBtn.setText("发布");
                    
                    Toast.makeText(PublishBlessingActivity.this, "发布失败：" + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Publish error: " + error);
                }
            });
    }
}
