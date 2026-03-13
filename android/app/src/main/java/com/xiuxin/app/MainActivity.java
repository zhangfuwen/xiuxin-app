package com.xiuxin.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

    // UI Components
    private TextView blessingText, blessingSource, blessingPractice;
    private TextView totalDaysText, streakText, meditationMinutesText;
    private Button startBreathingBtn, startMeditationBtn, startPracticeBtn;
    private LinearLayout blessingCard;

    // Data
    private SharedPreferences prefs;
    private final Random random = new Random();

    // Blessings database
    private final String[][] blessings = {
        {
            "心本无生因境有，境若无时心亦无。",
            "《楞严经》",
            "心随境转是凡夫，境随心转是圣贤。今日练习：观察一个升起的念头，不跟随，不压抑，只是看着它来去。"
        },
        {
            "应无所住而生其心。",
            "《金刚经》",
            "不执着于任何事物，心才能自由。今日练习：做一件事时，全然投入；做完后，全然放下。"
        },
        {
            "一切有为法，如梦幻泡影，如露亦如电，应作如是观。",
            "《金刚经》",
            "世间万物皆无常，如露如电。今日练习：当烦恼生起时，默念'这也是无常的'，观察它的变化。"
        },
        {
            "菩提本无树，明镜亦非台。本来无一物，何处惹尘埃。",
            "六祖慧能",
            "自性本净，无需外求。今日练习：静坐五分钟，问自己'正在烦恼的是谁？'"
        },
        {
            "行到水穷处，坐看云起时。",
            "王维",
            "绝境处自有转机。今日练习：遇到困难时，停下来，深呼吸三次，再问'还有另一种可能吗？'"
        },
        {
            "宠辱不惊，看庭前花开花落；去留无意，望天上云卷云舒。",
            "陈继儒",
            "得失随缘，心无增减。今日练习：今天遇到赞美或批评时，都当作耳边风。"
        },
        {
            "知止而后有定，定而后能静，静而后能安，安而后能虑，虑而后能得。",
            "《大学》",
            "知止是智慧的第一步。今日练习：在说话或行动前，停顿三秒，问'这是必要的吗？'"
        },
        {
            "上善若水，水善利万物而不争。",
            "《道德经》",
            "柔能克刚，不争而胜。今日练习：今天遇到冲突时，尝试像水一样，绕行而不硬碰。"
        },
        {
            "制心一处，无事不办。",
            "佛经",
            "专注的力量无穷。今日练习：选择一件事，用 100% 的注意力完成它。"
        },
        {
            "日日是好日。",
            "禅宗公案",
            "好坏皆由心生。今日练习：无论今天发生什么，睡前都说'今天是好日'。"
        }
    };

    // Precepts for daily practice
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("xiuxin", Context.MODE_PRIVATE);

        // Bind UI components
        bindViews();

        // Setup click listeners
        setupListeners();

        // Load initial data
        loadBlessing();
        loadStats();
    }

    private void bindViews() {
        blessingText = findViewById(R.id.blessingText);
        blessingSource = findViewById(R.id.blessingSource);
        blessingPractice = findViewById(R.id.blessingPractice);
        totalDaysText = findViewById(R.id.totalDaysText);
        streakText = findViewById(R.id.streakText);
        meditationMinutesText = findViewById(R.id.meditationMinutesText);
        startBreathingBtn = findViewById(R.id.startBreathingBtn);
        startMeditationBtn = findViewById(R.id.startMeditationBtn);
        startPracticeBtn = findViewById(R.id.startPracticeBtn);
        blessingCard = findViewById(R.id.blessingCard);
    }

    private void setupListeners() {
        // Blessing card click - refresh blessing
        blessingCard.setOnClickListener(v -> {
            loadBlessing();
            Toast.makeText(this, "✨ 加持语已刷新", Toast.LENGTH_SHORT).show();
        });

        // Breathing exercise
        startBreathingBtn.setOnClickListener(v -> startBreathingExercise());

        // Meditation timer
        startMeditationBtn.setOnClickListener(v -> showMeditationOptions());

        // Daily practice
        startPracticeBtn.setOnClickListener(v -> showDailyPractice());
    }

    private void loadBlessing() {
        int index = random.nextInt(blessings.length);
        String[] blessing = blessings[index];
        
        blessingText.setText(blessing[0]);
        blessingSource.setText("—— " + blessing[1]);
        blessingPractice.setText("💡 " + blessing[2]);
    }

    private void loadStats() {
        int totalDays = prefs.getInt("total_days", 0);
        int streak = prefs.getInt("streak", 0);
        int meditationMinutes = prefs.getInt("meditation_minutes", 0);

        totalDaysText.setText(String.valueOf(totalDays));
        streakText.setText(String.valueOf(streak));
        meditationMinutesText.setText(String.valueOf(meditationMinutes));
    }

    private void startBreathingExercise() {
        // Show breathing exercise dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_breathing, null);

        TextView breathingInstruction = dialogView.findViewById(R.id.breathingInstruction);
        Button stopBreathingBtn = dialogView.findViewById(R.id.stopBreathingBtn);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        // Breathing animation simulation
        final boolean[] isBreathing = {true};
        final int[] phase = {0}; // 0: inhale, 1: hold, 2: exhale
        final String[] instructions = {"吸气...", "屏息...", "呼气..."};

        final android.os.Handler handler = new android.os.Handler();
        final Runnable[] runnable = {null};

        runnable[0] = new Runnable() {
            @Override
            public void run() {
                if (!isBreathing[0]) return;
                
                breathingInstruction.setText(instructions[phase[0]]);
                phase[0] = (phase[0] + 1) % 3;
                
                // 4-7-8 breathing: inhale 4s, hold 7s, exhale 8s
                int delay = phase[0] == 0 ? 4000 : (phase[0] == 1 ? 7000 : 8000);
                handler.postDelayed(runnable[0], delay);
            }
        };

        handler.post(runnable[0]);

        stopBreathingBtn.setOnClickListener(v -> {
            isBreathing[0] = false;
            handler.removeCallbacks(runnable[0]);
            dialog.dismiss();

            // Update stats
            int minutes = prefs.getInt("meditation_minutes", 0) + 2;
            prefs.edit().putInt("meditation_minutes", minutes).apply();
            updateStats();

            Toast.makeText(this, "🙏 呼吸练习完成", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showMeditationOptions() {
        String[] options = {"5 分钟 - 数息观", "10 分钟 - 随息观", "20 分钟 - 止观"};

        new AlertDialog.Builder(this)
            .setTitle("选择法门")
            .setItems(options, (dialog, which) -> {
                int[] minutes = {5, 10, 20};
                String[] names = {"数息观", "随息观", "止观"};
                startMeditation(minutes[which], names[which]);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void startMeditation(int minutes, String name) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_meditation, null);

        TextView meditationTimer = dialogView.findViewById(R.id.meditationTimer);
        TextView meditationStatus = dialogView.findViewById(R.id.meditationStatus);
        Button stopMeditationBtn = dialogView.findViewById(R.id.stopMeditationBtn);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        final int totalSeconds = minutes * 60;
        final int[] secondsLeft = {totalSeconds};
        final boolean[] isRunning = {true};

        meditationTimer.setText(formatTime(totalSeconds));

        final android.os.Handler handler = new android.os.Handler();
        final Runnable[] runnable = {null};

        runnable[0] = new Runnable() {
            @Override
            public void run() {
                if (!isRunning[0] || secondsLeft[0] <= 0) return;

                secondsLeft[0]--;
                meditationTimer.setText(formatTime(secondsLeft[0]));

                if (secondsLeft[0] > 0) {
                    handler.postDelayed(runnable[0], 1000);
                } else {
                    meditationStatus.setText("✅ 修习完成");
                    stopMeditationBtn.setText("关闭");

                    // Update stats
                    int currentMinutes = prefs.getInt("meditation_minutes", 0) + minutes;
                    prefs.edit().putInt("meditation_minutes", currentMinutes).apply();
                    updateStats();

                    Toast.makeText(MainActivity.this, "🙏 " + name + " 完成", Toast.LENGTH_SHORT).show();
                }
            }
        };

        handler.post(runnable[0]);

        stopMeditationBtn.setOnClickListener(v -> {
            isRunning[0] = false;
            handler.removeCallbacks(runnable[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
    }

    private void showDailyPractice() {
        // Get today's precept
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        String todayPrecept = precepts[dayOfYear % precepts.length];

        // Check if already completed today
        long lastPracticeDate = prefs.getLong("last_practice_date", 0);
        boolean completedToday = isSameDay(lastPracticeDate, System.currentTimeMillis());

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_practice, null);

        TextView preceptText = dialogView.findViewById(R.id.preceptText);
        Button completeBtn = dialogView.findViewById(R.id.completeBtn);
        TextView practiceStatus = dialogView.findViewById(R.id.practiceStatus);

        preceptText.setText(todayPrecept);

        if (completedToday) {
            completeBtn.setText("已完成 ✓");
            completeBtn.setEnabled(false);
            practiceStatus.setText("✅ 今日功课已完成，随喜赞叹！");
        } else {
            completeBtn.setText("完成打卡");
            practiceStatus.setText("🙏 今日功课，请精进完成");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        completeBtn.setOnClickListener(v -> {
            prefs.edit()
                .putLong("last_practice_date", System.currentTimeMillis())
                .putInt("total_days", prefs.getInt("total_days", 0) + 1)
                .putInt("streak", calculateStreak())
                .apply();

            updateStats();
            completeBtn.setText("已完成 ✓");
            completeBtn.setEnabled(false);
            practiceStatus.setText("✅ 今日功课已完成，随喜赞叹！");

            Toast.makeText(this, "🙏 打卡成功，功德 +1", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
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
            return 1; // Reset streak
        }
    }

    private void updateStats() {
        totalDaysText.setText(String.valueOf(prefs.getInt("total_days", 0)));
        streakText.setText(String.valueOf(prefs.getInt("streak", 0)));
        meditationMinutesText.setText(String.valueOf(prefs.getInt("meditation_minutes", 0)));
    }

    private long backPressedTime;

    @Override
    public void onBackPressed() {
        // Double tap to exit
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        }
        Toast.makeText(this, "再按一次退出修心", Toast.LENGTH_SHORT).show();
        backPressedTime = System.currentTimeMillis();
    }
}
