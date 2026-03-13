package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiuxin.app.R;

import java.util.Random;

public class BlessingFragment extends Fragment {

    private TextView blessingText, blessingSource, blessingPractice;
    private LinearLayout blessingCard;
    private SharedPreferences prefs;
    private final Random random = new Random();

    private final String[][] blessings = {
        {"心本无生因境有，境若无时心亦无。", "《楞严经》", "心随境转是凡夫，境随心转是圣贤。今日练习：观察一个升起的念头，不跟随，不压抑，只是看着它来去。"},
        {"应无所住而生其心。", "《金刚经》", "不执着于任何事物，心才能自由。今日练习：做一件事时，全然投入；做完后，全然放下。"},
        {"一切有为法，如梦幻泡影，如露亦如电，应作如是观。", "《金刚经》", "世间万物皆无常，如露如电。今日练习：当烦恼生起时，默念'这也是无常的'，观察它的变化。"},
        {"菩提本无树，明镜亦非台。本来无一物，何处惹尘埃。", "六祖慧能", "自性本净，无需外求。今日练习：静坐五分钟，问自己'正在烦恼的是谁？'"},
        {"行到水穷处，坐看云起时。", "王维", "绝境处自有转机。今日练习：遇到困难时，停下来，深呼吸三次，再问'还有另一种可能吗？'"},
        {"宠辱不惊，看庭前花开花落；去留无意，望天上云卷云舒。", "陈继儒", "得失随缘，心无增减。今日练习：今天遇到赞美或批评时，都当作耳边风。"},
        {"知止而后有定，定而后能静，静而后能安，安而后能虑，虑而后能得。", "《大学》", "知止是智慧的第一步。今日练习：在说话或行动前，停顿三秒，问'这是必要的吗？'"},
        {"上善若水，水善利万物而不争。", "《道德经》", "柔能克刚，不争而胜。今日练习：今天遇到冲突时，尝试像水一样，绕行而不硬碰。"},
        {"制心一处，无事不办。", "佛经", "专注的力量无穷。今日练习：选择一件事，用 100% 的注意力完成它。"},
        {"日日是好日。", "禅宗公案", "好坏皆由心生。今日练习：无论今天发生什么，睡前都说'今天是好日'。"}
    };

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
        blessingCard = view.findViewById(R.id.blessingCard);

        blessingCard.setOnClickListener(v -> {
            loadBlessing();
            Toast.makeText(getContext(), "✨ 加持语已刷新", Toast.LENGTH_SHORT).show();
        });

        loadBlessing();
    }

    private void loadBlessing() {
        int index = random.nextInt(blessings.length);
        String[] blessing = blessings[index];

        blessingText.setText(blessing[0]);
        blessingSource.setText("—— " + blessing[1]);
        blessingPractice.setText("💡 " + blessing[2]);
    }
}
