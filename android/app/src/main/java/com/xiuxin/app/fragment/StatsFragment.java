package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class StatsFragment extends Fragment {

    private TextView totalDaysText, streakText, meditationMinutesText;
    private LinearLayout activityList;
    private GridLayout calendarGrid;
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
        calendarGrid = view.findViewById(R.id.calendarGrid);

        loadStats();
        renderCalendar();
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
    
    /**
     * 渲染日历
     */
    private void renderCalendar() {
        calendarGrid.removeAllViews();
        
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        
        // Set to first day of current month
        calendar.set(year, month, 1);
        
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Get practiced days from SharedPreferences
        Set<String> practicedDays = prefs.getStringSet("practiced_days", new HashSet<>());
        
        // Add empty cells for days before the first day of month
        for (int i = 1; i < firstDayOfWeek; i++) {
            addEmptyDayToGrid();
        }
        
        // Add day cells
        for (int day = 1; day <= daysInMonth; day++) {
            String dateKey = year + "-" + (month + 1) + "-" + day;
            boolean isPracticed = practicedDays.contains(dateKey);
            boolean isToday = (day == calendar.get(Calendar.DAY_OF_MONTH));
            
            addDayToGrid(day, isPracticed, isToday);
        }
    }
    
    /**
     * 添加空白格子
     */
    private void addEmptyDayToGrid() {
        View emptyView = new View(getContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        params.height = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        params.setMargins(4, 4, 4, 4);
        emptyView.setLayoutParams(params);
        calendarGrid.addView(emptyView);
    }
    
    /**
     * 添加日期格子
     */
    private void addDayToGrid(int day, boolean isPracticed, boolean isToday) {
        TextView dayText = new TextView(getContext());
        dayText.setText(String.valueOf(day));
        dayText.setTextSize(12);
        dayText.setGravity(Gravity.CENTER);
        
        // Create background drawable
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setSize(32, 32);
        
        if (isPracticed) {
            background.setColor(getResources().getColor(R.color.practiced_color, null));
            dayText.setTextColor(Color.WHITE);
            dayText.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (isToday) {
            background.setColor(getResources().getColor(R.color.today_color, null));
            background.setStroke(2, getResources().getColor(R.color.today_stroke_color, null));
            dayText.setTextColor(getResources().getColor(R.color.today_text_color, null));
        } else {
            background.setColor(Color.TRANSPARENT);
            dayText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
        
        dayText.setBackground(background);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        params.height = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        params.setMargins(4, 4, 4, 4);
        dayText.setLayoutParams(params);
        
        calendarGrid.addView(dayText);
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
