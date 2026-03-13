package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

public class BreathingFragment extends Fragment {

    private TextView breathingInstruction;
    private Button startBreathingBtn;
    private View breathingCircle;
    private SharedPreferences prefs;
    private Handler handler;
    private Runnable breathingRunnable;
    private boolean isBreathing = false;
    private int phase = 0;

    private final String[] instructions = {"吸气...", "屏息...", "呼气..."};
    private final int[] delays = {4000, 7000, 8000};

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

        breathingInstruction = view.findViewById(R.id.breathingInstruction);
        startBreathingBtn = view.findViewById(R.id.startBreathingBtn);
        breathingCircle = view.findViewById(R.id.breathingCircle);
        handler = new Handler();

        startBreathingBtn.setOnClickListener(v -> {
            if (!isBreathing) {
                startBreathing();
            } else {
                stopBreathing();
            }
        });
    }

    private void startBreathing() {
        isBreathing = true;
        startBreathingBtn.setText("完成练习");
        startBreathingBtn.setBackgroundResource(R.drawable.button_secondary);
        phase = 0;

        breathingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isBreathing) return;

                breathingInstruction.setText(instructions[phase]);
                animateCircle(phase == 0);

                phase = (phase + 1) % 3;
                handler.postDelayed(this, delays[phase]);
            }
        };

        handler.post(breathingRunnable);
    }

    private void animateCircle(boolean expand) {
        if (breathingCircle == null) return;

        AlphaAnimation alphaAnim = new AlphaAnimation(expand ? 0.5f : 1.0f, expand ? 1.0f : 0.5f);
        alphaAnim.setDuration(expand ? 4000 : 8000);
        alphaAnim.setFillAfter(true);
        breathingCircle.startAnimation(alphaAnim);
    }

    private void stopBreathing() {
        isBreathing = false;
        if (breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
        }

        int minutes = prefs.getInt("meditation_minutes", 0) + 2;
        prefs.edit().putInt("meditation_minutes", minutes).apply();

        breathingInstruction.setText("练习完成");
        startBreathingBtn.setText("开始练习");
        startBreathingBtn.setBackgroundResource(R.drawable.button_primary);

        Toast.makeText(getContext(), "🙏 呼吸练习完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBreathing) {
            stopBreathing();
        }
    }
}
