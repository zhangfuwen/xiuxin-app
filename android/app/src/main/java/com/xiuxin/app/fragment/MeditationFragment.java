package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

import java.util.Locale;

public class MeditationFragment extends Fragment {

    private TextView meditationTimer, meditationMode, meditationGuide;
    private TextView meditationProgress, customDuration;
    private TextView totalMinutes, totalSessions;
    private Button mode5minBtn, mode10minBtn, mode20minBtn;
    private Button startCustomBtn, stopMeditationBtn;
    private Button btnDecrease, btnIncrease;
    private SharedPreferences prefs;
    private Handler handler;
    private Runnable timerRunnable;
    private int secondsLeft;
    private int totalSeconds;
    private boolean isRunning = false;
    private int customMinutes = 15;

    // Meditation modes with guides
    private final String[] modeNames = {"数息观", "随息观", "止观", "慈心观", "身体扫描"};
    private final String[] modeGuides = {
        "专注呼吸，数息入定。吸气数 1，呼气数 2，数到 10 重新开始。",
        "随顺呼吸，不控制不干预。只是觉察气息的进出。",
        "止息妄念，观照当下。让心自然地安住。",
        "发送慈爱给自己和他人。愿我平安，愿你平安。",
        "从头到脚，觉察身体每个部位的感受。"
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meditation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        meditationTimer = view.findViewById(R.id.meditationTimer);
        meditationMode = view.findViewById(R.id.meditationMode);
        meditationGuide = view.findViewById(R.id.meditationGuide);
        meditationProgress = view.findViewById(R.id.meditationProgress);
        customDuration = view.findViewById(R.id.customDuration);
        totalMinutes = view.findViewById(R.id.totalMinutes);
        totalSessions = view.findViewById(R.id.totalSessions);
        
        mode5minBtn = view.findViewById(R.id.mode5minBtn);
        mode10minBtn = view.findViewById(R.id.mode10minBtn);
        mode20minBtn = view.findViewById(R.id.mode20minBtn);
        startCustomBtn = view.findViewById(R.id.startCustomBtn);
        stopMeditationBtn = view.findViewById(R.id.stopMeditationBtn);
        btnDecrease = view.findViewById(R.id.btnDecrease);
        btnIncrease = view.findViewById(R.id.btnIncrease);

        handler = new Handler();

        // Load stats
        loadStats();

        // Preset buttons - now cycle through modes
        mode5minBtn.setOnClickListener(v -> startMeditation(5, 0));
        mode10minBtn.setOnClickListener(v -> startMeditation(10, 1));
        mode20minBtn.setOnClickListener(v -> startMeditation(20, 2));
        
        // Custom duration controls
        btnDecrease.setOnClickListener(v -> {
            if (!isRunning && customMinutes > 1) {
                customMinutes--;
                updateCustomDurationDisplay();
            }
        });
        
        btnIncrease.setOnClickListener(v -> {
            if (!isRunning && customMinutes < 120) {
                customMinutes++;
                updateCustomDurationDisplay();
            }
        });
        
        startCustomBtn.setOnClickListener(v -> startMeditation(customMinutes, 3));
        stopMeditationBtn.setOnClickListener(v -> stopMeditation());

        updateCustomDurationDisplay();
    }

    private void updateCustomDurationDisplay() {
        customDuration.setText(customMinutes + "分钟");
    }

    private void startMeditation(int minutes, int modeIndex) {
        if (isRunning) return;

        isRunning = true;
        totalSeconds = minutes * 60;
        secondsLeft = totalSeconds;

        String modeName = modeNames[modeIndex];
        String guide = modeGuides[modeIndex];

        meditationMode.setText("🧘 " + modeName);
        meditationGuide.setText(guide);
        meditationTimer.setText(formatTime(secondsLeft));
        meditationProgress.setText("0%");
        stopMeditationBtn.setText("结束修习");
        stopMeditationBtn.setVisibility(View.VISIBLE);

        // Disable all buttons
        setAllButtonsEnabled(false);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || secondsLeft <= 0) return;

                secondsLeft--;
                meditationTimer.setText(formatTime(secondsLeft));
                
