package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

import java.util.Calendar;

public class PracticeFragment extends Fragment {

    private TextView preceptText, practiceStatus;
    private Button completeBtn;
    private LinearLayout preceptsList;
    private SharedPreferences prefs;

    private final String[] precepts = {
        "今日不杀生（包括不恶语）",
        "今日不偷盗（包括不占便宜）",
        "今日不邪淫（保持清净）",
        "今日不妄语（只说真话）",
        "今日不饮酒（保持清醒）",
        "今日素食一餐",
        "今日静坐 10 分钟",
        "今日行一善事",
        "今日感恩三人",
        "今日原谅一人"
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_practice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preceptText = view.findViewById(R.id.preceptText);
        practiceStatus = view.findViewById(R.id.practiceStatus);
        completeBtn = view.findViewById(R.id.completeBtn);
        preceptsList = view.findViewById(R.id.preceptsList);

        completeBtn.setOnClickListener(v -> completePractice());

        loadDailyPractice();
        populatePreceptsList();
    }

    private void loadDailyPractice() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        String todayPrecept = precepts[dayOfYear % precepts.length];

        preceptText.setText(todayPrecept);

        long lastPracticeDate = prefs.getLong("last_practice_date", 0);
        boolean completedToday = isSameDay(lastPracticeDate, System.currentTimeMillis());

        if (completedToday) {
            completeBtn.setText("已完成 ✓");
            completeBtn.setEnabled(false);
            practiceStatus.setText("✅ 今日功课已完成，随喜赞叹！");
        } else {
            completeBtn.setText("完成打卡");
            practiceStatus.setText("🙏 今日功课，请精进完成");
        }
    }

    private void completePractice() {
        prefs.edit()
            .putLong("last_practice_date", System.currentTimeMillis())
            .putInt("total_days", prefs.getInt("total_days", 0) + 1)
            .putInt("streak", calculateStreak())
            .apply();

        completeBtn.setText("已完成 ✓");
        completeBtn.setEnabled(false);
        practiceStatus.setText("✅ 今日功课已完成，随喜赞叹！");

        Toast.makeText(getContext(), "🙏 打卡成功，功德 +1", Toast.LENGTH_SHORT).show();
    }

    private void populatePreceptsList() {
        preceptsList.removeAllViews();

        for (int i = 0; i < precepts.length; i++) {
            TextView textView = new TextView(getContext());
            textView.setText(precepts[i]);
            textView.setTextSize(15);
            textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            textView.setPadding(16, 12, 16, 12);
            textView.setBackgroundResource(R.drawable.stat_card);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            textView.setLayoutParams(params);

            preceptsList.addView(textView);
        }
    }

    private boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private int calculateStreak() {
        long lastPracticeDate = prefs.getLong("last_practice_date", 0);
        if (lastPracticeDate == 0) return 1;

        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastPracticeDate);
        Calendar today = Calendar.getInstance();

        int daysDiff = (int) ((today.getTimeInMillis() - lastCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));

        if (daysDiff == 0 || daysDiff == 1) {
            return prefs.getInt("streak", 0) + 1;
        } else {
            return 1;
        }
    }
}
