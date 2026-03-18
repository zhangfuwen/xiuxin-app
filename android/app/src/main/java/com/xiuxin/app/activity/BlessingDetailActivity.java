package com.xiuxin.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.xiuxin.app.R;
import com.xiuxin.app.api.BlessingsApiClient;
import com.xiuxin.app.model.Comment;

import java.util.List;
import java.util.Random;

public class BlessingDetailActivity extends AppCompatActivity {

    private TextView categoryTag, blessingText, blessingTextPlain, blessingSource, blessingPractice;
    private ImageView cardBackground;
    private Button likeBtn, favoriteBtn, shareBtn;
    private ImageButton closeBtn;
    private LinearLayout commentsSection;
    private TextView commentsTitle, commentsList;
    private SharedPreferences prefs;
    private BlessingsApiClient apiClient;
    private int blessingId;
    private final Random random = new Random();
    
    // 背景图资源 ID 列表
    private static final int[] BG_RESOURCE_IDS = {
        R.drawable.paper_2,
        R.drawable.paper_3,
        R.drawable.paper_low_1,
        R.drawable.paper_low_2,
        R.drawable.paper_low_3,
        R.drawable.paper_low_4,
        R.drawable.paper_low_5,
        R.drawable.paper_low_6,
        R.drawable.paper_low_7,
        R.drawable.paper_low_8,
        R.drawable.paper_low_9
    };
    
    // 中文字体列表
    private static final String[] CHINESE_FONTS = {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blessing_detail);

        prefs = getSharedPreferences("xiuxin", MODE_PRIVATE);
        apiClient = BlessingsApiClient.getInstance();

        // Bind views
        categoryTag = findViewById(R.id.categoryTag);
        blessingText = findViewById(R.id.blessingText);
        blessingTextPlain = findViewById(R.id.blessingTextPlain);
        blessingSource = findViewById(R.id.blessingSource);
        blessingPractice = findViewById(R.id.blessingPractice);
        cardBackground = findViewById(R.id.cardBackground);
        likeBtn = findViewById(R.id.likeBtn);
        favoriteBtn = findViewById(R.id.favoriteBtn);
        shareBtn = findViewById(R.id.shareBtn);
        closeBtn = findViewById(R.id.closeBtn);
        commentsSection = findViewById(R.id.commentsSection);
        commentsTitle = findViewById(R.id.commentsTitle);
        commentsList = findViewById(R.id.commentsList);

        // Get data from intent
        String text = getIntent().getStringExtra("text");
        String source = getIntent().getStringExtra("source");
        String practice = getIntent().getStringExtra("practice");
        String category = getIntent().getStringExtra("category");
        blessingId = getIntent().getIntExtra("id", 0);

        // Display data
        categoryTag.setText(category);
        blessingText.setText(text);
        blessingTextPlain.setText(text);
        blessingSource.setText("—— " + source);
        
        // Load random background image
        int bgResId = BG_RESOURCE_IDS[random.nextInt(BG_RESOURCE_IDS.length)];
        Glide.with(this)
            .load(bgResId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(cardBackground);
        
        // Apply random font to both card text and plain text
        try {
            String randomFont = CHINESE_FONTS[random.nextInt(CHINESE_FONTS.length)];
            android.graphics.Typeface typeface = android.graphics.Typeface.createFromAsset(getAssets(), randomFont);
            blessingText.setTypeface(typeface);
            blessingTextPlain.setTypeface(typeface);
        } catch (Exception e) {
            // If font loading fails, use default font
            blessingText.setTypeface(null);
            blessingTextPlain.setTypeface(null);
        }
        if (practice != null && !practice.trim().isEmpty()) {
            blessingPractice.setText("💡 " + practice);
            blessingPractice.setVisibility(android.view.View.VISIBLE);
        } else {
            blessingPractice.setVisibility(android.view.View.GONE);
        }
        
        // Load comments
        loadComments();

        // Close button
        closeBtn.setOnClickListener(v -> finish());

        // Like button
        likeBtn.setOnClickListener(v -> {
            Toast.makeText(this, "❤️ 感恩", Toast.LENGTH_SHORT).show();
        });

        // Favorite button
        favoriteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "⭐ 已收藏", Toast.LENGTH_SHORT).show();
        });

        // Share button
        shareBtn.setOnClickListener(v -> {
            String shareText = text + "\n\n—— " + source + "\n\n💡 " + practice;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "分享正念"));
        });
    }
    
    /**
     * 加载评论
     */
    private void loadComments() {
        if (blessingId <= 0) {
            commentsSection.setVisibility(android.view.View.GONE);
            return;
        }
        
        apiClient.getComments(blessingId, new BlessingsApiClient.ApiCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (comments != null && !comments.isEmpty()) {
                    commentsSection.setVisibility(android.view.View.VISIBLE);
                    commentsTitle.setText("💬 评论 (" + comments.size() + ")");
                    
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < comments.size(); i++) {
                        Comment c = comments.get(i);
                        sb.append(c.userName).append(": ").append(c.content);
                        if (i < comments.size() - 1) {
                            sb.append("\n\n");
                        }
                    }
                    commentsList.setText(sb.toString());
                } else {
                    commentsSection.setVisibility(android.view.View.VISIBLE);
                    commentsTitle.setText("💬 评论 (0)");
                    commentsList.setText("暂无评论，快来抢沙发吧~");
                }
            }
            
            @Override
            public void onError(String error) {
                commentsSection.setVisibility(android.view.View.VISIBLE);
                commentsTitle.setText("💬 评论");
                commentsList.setText("评论加载失败，请稍后重试");
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
