package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

public class StatsFragment extends Fragment {

    private TextView totalDaysText, streakText, meditationMinutesText;
    private LinearLayout activityList;
    private SharedPreferences prefs;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        totalDaysText = view.findViewById(R.id.totalDaysText);
        streakText = view.findViewById(R.id.streakText);
        meditationMinutesText = view.findViewById(R.id.meditationMinutesText);
        activityList = view.findViewById(R.id.activityList);

        loadStats();
        populateActivityList();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        int totalDays = prefs.getInt("total_days", 0);
        int streak = prefs.getInt("streak", 0);
        int meditationMinutes = prefs.getInt("meditation_minutes", 0);

        totalDaysText.setText(String.valueOf(totalDays));
        streakText.setText(String.valueOf(streak));
        meditationMinutesText.setText(String.valueOf(meditationMinutes));
    }

    private void populateActivityList() {
        activityList.removeAllViews();

        String[] activities = {
            "🧘 呼吸练习 - 2 分钟",
            "⏱️ 数息观 - 5 分钟",
            "✅ 今日功课打卡",
            "📿 念诵加持语"
        };

        for (String activity : activities) {
            TextView textView = new TextView(getContext());
            textView.setText(activity);
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

            activityList.addView(textView);
        }
    }
}
