package com.xiuxin.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xiuxin.app.R;

public class BlessingDetailActivity extends AppCompatActivity {

    private TextView categoryTag, blessingText, blessingSource, blessingPractice;
    private Button likeBtn, favoriteBtn, shareBtn;
    private ImageButton closeBtn;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blessing_detail);

        prefs = getSharedPreferences("xiuxin", MODE_PRIVATE);

        // Bind views
        categoryTag = findViewById(R.id.categoryTag);
        blessingText = findViewById(R.id.blessingText);
        blessingSource = findViewById(R.id.blessingSource);
        blessingPractice = findViewById(R.id.blessingPractice);
        likeBtn = findViewById(R.id.likeBtn);
        favoriteBtn = findViewById(R.id.favoriteBtn);
        shareBtn = findViewById(R.id.shareBtn);
        closeBtn = findViewById(R.id.closeBtn);

        // Get data from intent
        String text = getIntent().getStringExtra("text");
        String source = getIntent().getStringExtra("source");
        String practice = getIntent().getStringExtra("practice");
        String category = getIntent().getStringExtra("category");

        // Display data
        categoryTag.setText(category);
        blessingText.setText(text);
        blessingSource.setText("—— " + source);
        blessingPractice.setText(practice);

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

    @Override
    public void onBackPressed() {
        finish();
    }
}
