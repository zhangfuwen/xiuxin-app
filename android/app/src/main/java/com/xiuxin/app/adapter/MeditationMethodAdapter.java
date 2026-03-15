package com.xiuxin.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiuxin.app.R;
import com.xiuxin.app.model.MeditationMethod;

import java.util.ArrayList;
import java.util.List;

public class MeditationMethodAdapter extends RecyclerView.Adapter<MeditationMethodAdapter.ViewHolder> {

    private Context context;
    private List<MeditationMethod> methods;
    private List<MeditationMethod> pinnedMethods;
    private SharedPreferences prefs;
    private OnMethodClickListener listener;

    public interface OnMethodClickListener {
        void onStartMethod(MeditationMethod method);
        void onTogglePin(MeditationMethod method, int position);
        void onPlayVideo(MeditationMethod method);
    }

    public MeditationMethodAdapter(Context context, OnMethodClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
        this.methods = new ArrayList<>();
        this.pinnedMethods = new ArrayList<>();
        loadMethods();
    }

    private void loadMethods() {
        // Initialize default meditation methods
        methods.clear();
        pinnedMethods.clear();

        // 专注类
        methods.add(new MeditationMethod("shuxi", "数息观", "专注类",
            "数呼吸次数，从 1 数到 10",
            "1. 舒适坐姿，轻闭双眼\n2. 自然呼吸，吸气时数 1，呼气时数 2\n3. 数到 10 后重新开始\n4. 走神时温和地回到呼吸",
            "培养专注力，平静心绪，适合初学者", "入门", 10));

        methods.add(new MeditationMethod("suixi", "随息观", "专注类",
            "觉察呼吸，不控制不干预",
            "1. 放松身体，自然呼吸\n2. 觉察气息进出鼻孔的感觉\n3. 不控制呼吸节奏，只是观察\n4. 心念飘走时，温和地带回",
            "训练觉知力，减少执着，培养平等心", "入门", 15));

        // 观照类
        methods.add(new MeditationMethod("zhiguan", "止观", "观照类",
            "止息妄念，观照当下",
            "1. 先数息让心安静\n2. 放下数数，只是安住\n3. 观察念头的生起和消失\n4. 不跟随不排斥，如天空观云",
            "开启智慧，照见实相，解脱烦恼", "进阶", 20));

        methods.add(new MeditationMethod("neiguan", "内观", "观照类",
            "观察身心现象的无常",
            "1. 从呼吸开始安定\n2. 扫描身体感受\n3. 观察感受的生灭\n4. 了知一切都是无常",
            "深刻洞察身心实相，减少执着", "进阶", 30));

        // 慈心类
        methods.add(new MeditationMethod("cixin", "慈心观", "慈心类",
            "发送慈爱给自己和他人",
            "1. 先祝福自己：愿我平安快乐\n2. 祝福亲人：愿你平安快乐\n3. 祝福中立的人\n4. 祝福困难的人\n5. 祝福一切众生",
            "培养慈悲心，减少嗔恨，改善人际关系", "入门", 15));

        // 身体类
        methods.add(new MeditationMethod("saomiao", "身体扫描", "身体类",
            "觉察身体每个部位的感受",
            "1. 从脚趾开始觉察\n2. 慢慢向上移动注意力\n3. 觉察每个部位的感受\n4. 不评判，只是觉察",
            "放松身心，增强身体觉知，缓解压力", "入门", 20));

        // 导引类
        MeditationMethod baduanjin = new MeditationMethod("baduanjin", "八段锦", "导引类",
            "传统养生功法，八式导引",
            "1. 两手托天理三焦\n2. 左右开弓似射雕\n3. 调理脾胃须单举\n4. 五劳七伤往后瞧\n5. 摇头摆尾去心火\n6. 两手攀足固肾腰\n7. 攒拳怒目增气力\n8. 背后七颠百病消",
            "强身健体，疏通经络，调和气血", "入门", 15);
        baduanjin.videoUrl = "https://www.youtube.com/watch?v=example"; // TODO: Add real video
        methods.add(baduanjin);

        methods.add(new MeditationMethod("yijinjing", "易筋经", "导引类",
            "少林传统导引术",
            "1. 韦驮献杵\n2. 横担降魔杵\n3. 掌托天门\n4. 摘星换斗\n...共十二式",
            "强筋健骨，增强内力，延年益寿", "进阶", 30));

        // Load pinned status
        String pinnedIds = prefs.getString("pinned_methods", "");
        for (MeditationMethod method : methods) {
            if (pinnedIds.contains(method.id + ",")) {
                method.isPinned = true;
                pinnedMethods.add(method);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meditation_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeditationMethod method = methods.get(position);
        
        holder.nameText.setText(method.name);
        holder.categoryText.setText(method.category);
        holder.briefText.setText(method.briefMethod);
        holder.durationText.setText(method.duration + "分钟");
        holder.difficultyText.setText(method.difficulty);
        holder.benefitsText.setText(method.benefits);
        holder.detailedText.setText(method.detailedMethod);

        // Pin button
        holder.pinBtn.setImageResource(method.isPinned ? R.drawable.ic_pin_filled : R.drawable.ic_pin_outline);
        
        // Video button (only for 导引类)
        holder.videoBtn.setVisibility(method.videoUrl != null ? View.VISIBLE : View.GONE);

        // Expand/collapse
        holder.detailLayout.setVisibility(method.isPinned ? View.VISIBLE : View.GONE);

        // Click listeners
        holder.startBtn.setOnClickListener(v -> listener.onStartMethod(method));
        holder.pinBtn.setOnClickListener(v -> listener.onTogglePin(method, position));
        holder.videoBtn.setOnClickListener(v -> listener.onPlayVideo(method));
        
        holder.itemView.setOnClickListener(v -> {
            if (holder.detailLayout.getVisibility() == View.VISIBLE) {
                holder.detailLayout.setVisibility(View.GONE);
            } else {
                holder.detailLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    public List<MeditationMethod> getPinnedMethods() {
        List<MeditationMethod> result = new ArrayList<>();
        for (MeditationMethod method : methods) {
            if (method.isPinned) {
                result.add(method);
            }
        }
        return result;
    }

    public void togglePin(int position) {
        MeditationMethod method = methods.get(position);
        method.isPinned = !method.isPinned;
        
        // Check pin limit
        if (method.isPinned && getPinnedMethods().size() > 5) {
            method.isPinned = false;
            return;
        }
        
        savePinnedStatus();
        notifyItemChanged(position);
    }

    private void savePinnedStatus() {
        StringBuilder sb = new StringBuilder();
        for (MeditationMethod method : methods) {
            if (method.isPinned) {
                sb.append(method.id).append(",");
            }
        }
        prefs.edit().putString("pinned_methods", sb.toString()).apply();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, categoryText, briefText, durationText, difficultyText;
        TextView benefitsText, detailedText;
        Button startBtn;
        ImageButton pinBtn, videoBtn;
        LinearLayout detailLayout;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.methodName);
            categoryText = itemView.findViewById(R.id.methodCategory);
            briefText = itemView.findViewById(R.id.methodBrief);
            durationText = itemView.findViewById(R.id.methodDuration);
            difficultyText = itemView.findViewById(R.id.methodDifficulty);
            benefitsText = itemView.findViewById(R.id.methodBenefits);
            detailedText = itemView.findViewById(R.id.methodDetailed);
            startBtn = itemView.findViewById(R.id.btnStartMethod);
            pinBtn = itemView.findViewById(R.id.btnPin);
            videoBtn = itemView.findViewById(R.id.btnVideo);
            detailLayout = itemView.findViewById(R.id.detailLayout);
        }
    }
}
