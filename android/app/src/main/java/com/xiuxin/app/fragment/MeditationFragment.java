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
import com.xiuxin.app.activity.MeditationMethodsActivity;
import com.xiuxin.app.model.MeditationMethod;

import java.util.Locale;

public class MeditationFragment extends Fragment {

    private TextView meditationTimer, meditationMode, meditationGuide;
    private TextView meditationProgress, customDuration;
    private TextView totalMinutes, totalSessions;
    private Button btnSelectMethodTop, startCustomBtn, stopMeditationBtn;
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
    private int selectedMethodIndex = -1; // Currently selected method index
    private static final int REQUEST_SELECT_METHOD = 1001;

    // Meditation methods data
    private final String[] methodNames = {"数息观", "随息观", "止观", "慈心观", "身体扫描", "呼吸法门"};
    private final String[] methodGuides = {
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
        btnSelectMethodTop = view.findViewById(R.id.btnSelectMethodTop);
        startCustomBtn = view.findViewById(R.id.startCustomBtn);
        stopMeditationBtn = view.findViewById(R.id.stopMeditationBtn);
        btnDecrease = view.findViewById(R.id.btnDecrease);
        btnIncrease = view.findViewById(R.id.btnIncrease);

        handler = new Handler();

        // Select method button - triggers method selection activity
        View.OnClickListener selectMethodListener = v -> {
            MeditationMethodsActivity.startForResult(MeditationFragment.this, REQUEST_SELECT_METHOD);
        };
        if (btnSelectMethodTop != null) {
            btnSelectMethodTop.setOnClickListener(selectMethodListener);
        }

        // Load stats
        loadStats();
        
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
        
        startCustomBtn.setOnClickListener(v -> {
            if (selectedMethodIndex >= 0) {
                startMeditation(customMinutes, selectedMethodIndex);
            } else {
                Toast.makeText(getContext(), "请先选择一个法门", Toast.LENGTH_SHORT).show();
            }
        });
        stopMeditationBtn.setOnClickListener(v -> stopMeditation());

        updateCustomDurationDisplay();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_METHOD && resultCode == getActivity().RESULT_OK && data != null) {
            int methodIndex = data.getIntExtra("selected_method_index", -1);
            if (methodIndex >= 0 && methodIndex < methodNames.length) {
                selectMethod(methodIndex);
            }
        }
    }
    
    /**
     * Select a meditation method
     */
    private void selectMethod(int methodIndex) {
        if (isRunning) return;
        
        selectedMethodIndex = methodIndex;
        meditationMode.setText("🧘 " + methodNames[methodIndex]);
        meditationGuide.setText(methodGuides[methodIndex]);
        startCustomBtn.setEnabled(true);
        
        // Hide the select button after selection
        if (btnSelectMethodTop != null) {
            btnSelectMethodTop.setVisibility(View.GONE);
        }
        
        // Show breathing info if breathing mode
        if (methodIndex == 5) {
            meditationGuide.setText("跟随动画圆圈进行呼吸练习。吸气时圆圈放大，呼气时缩小。");
        }
    }

    private void updateCustomDurationDisplay() {
        customDuration.setText(customMinutes + "分钟");
    }

    private void startMeditation(int minutes, int methodIndex) {
        if (isRunning) return;
        
        isRunning = true;
        isBreathingMode = (methodIndex == 5); // 呼吸法门 is index 5
        secondsLeft = minutes * 60;
        totalSeconds = secondsLeft;
        
        // Update UI
        setControlsEnabled(false);
        startCustomBtn.setVisibility(View.GONE);
        stopMeditationBtn.setVisibility(View.VISIBLE);
        
        if (isBreathingMode) {
            // Show breathing circle
            breathingCircle.setVisibility(View.VISIBLE);
            breathingCircleOuter.setVisibility(View.VISIBLE);
            meditationGuide.setText("跟随圆圈呼吸：放大时吸气，缩小时呼气");
            startBreathingAnimation();
        } else {
            meditationGuide.setText(methodGuides[methodIndex]);
        }
        
        updateTimerDisplay();
        
        // Start timer
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (secondsLeft > 0) {
                    secondsLeft--;
                    updateTimerDisplay();
                    handler.postDelayed(this, 1000);
                } else {
                    completeMeditation();
                }
            }
        };
        handler.post(timerRunnable);
        
        // Save start time
        prefs.edit().putLong("meditation_start_time", System.currentTimeMillis()).apply();
    }

    private void startBreathingAnimation() {
        int[] pattern = breathingPatterns[currentBreathingMode];
        int inhaleMs = pattern[0];
        int holdMs = pattern[1];
        int exhaleMs = pattern[2];
        int holdAfterMs = pattern[3];
        
        breathingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || !isBreathingMode) return;
                
                // Inhale - expand
                breathingCircle.animate().scaleX(1.3f).scaleY(1.3f).setDuration(inhaleMs).withEndAction(() -> {
                    if (!isRunning) return;
                    
                    // Hold after inhale
                    if (holdMs > 0) {
                        handler.postDelayed(() -> {
                            if (!isRunning) return;
                            
                            // Exhale - contract
                            breathingCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(exhaleMs).withEndAction(() -> {
                                if (!isRunning) return;
                                
                                // Hold after exhale
                                if (holdAfterMs > 0) {
                                    handler.postDelayed(this, holdAfterMs);
                                } else {
                                    handler.postDelayed(this, 100); // Small delay before next cycle
                                }
                            }).start();
                        }, holdMs);
                    } else {
                        // Exhale - contract
                        breathingCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(exhaleMs).withEndAction(() -> {
                            if (!isRunning) return;
                            
                            // Hold after exhale
                            if (holdAfterMs > 0) {
                                handler.postDelayed(this, holdAfterMs);
                            } else {
                                handler.postDelayed(this, 100);
                            }
                        }).start();
                    }
                }).start();
            }
        };
        handler.post(breathingRunnable);
    }

    private void updateTimerDisplay() {
        int mins = secondsLeft / 60;
        int secs = secondsLeft % 60;
        meditationTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
        
        // Update progress
        int elapsed = totalSeconds - secondsLeft;
        meditationProgress.setText("已修习：" + elapsed + "秒");
    }

    private void completeMeditation() {
        isRunning = false;
        isBreathingMode = false;
        
        // Stop animations
        if (breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
        }
        breathingCircle.setVisibility(View.GONE);
        breathingCircleOuter.setVisibility(View.GONE);
        
        // Update stats
        int minutes = prefs.getInt("meditation_minutes", 0) + customMinutes;
        int sessions = prefs.getInt("meditation_sessions", 0) + 1;
        prefs.edit()
            .putInt("meditation_minutes", minutes)
            .putInt("meditation_sessions", sessions)
            .remove("meditation_start_time")
            .apply();
        
        loadStats();
        
        // Show completion message
        Toast.makeText(getContext(), "修习完成！🙏", Toast.LENGTH_LONG).show();
        
        // Reset UI
        setControlsEnabled(true);
        startCustomBtn.setVisibility(View.VISIBLE);
        stopMeditationBtn.setVisibility(View.GONE);
        meditationTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", customMinutes, 0));
        meditationGuide.setText("修习完成，休息片刻");
    }

    private void stopMeditation() {
        if (!isRunning) return;
        
        isRunning = false;
        isBreathingMode = false;
        
        // Stop animations
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        if (breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
        }
        breathingCircle.setVisibility(View.GONE);
        breathingCircleOuter.setVisibility(View.GONE);
        
        // Clear start time
        prefs.edit().remove("meditation_start_time").apply();
        
        // Reset UI
        setControlsEnabled(true);
        startCustomBtn.setVisibility(View.VISIBLE);
        stopMeditationBtn.setVisibility(View.GONE);
        meditationTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", customMinutes, 0));
        meditationGuide.setText("修习已停止");
    }

    private void setControlsEnabled(boolean enabled) {
        btnDecrease.setEnabled(enabled);
        btnIncrease.setEnabled(enabled);
        
        startCustomBtn.setAlpha(enabled ? 1.0f : 0.5f);
        btnDecrease.setAlpha(enabled ? 1.0f : 0.5f);
        btnIncrease.setAlpha(enabled ? 1.0f : 0.5f);
        
        // Also update top select button if visible
        if (btnSelectMethodTop != null && selectedMethodIndex < 0) {
            btnSelectMethodTop.setAlpha(enabled ? 1.0f : 0.5f);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        // Check if meditation was interrupted
        long startTime = prefs.getLong("meditation_start_time", 0);
        if (startTime > 0) {
            // Resume or clean up interrupted session
            prefs.edit().remove("meditation_start_time").apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Don't stop meditation on pause, let it continue in background
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
