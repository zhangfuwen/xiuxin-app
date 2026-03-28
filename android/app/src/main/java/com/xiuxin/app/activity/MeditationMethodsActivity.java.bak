package com.xiuxin.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.xiuxin.app.R;

/**
 * 冥想法门列表 Activity
 * 展示所有冥想方法，支持点击选择
 */
public class MeditationMethodsActivity extends AppCompatActivity {

    private static final String[] METHOD_NAMES = {"数息观", "随息观", "止观", "慈心观", "身体扫描", "呼吸法门"};
    private static final String[] METHOD_CATEGORIES = {"专注类", "专注类", "观照类", "慈心类", "身体类", "呼吸类"};
    private static final String[] METHOD_BRIEFS = {
        "数呼吸次数，从 1 数到 10",
        "觉察呼吸，不控制不干预",
        "止息妄念，观照当下",
        "发送慈爱给自己和他人",
        "觉察身体每个部位的感受",
        "跟随动画圆圈进行呼吸练习"
    };
    private static final String[] METHOD_GUIDES = {
        "1. 舒适坐姿，轻闭双眼\n2. 自然呼吸，吸气时数 1，呼气时数 2\n3. 数到 10 后重新开始\n4. 走神时温和地回到呼吸",
        "1. 放松身体，自然呼吸\n2. 觉察气息进出鼻孔的感觉\n3. 不控制呼吸节奏，只是观察\n4. 心念飘走时，温和地带回",
        "1. 先数息让心安静\n2. 放下数数，只是安住\n3. 观察念头的生起和消失\n4. 不跟随不排斥，如天空观云",
        "1. 先祝福自己：愿我平安快乐\n2. 祝福亲人：愿你平安快乐\n3. 祝福中立的人\n4. 祝福困难的人\n5. 祝福一切众生",
        "1. 从脚趾开始觉察\n2. 慢慢向上移动注意力\n3. 觉察每个部位的感受\n4. 不评判，只是觉察",
        "1. 吸气时圆圈放大\n2. 呼气时圆圈缩小\n3. 自然地跟随节奏\n4. 可选择不同呼吸模式"
    };
    private static final String[] METHOD_BENEFITS = {
        "培养专注力，平静心绪，适合初学者",
        "训练觉知力，减少执着，培养平等心",
        "开启智慧，照见实相，解脱烦恼",
        "培养慈悲心，减少嗔恨，改善人际关系",
        "放松身心，增强身体觉知，缓解压力",
        "放松身心，调节呼吸节奏"
    };
    private static final String[] METHOD_DIFFICULTIES = {"入门", "入门", "进阶", "入门", "入门", "入门"};
    public static final int[] METHOD_DURATIONS = {10, 15, 20, 15, 20, 10};

    private LinearLayout methodsContainer;
    private TextView emptyText;

    public static void startForResult(androidx.fragment.app.Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), MeditationMethodsActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_methods);

        // Bind views
        methodsContainer = findViewById(R.id.methodsContainer);
        emptyText = findViewById(R.id.emptyText);
        ImageButton closeBtn = findViewById(R.id.closeBtn);
        TextView titleText = findViewById(R.id.titleText);

        // Initialize methods list
        initializeMethods();

        // Close button
        closeBtn.setOnClickListener(v -> finish());

        // Title
        titleText.setText("📚 全部法门 (" + METHOD_NAMES.length + ")");
    }

    /**
     * Initialize meditation methods list
     */
    private void initializeMethods() {
        methodsContainer.removeAllViews();

        for (int i = 0; i < METHOD_NAMES.length; i++) {
            final int index = i;
            View methodView = getLayoutInflater().inflate(R.layout.item_method_simple, methodsContainer, false);

            TextView nameText = methodView.findViewById(R.id.methodName);
            TextView categoryText = methodView.findViewById(R.id.methodCategory);
            TextView briefText = methodView.findViewById(R.id.methodBrief);
            TextView durationText = methodView.findViewById(R.id.methodDuration);
            TextView difficultyText = methodView.findViewById(R.id.methodDifficulty);
            TextView benefitsText = methodView.findViewById(R.id.methodBenefits);
            TextView detailedText = methodView.findViewById(R.id.methodDetailed);
            View detailLayout = methodView.findViewById(R.id.detailLayout);
            View startBtn = methodView.findViewById(R.id.btnStartMethod);

            nameText.setText(METHOD_NAMES[i]);
            categoryText.setText(METHOD_CATEGORIES[i]);
            briefText.setText(METHOD_BRIEFS[i]);
            durationText.setText(METHOD_DURATIONS[i] + "分钟");
            difficultyText.setText(METHOD_DIFFICULTIES[i]);
            benefitsText.setText(METHOD_BENEFITS[i]);
            detailedText.setText(METHOD_GUIDES[i]);

            // Click to expand/collapse
            methodView.setOnClickListener(v -> {
                if (detailLayout.getVisibility() == View.VISIBLE) {
                    detailLayout.setVisibility(View.GONE);
                } else {
                    detailLayout.setVisibility(View.VISIBLE);
                }
            });

            // Start button click
            startBtn.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_method_index", index);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });

            methodsContainer.addView(methodView);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
