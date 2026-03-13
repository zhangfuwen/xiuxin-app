package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

    private TextView meditationTimer, meditationMode;
    private Button mode5minBtn, mode10minBtn, mode20minBtn, stopMeditationBtn;
    private SharedPreferences prefs;
    private Handler handler;
    private Runnable timerRunnable;
    private int secondsLeft;
    private boolean isRunning = false;
    private int totalMinutes;

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

        meditationTimer = view.findViewById(R.id.meditationTimer);
        meditationMode = view.findViewById(R.id.meditationMode);
        mode5minBtn = view.findViewById(R.id.mode5minBtn);
        mode10minBtn = view.findViewById(R.id.mode10minBtn);
        mode20minBtn = view.findViewById(R.id.mode20minBtn);
        stopMeditationBtn = view.findViewById(R.id.stopMeditationBtn);
        handler = new Handler();

        mode5minBtn.setOnClickListener(v -> startMeditation(5, "数息观"));
        mode10minBtn.setOnClickListener(v -> startMeditation(10, "随息观"));
        mode20minBtn.setOnClickListener(v -> startMeditation(20, "止观"));
        stopMeditationBtn.setOnClickListener(v -> stopMeditation());
    }

    private void startMeditation(int minutes, String modeName) {
        if (isRunning) return;

        isRunning = true;
        totalMinutes = minutes;
        secondsLeft = minutes * 60;

        meditationMode.setText(modeName);
        meditationTimer.setText(formatTime(secondsLeft));
        stopMeditationBtn.setVisibility(View.VISIBLE);

        disableModeButtons(true);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning || secondsLeft <= 0) return;

                secondsLeft--;
                meditationTimer.setText(formatTime(secondsLeft));

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
        int minutes = prefs.getInt("meditation_minutes", 0) + totalMinutes;
        prefs.edit().putInt("meditation_minutes", minutes).apply();

        meditationTimer.setText("完成!");
        meditationMode.setText("🙏 " + modeName + " 修习圆满");
        stopMeditationBtn.setText("关闭");

        Toast.makeText(getContext(), "🙏 " + modeName + " 完成", Toast.LENGTH_SHORT).show();
    }

    private void stopMeditation() {
        isRunning = false;
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        meditationTimer.setText(formatTime(secondsLeft));
        meditationMode.setText("已暂停");
        stopMeditationBtn.setVisibility(View.GONE);
        disableModeButtons(false);

        if (secondsLeft > 0) {
            int completedMinutes = totalMinutes - (secondsLeft / 60);
            if (completedMinutes > 0) {
                int minutes = prefs.getInt("meditation_minutes", 0) + completedMinutes;
                prefs.edit().putInt("meditation_minutes", minutes).apply();
            }
        }
    }

    private void disableModeButtons(boolean disable) {
        mode5minBtn.setEnabled(!disable);
        mode10minBtn.setEnabled(!disable);
        mode20minBtn.setEnabled(!disable);
        mode5minBtn.setAlpha(disable ? 0.5f : 1.0f);
        mode10minBtn.setAlpha(disable ? 0.5f : 1.0f);
        mode20minBtn.setAlpha(disable ? 0.5f : 1.0f);
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isRunning) {
            stopMeditation();
        }
    }
}
