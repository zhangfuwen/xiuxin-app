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
    private View breathingCircle, breathingCircleOuter;
    private SharedPreferences prefs;
    private Handler handler;
    private Runnable timerRunnable;
    private Runnable breathingRunnable;
    private int secondsLeft;
    private int totalSeconds;
    private boolean isRunning = false;
    private int customMinutes = 15;
    private int breathingPhase = 0;
    private int breathCount = 0;
    private boolean isBreathingMode = false;
    private int currentBreathingMode = 0; // 0: 4-7-8, 1: Box, 2: Coherent, 3: Custom

    // Meditation modes with guides
    private final String[] modeNames = {"数息观", "随息观", "止观", "慈心观", "身体扫描", "呼吸法门"};
    private final String[] modeGuides = {
        "专注呼吸，数息入定。吸气数 1，呼气数 2，数到 10 重新开始。",
        "随顺呼吸，不控制不干预。只是觉察气息的进出。",
        "止息妄念，观照当下。让心自然地安住。",
        "发送慈爱给自己和他人。愿我平安，愿你平安。",
        "从头到脚，觉察身体每个部位的感受。",
        "跟随动画圆圈进行呼吸练习。吸气时圆圈放大，呼气时缩小。"
    };
    
    // Breathing patterns for 呼吸法门 mode: {inhale, hold, exhale, hold_after} in milliseconds
    private final int[][] breathingPatterns = {
        {4000, 7000, 8000, 0},      // 4-7-8
        {4000, 4000, 4000, 4000},   // Box breathing
        {5000, 0, 5000, 0},         // Coherent breathing
        {4000, 4000, 4000, 0}       // Custom
    };
    private final String[] breathingModeNames = {"4-7-8 放松法", "方箱呼吸法", "共振呼吸法", "自定义呼吸法"};

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
        breathingCircle = view.findViewById(R.id.breathingCircle);
        breathingCircleOuter = view.findViewById(R.id.breathingCircleOuter);
        
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

        // Check if this is breathing mode (index 5)
        if (modeIndex == 5) {
            startBreathingMode(minutes);
            return;
        }

        isRunning = true;
        isBreathingMode = false;
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
        
        // Hide breathing circle for normal meditation
        if (breathingCircle != null) {
            breathingCircle.setVisibility(View.GONE);
            breathingCircleOuter.setVisibility(View.GONE);
        }

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
    
    /**
     * Start breathing mode with animation
     */
    private void startBreathingMode(int minutes) {
        isRunning = true;
        isBreathingMode = true;
        totalSeconds = minutes * 60;
        secondsLeft = totalSeconds;
        breathingPhase = 0;
        breathCount = 0;
        
        // Show breathing circle
        if (breathingCircle != null) {
            breathingCircle.setVisibility(View.VISIBLE);
            breathingCircleOuter.setVisibility(View.VISIBLE);
        }
        
        meditationMode.setText("🌬️ 呼吸法门");
        meditationGuide.setText("跟随圆圈节奏：放大时吸气，缩小时呼气");
        meditationTimer.setText(formatTime(secondsLeft));
        meditationProgress.setText("0 次呼吸");
        stopMeditationBtn.setText("结束练习");
        stopMeditationBtn.setVisibility(View.VISIBLE);
        
        setAllButtonsEnabled(false);
        
        // Start breathing animation
        breathingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || !isBreathingMode) return;
                
                int[] pattern = breathingPatterns[currentBreathingMode];
                String[] phases = {"吸气", "屏息", "呼气", "屏息"};
                
                // Update instruction
                meditationGuide.setText(phases[breathingPhase]);
                
                // Animate circle
                animateBreathingCircle(breathingPhase == 0, pattern[breathingPhase]);
                
                // Count breaths (after inhale completes)
                if (breathingPhase == 0) {
                    breathCount++;
                    meditationProgress.setText(breathCount + " 次呼吸");
                }
                
                // Move to next phase
                breathingPhase = (breathingPhase + 1) % 4;
                
                // Schedule next phase
                handler.postDelayed(this, pattern[breathingPhase]);
            }
        };
        
        handler.post(breathingRunnable);
        
        // Also run timer for countdown
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || secondsLeft <= 0) return;
                
                secondsLeft--;
                meditationTimer.setText(formatTime(secondsLeft));
                
                if (secondsLeft > 0) {
                    handler.postDelayed(this, 1000);
                } else {
                    meditationComplete("呼吸法门");
                }
            }
        };
        
        handler.post(timerRunnable);
    }
    
    private void animateBreathingCircle(boolean expand, int duration) {
        if (breathingCircle == null) return;
        
        // Scale animation
        android.view.animation.ScaleAnimation scaleAnim = new android.view.animation.ScaleAnimation(
            expand ? 0.7f : 1.0f, expand ? 1.0f : 0.7f,
            expand ? 0.7f : 1.0f, expand ? 1.0f : 0.7f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(duration);
        scaleAnim.setFillAfter(true);
        breathingCircle.startAnimation(scaleAnim);
        
        // Alpha animation for outer circle
        android.view.animation.AlphaAnimation alphaAnim = new android.view.animation.AlphaAnimation(
            expand ? 0.5f : 1.0f, expand ? 1.0f : 0.5f
        );
        alphaAnim.setDuration(duration);
        if (breathingCircleOuter != null) {
            breathingCircleOuter.startAnimation(alphaAnim);
        }
    }

    private void meditationComplete(String modeName) {
        isRunning = false;
        isBreathingMode = false;
        
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
        if (breathCount > 0) {
            meditationProgress.setText(breathCount + " 次呼吸");
        } else {
            meditationProgress.setText("100%");
        }
        stopMeditationBtn.setText("关闭");
        
        // Hide breathing circle
        if (breathingCircle != null) {
            breathingCircle.setVisibility(View.GONE);
            breathingCircleOuter.setVisibility(View.GONE);
        }

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
        isBreathingMode = false;
        
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        if (breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
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
        if (breathCount > 0) {
            meditationProgress.setText(breathCount + " 次呼吸");
        } else {
            meditationProgress.setText("");
        }
        stopMeditationBtn.setVisibility(View.GONE);
        
        // Hide breathing circle
        if (breathingCircle != null) {
            breathingCircle.setVisibility(View.GONE);
            breathingCircleOuter.setVisibility(View.GONE);
        }
        
        setAllButtonsEnabled(true);

        loadStats();
    }

    private void resetUI() {
        meditationTimer.setText("05:00");
        meditationMode.setText("选择冥想模式");
        meditationGuide.setText("点击下方按钮开始修习");
        meditationProgress.setText("");
        stopMeditationBtn.setVisibility(View.GONE);
        
        // Hide breathing circle
        if (breathingCircle != null) {
            breathingCircle.setVisibility(View.GONE);
            breathingCircleOuter.setVisibility(View.GONE);
        }
        
        setAllButtonsEnabled(true);
        isBreathingMode = false;
        breathCount = 0;
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
        breathingRunnable = null;
        timerRunnable = null;
    }
}
