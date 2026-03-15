package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

public class BreathingFragment extends Fragment {

    private TextView breathingInstruction, breathingGuide;
    private TextView phaseInhale, phaseHold, phaseExhale;
    private TextView totalBreaths, totalMinutes;
    private Button startBreathingBtn;
    private Button mode478Btn, modeBoxBtn, modeCoherentBtn, modeCustomBtn;
    private View breathingCircle, breathingCircleOuter;
    private SharedPreferences prefs;
    private Handler handler;
    private Runnable breathingRunnable;
    private boolean isBreathing = false;
    private int phase = 0;
    private int breathCount = 0;
    private int currentMode = 0; // 0: 4-7-8, 1: Box, 2: Coherent, 3: Custom

    // Breathing patterns: {inhale, hold, exhale, hold_after} in milliseconds
    private final int[][] patterns = {
        {4000, 7000, 8000, 0},      // 4-7-8
        {4000, 4000, 4000, 4000},   // Box breathing
        {5000, 0, 5000, 0},         // Coherent breathing
        {4000, 4000, 4000, 0}       // Custom (default same as Box)
    };

    private final String[] modeNames = {"4-7-8 放松法", "方箱呼吸法", "共振呼吸法", "自定义呼吸法"};
    private final String[] phaseNames = {"吸气", "屏息", "呼气", "屏息"};
    private final String[] guides = {
        "4-7-8 呼吸法：吸气 4 秒，屏息 7 秒，呼气 8 秒。有效缓解焦虑，帮助入眠。",
        "方箱呼吸法：四个阶段各 4 秒。提升专注力，平静心神。",
        "共振呼吸法：吸气和呼气各 5 秒。平衡自主神经系统，达到最佳共振频率。",
        "自定义呼吸法：可以按照自己的节奏调整呼吸。"
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_breathing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        breathingInstruction = view.findViewById(R.id.breathingInstruction);
        breathingGuide = view.findViewById(R.id.breathingGuide);
        phaseInhale = view.findViewById(R.id.phaseInhale);
        phaseHold = view.findViewById(R.id.phaseHold);
        phaseExhale = view.findViewById(R.id.phaseExhale);
        totalBreaths = view.findViewById(R.id.totalBreaths);
        totalMinutes = view.findViewById(R.id.totalMinutes);
        startBreathingBtn = view.findViewById(R.id.startBreathingBtn);
        mode478Btn = view.findViewById(R.id.mode478Btn);
        modeBoxBtn = view.findViewById(R.id.modeBoxBtn);
        modeCoherentBtn = view.findViewById(R.id.modeCoherentBtn);
        modeCustomBtn = view.findViewById(R.id.modeCustomBtn);
        breathingCircle = view.findViewById(R.id.breathingCircle);
        breathingCircleOuter = view.findViewById(R.id.breathingCircleOuter);

        handler = new Handler();

        // Load stats
        loadStats();

        // Mode selection
        mode478Btn.setOnClickListener(v -> selectMode(0));
        modeBoxBtn.setOnClickListener(v -> selectMode(1));
        modeCoherentBtn.setOnClickListener(v -> selectMode(2));
        modeCustomBtn.setOnClickListener(v -> selectMode(3));

        // Start/Stop button
        startBreathingBtn.setOnClickListener(v -> {
            if (!isBreathing) {
                startBreathing();
            } else {
                stopBreathing();
            }
        });

        // Set default mode
        selectMode(0);
    }

    private void selectMode(int mode) {
        if (isBreathing) return;

        currentMode = mode;
        breathingGuide.setText(guides[mode]);

        // Update button styles
        updateModeButtonStyles(mode);
    }

    private void updateModeButtonStyles(int selectedMode) {
        mode478Btn.setBackgroundResource(selectedMode == 0 ? R.drawable.button_primary : R.drawable.button_secondary);
        mode478Btn.setTextColor(selectedMode == 0 ? 0xFFFFFFFF : 0xFF8b7355);
        
        modeBoxBtn.setBackgroundResource(selectedMode == 1 ? R.drawable.button_primary : R.drawable.button_secondary);
        modeBoxBtn.setTextColor(selectedMode == 1 ? 0xFFFFFFFF : 0xFF8b7355);
        
        modeCoherentBtn.setBackgroundResource(selectedMode == 2 ? R.drawable.button_primary : R.drawable.button_secondary);
        modeCoherentBtn.setTextColor(selectedMode == 2 ? 0xFFFFFFFF : 0xFF8b7355);
        
        modeCustomBtn.setBackgroundResource(selectedMode == 3 ? R.drawable.button_primary : R.drawable.button_secondary);
        modeCustomBtn.setTextColor(selectedMode == 3 ? 0xFFFFFFFF : 0xFF8b7355);
    }

