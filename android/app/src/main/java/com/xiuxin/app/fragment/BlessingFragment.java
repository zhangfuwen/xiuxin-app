package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlessingFragment extends Fragment {

    private TextView blessingText, blessingSource, blessingPractice, categoryChip;
    private LinearLayout blessingCard;
    private ImageButton prevBtn, nextBtn;
    private SharedPreferences prefs;
    private final Random random = new Random();
    private Handler handler = new Handler();
    private int currentIndex = 0;
    private long lastClickTime = 0;

    private static class Blessing {
        String text;
        String source;
        String practice;
        String category;

        Blessing(String text, String source, String practice, String category) {
            this.text = text;
            this.source = source;
            this.practice = practice;
            this.category = category;
        }
    }

    private final List<Blessing> blessings = new ArrayList<>();

    {
        blessings.add(new Blessing("心本无生因境有，境若无时心亦无。", "《楞严经》", "心随境转是凡夫，境随心转是圣贤。今日练习：观察一个升起的念头，不跟随，不压抑，只是看着它来去。", "禅宗"));
        blessings.add(new Blessing("应无所住而生其心。", "《金刚经》", "不执着于任何事物，心才能自由。今日练习：做一件事时，全然投入；做完后，全然放下。", "禅宗"));
        blessings.add(new Blessing("一切有为法，如梦幻泡影，如露亦如电，应作如是观。", "《金刚经》", "世间万物皆无常，如露如电。今日练习：当烦恼生起时，默念'这也是无常的'，观察它的变化。", "禅宗"));
        blessings.add(new Blessing("菩提本无树，明镜亦非台。本来无一物，何处惹尘埃。", "六祖慧能", "自性本净，无需外求。今日练习：静坐五分钟，问自己'正在烦恼的是谁？'", "禅宗"));
        blessings.add(new Blessing("行到水穷处，坐看云起时。", "王维", "绝境处自有转机。今日练习：遇到困难时，停下来，深呼吸三次，再问'还有另一种可能吗？'", "禅宗"));
        blessings.add(new Blessing("宠辱不惊，看庭前花开花落；去留无意，望天上云卷云舒。", "陈继儒", "得失随缘，心无增减。今日练习：今天遇到赞美或批评时，都当作耳边风。", "禅宗"));
        blessings.add(new Blessing("知止而后有定，定而后能静，静而后能安，安而后能虑，虑而后能得。", "《大学》", "知止是智慧的第一步。今日练习：在说话或行动前，停顿三秒，问'这是必要的吗？'", "儒家"));
        blessings.add(new Blessing("上善若水，水善利万物而不争。", "《道德经》", "柔能克刚，不争而胜。今日练习：今天遇到冲突时，尝试像水一样，绕行而不硬碰。", "道家"));
        blessings.add(new Blessing("制心一处，无事不办。", "佛经", "专注的力量无穷。今日练习：选择一件事，用 100% 的注意力完成它。", "佛经"));
        blessings.add(new Blessing("日日是好日。", "禅宗公案", "好坏皆由心生。今日练习：无论今天发生什么，睡前都说'今天是好日'。", "禅宗"));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blessing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        blessingText = view.findViewById(R.id.blessingText);
        blessingSource = view.findViewById(R.id.blessingSource);
        blessingPractice = view.findViewById(R.id.blessingPractice);
        categoryChip = view.findViewById(R.id.categoryChip);
        blessingCard = view.findViewById(R.id.blessingCard);
        prevBtn = view.findViewById(R.id.prevBtn);
        nextBtn = view.findViewById(R.id.nextBtn);

        // 随机初始位置
        currentIndex = random.nextInt(blessings.size());

        blessingCard.setOnClickListener(v -> {
            // 双击收藏检测
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 300) {
                // 双击
                saveFavorite(currentIndex);
                Toast.makeText(getContext(), "💜 已收藏", Toast.LENGTH_SHORT).show();
            } else {
                // 单击刷新
                refreshBlessing();
            }
            lastClickTime = currentTime;
        });

        prevBtn.setOnClickListener(v -> {
            currentIndex = (currentIndex - 1 + blessings.size()) % blessings.size();
            displayBlessing(currentIndex);
        });

        nextBtn.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % blessings.size();
            displayBlessing(currentIndex);
        });

        displayBlessing(currentIndex);
    }

    private void displayBlessing(int index) {
        Blessing b = blessings.get(index);
        blessingText.setText(b.text);
        blessingSource.setText("—— " + b.source);
        blessingPractice.setText(b.practice);
        categoryChip.setText(b.category);

        // 添加淡入动画
        blessingText.setAlpha(0f);
        blessingText.animate().alpha(1f).setDuration(300).start();
    }

    private void refreshBlessing() {
        int newIndex;
        do {
            newIndex = random.nextInt(blessings.size());
        } while (newIndex == currentIndex && blessings.size() > 1);
        
        currentIndex = newIndex;
        displayBlessing(currentIndex);
    }

    private void saveFavorite(int index) {
        SharedPreferences.Editor editor = prefs.edit();
        String favorites = prefs.getString("favorites", "");
        if (!favorites.contains(String.valueOf(index))) {
            if (!favorites.isEmpty()) favorites += ",";
            favorites += index;
            editor.putString("favorites", favorites);
            editor.apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