                // Update progress
                int progress = (int)((totalSeconds - secondsLeft) * 100.0 / totalSeconds);
                meditationProgress.setText(progress + "%");

                // Vibration at halfway point
                if (secondsLeft == totalSeconds / 2) {
                    vibrate(200);
                }

                if (secondsLeft > 0) {
                    handler.postDelayed(this, 1000);
                } else {
                    meditationComplete(modeName);
                }
            }
        };

        handler.post(timerRunnable);
    }

    private void meditationComplete(String modeName) {
        isRunning = false;
        
        // Save stats
        int minutes = prefs.getInt("meditation_minutes", 0) + totalSeconds / 60;
        int sessions = prefs.getInt("meditation_sessions", 0) + 1;
        prefs.edit()
            .putInt("meditation_minutes", minutes)
            .putInt("meditation_sessions", sessions)
            .apply();

        // Vibration completion
        vibrate(new long[]{0, 200, 100, 200, 100, 300}, -1);

        meditationTimer.setText("完成!");
        meditationMode.setText("🙏 修习圆满");
        meditationGuide.setText("随喜赞叹你的修习功德");
        meditationProgress.setText("100%");
        stopMeditationBtn.setText("关闭");

        Toast.makeText(getContext(), "🙏 " + modeName + " 完成 • 随喜功德", Toast.LENGTH_LONG).show();
        
        loadStats();
    }

    private void stopMeditation() {
        if (!isRunning && stopMeditationBtn.getText().toString().equals("关闭")) {
            // Reset after completion
            resetUI();
            return;
        }

        isRunning = false;
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        // Save partial progress
        int completedSeconds = totalSeconds - secondsLeft;
        if (completedSeconds > 30) { // Only save if more than 30 seconds
            int minutes = prefs.getInt("meditation_minutes", 0) + completedSeconds / 60;
            int sessions = prefs.getInt("meditation_sessions", 0) + 1;
            prefs.edit()
                .putInt("meditation_minutes", minutes)
                .putInt("meditation_sessions", sessions)
                .apply();
        }

        meditationTimer.setText(formatTime(secondsLeft));
        meditationMode.setText("已暂停");
        meditationGuide.setText("随时可以继续修习");
        meditationProgress.setText("");
        stopMeditationBtn.setVisibility(View.GONE);
        setAllButtonsEnabled(true);

        loadStats();
    }

    private void resetUI() {
        meditationTimer.setText("05:00");
        meditationMode.setText("选择冥想模式");
        meditationGuide.setText("点击下方按钮开始修习");
        meditationProgress.setText("");
        stopMeditationBtn.setVisibility(View.GONE);
        setAllButtonsEnabled(true);
    }

    private void setAllButtonsEnabled(boolean enabled) {
        mode5minBtn.setEnabled(enabled);
        mode10minBtn.setEnabled(enabled);
        mode20minBtn.setEnabled(enabled);
        startCustomBtn.setEnabled(enabled);
        btnDecrease.setEnabled(enabled);
        btnIncrease.setEnabled(enabled);
        
        mode5minBtn.setAlpha(enabled ? 1.0f : 0.5f);
        mode10minBtn.setAlpha(enabled ? 1.0f : 0.5f);
        mode20minBtn.setAlpha(enabled ? 1.0f : 0.5f);
        startCustomBtn.setAlpha(enabled ? 1.0f : 0.5f);
        btnDecrease.setAlpha(enabled ? 1.0f : 0.5f);
        btnIncrease.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void loadStats() {
        int minutes = prefs.getInt("meditation_minutes", 0);
        int sessions = prefs.getInt("meditation_sessions", 0);
        totalMinutes.setText(String.valueOf(minutes));
        totalSessions.setText(String.valueOf(sessions));
    }

    private void vibrate(long milliseconds) {
        try {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(milliseconds);
            }
        } catch (Exception e) {
            // Ignore vibration errors
        }
    }

    private void vibrate(long[] pattern, int repeat) {
        try {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(pattern, repeat);
            }
        } catch (Exception e) {
            // Ignore vibration errors
        }
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isRunning) {
            stopMeditation();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