    private void startBreathing() {
        isBreathing = true;
        startBreathingBtn.setText("完成练习");
        startBreathingBtn.setBackgroundResource(R.drawable.button_secondary);
        phase = 0;
        breathCount = 0;

        // Disable mode buttons during breathing
        setModeButtonsEnabled(false);

        breathingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isBreathing) return;

                int[] pattern = patterns[currentMode];
                
                // Update instruction and animation
                breathingInstruction.setText(phaseNames[phase]);
                updatePhaseIndicator(phase);
                animateCircle(phase == 0, pattern[phase]);

                // Count breaths (after inhale completes)
                if (phase == 0) {
                    breathCount++;
                }

                // Move to next phase
                int nextPhase = (phase + 1) % 4;
                phase = nextPhase;

                // Schedule next phase
                handler.postDelayed(this, pattern[phase]);
            }
        };

        handler.post(breathingRunnable);
    }

    private void animateCircle(boolean expand, int duration) {
        if (breathingCircle == null) return;

        // Scale animation
        ScaleAnimation scaleAnim = new ScaleAnimation(
            expand ? 0.7f : 1.0f, expand ? 1.0f : 0.7f,
            expand ? 0.7f : 1.0f, expand ? 1.0f : 0.7f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(duration);
        scaleAnim.setFillAfter(true);
        breathingCircle.startAnimation(scaleAnim);

        // Alpha animation for outer circle
        AlphaAnimation alphaAnim = new AlphaAnimation(expand ? 0.5f : 1.0f, expand ? 1.0f : 0.5f);
        alphaAnim.setDuration(duration);
        if (breathingCircleOuter != null) {
            breathingCircleOuter.startAnimation(alphaAnim);
        }
    }

    private void updatePhaseIndicator(int activePhase) {
        int activeColor = 0xFF8b7355;
        int inactiveColor = 0xFFcccccc;

        phaseInhale.setTextColor(activePhase == 0 ? activeColor : inactiveColor);
        phaseHold.setTextColor(activePhase == 1 || activePhase == 3 ? activeColor : inactiveColor);
        phaseExhale.setTextColor(activePhase == 2 ? activeColor : inactiveColor);
    }

    private void stopBreathing() {
        isBreathing = false;
        if (breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
        }

        // Calculate session duration (approximate)
        int[] pattern = patterns[currentMode];
        int cycleDuration = (pattern[0] + pattern[1] + pattern[2] + pattern[3]) / 1000; // seconds
        int minutes = (breathCount * cycleDuration) / 60;
        if (minutes < 1) minutes = 1;

        // Save stats
        int totalMins = prefs.getInt("meditation_minutes", 0) + minutes;
        int totalBreathsAll = prefs.getInt("breathing_count", 0) + breathCount;
        int sessions = prefs.getInt("breathing_sessions", 0) + 1;
        
        prefs.edit()
            .putInt("meditation_minutes", totalMins)
            .putInt("breathing_count", totalBreathsAll)
            .putInt("breathing_sessions", sessions)
            .apply();

        // Vibration completion
        vibrate(new long[]{0, 200, 100, 200, 100, 300}, -1);

        breathingInstruction.setText("完成");
        breathingGuide.setText("🙏 修习圆满，随喜功德");
        startBreathingBtn.setText("开始练习");
        startBreathingBtn.setBackgroundResource(R.drawable.button_primary);

        // Reset phase indicators
        updatePhaseIndicator(-1);

        // Re-enable mode buttons
        setModeButtonsEnabled(true);

        Toast.makeText(getContext(), "🙏 呼吸练习完成 • " + breathCount + " 次呼吸", Toast.LENGTH_LONG).show();

        loadStats();
    }

    private void setModeButtonsEnabled(boolean enabled) {
        mode478Btn.setEnabled(enabled);
        modeBoxBtn.setEnabled(enabled);
        modeCoherentBtn.setEnabled(enabled);
        modeCustomBtn.setEnabled(enabled);
        mode478Btn.setAlpha(enabled ? 1.0f : 0.5f);
        modeBoxBtn.setAlpha(enabled ? 1.0f : 0.5f);
        modeCoherentBtn.setAlpha(enabled ? 1.0f : 0.5f);
        modeCustomBtn.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void loadStats() {
        int breaths = prefs.getInt("breathing_count", 0);
        int minutes = prefs.getInt("meditation_minutes", 0);
        totalBreaths.setText(String.valueOf(breaths));
        totalMinutes.setText(String.valueOf(minutes));
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

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBreathing) {
            stopBreathing();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
