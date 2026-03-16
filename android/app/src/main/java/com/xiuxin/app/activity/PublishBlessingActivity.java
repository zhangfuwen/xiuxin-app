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
    private TextView fontSelector;
    
    // Toggle state
    private boolean isAdvancedExpanded = false;
    
    private final String[] categories = {"禅宗", "儒家", "道家", "佛经"};
    private int currentCategoryIndex = 0;
    
    // Fonts
    private static final String[] FONTS = {
        "默认字体",
        "洪磊板书体",
        "庞门正道真贵楷体",
        "前途笔锋手写体",
        "前途纤墨体",
        "三极行楷简体",
        "Tanugo Round",
        "杨任东竹体 Light",
        "演示小行楷",
        "真宗圣典楷书"
    };
    
    private static final String[] FONT_PATHS = {
        "",
        "fonts/hongleibanshujianti_2.ttf",
        "fonts/pangmenzhengdaozhenguikaiti_2.ttf",
        "fonts/qiantubifengshouxieti_2.ttf",
        "fonts/qiantuxianmoti_2.ttf",
        "fonts/sanjixingkaijianti_cu_2.ttf",
        "fonts/tanugo_round_regular.otf",
        "fonts/yangrendongzhushiti_light_2.ttf",
        "fonts/yanshixiaxingkai_2.ttf",
        "fonts/zhenzongshengdiankaishu.ttf"
    };
    
    private int currentFontIndex = 0;
    
    // Background images
    private static final String[] BG_NAMES = {
        "paper_2.jpg",
        "paper_3.jpg",
        "paper_low_1.jpg",
        "paper_low_2.jpg",
        "paper_low_3.jpg",
        "paper_low_4.png",
        "paper_low_5.png",
        "paper_low_6.png",
        "paper_low_7.png",
        "paper_low_8.png",
        "paper_low_9.png"
    };
    private static final String[] BG_PATHS = {
        "drawable/paper_2.jpg",
        "drawable/paper_3.jpg",
        "drawable/paper_low_1.jpg",
        "drawable/paper_low_2.jpg",
        "drawable/paper_low_3.jpg",
        "drawable/paper_low_4.png",
        "drawable/paper_low_5.png",
        "drawable/paper_low_6.png",
        "drawable/paper_low_7.png",
        "drawable/paper_low_8.png",
        "drawable/paper_low_9.png"
    };
    private int currentBgIndex = 0;
    
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
        fontSelector = findViewById(R.id.fontSelector);
        TextView bgSelector = findViewById(R.id.bgSelector);
        
        // Set default category
        categoryEdit.setText(categories[0]);
        categoryEdit.setEnabled(false); // Read-only, changed via button
        
        // Set default font
        updateFontSelector();
        
        // Set default background
        updateBgSelector();
        
        // Background selector click
        bgSelector.setOnClickListener(v -> cycleBackground());
        
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
        
        // Font selector - cycle through fonts
        fontSelector.setOnClickListener(v -> cycleFont());
        
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
    
    private void cycleFont() {
        currentFontIndex = (currentFontIndex + 1) % FONTS.length;
        updateFontSelector();
        Log.d(TAG, "Font changed to: " + FONTS[currentFontIndex]);
    }
    
    private void updateFontSelector() {
        fontSelector.setText("🎨 字体：" + FONTS[currentFontIndex]);
        
        // Preview font on text input
        if (currentFontIndex > 0) {
            try {
                android.graphics.Typeface typeface = android.graphics.Typeface.createFromAsset(getAssets(), FONT_PATHS[currentFontIndex]);
                editText.setTypeface(typeface);
            } catch (Exception e) {
                editText.setTypeface(null);
            }
        } else {
            editText.setTypeface(null);
        }
    }
    
    private void cycleBackground() {
        currentBgIndex = (currentBgIndex + 1) % BG_NAMES.length;
        updateBgSelector();
        Log.d(TAG, "Background changed to: " + BG_NAMES[currentBgIndex]);
    }
    
    private void updateBgSelector() {
        TextView bgSelector = findViewById(R.id.bgSelector);
        bgSelector.setText("🖼️ 背景：" + BG_NAMES[currentBgIndex]);
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
        String fontPath = FONT_PATHS[currentFontIndex];
        String bgPath = BG_PATHS[currentBgIndex];
        
        if (userName.isEmpty()) {
            userName = "匿名";
        }
        
        Log.d(TAG, "Publishing blessing: " + text.substring(0, Math.min(20, text.length())) + "... with font: " + fontPath + ", bg: " + bgPath);
        publishBlessing(text, source, practice, category, userName, fontPath, bgPath);
    }
    
    private void publishBlessing(String text, String source, String practice, String category, String userName, String fontPath, String bgPath) {
        // Disable publish button during request
        publishBtn.setEnabled(false);
        publishBtn.setText("发布中...");
        
        apiClient.publishBlessingWithFontAndBg(text, source, practice, category, fontPath, bgPath,
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
