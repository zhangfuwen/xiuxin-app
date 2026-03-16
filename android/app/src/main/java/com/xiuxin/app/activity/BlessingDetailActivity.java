package com.xiuxin.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xiuxin.app.R;
import com.xiuxin.app.api.BlessingsApiClient;
import com.xiuxin.app.model.Comment;

import java.util.List;

public class BlessingDetailActivity extends AppCompatActivity {

    private TextView categoryTag, blessingText, blessingSource, blessingPractice;
    private Button likeBtn, favoriteBtn, shareBtn;
    private ImageButton closeBtn;
    private LinearLayout commentsSection;
    private TextView commentsTitle, commentsList;
    private SharedPreferences prefs;
    private BlessingsApiClient apiClient;
    private int blessingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blessing_detail);

        prefs = getSharedPreferences("xiuxin", MODE_PRIVATE);
        apiClient = BlessingsApiClient.getInstance();

        // Bind views
        categoryTag = findViewById(R.id.categoryTag);
        blessingText = findViewById(R.id.blessingText);
        blessingSource = findViewById(R.id.blessingSource);
        blessingPractice = findViewById(R.id.blessingPractice);
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
        blessingSource.setText("—— " + source);
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
