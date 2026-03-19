package com.xiuxin.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiuxin.app.R;
import com.xiuxin.app.model.MeditationMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单法门列表 Adapter - 用于底部水平滚动选择
 */
public class SimpleMethodAdapter extends RecyclerView.Adapter<SimpleMethodAdapter.ViewHolder> {

    private Context context;
    private List<MeditationMethod> methods;
    private int selectedIndex = -1;
    private OnMethodSelectListener listener;

    public interface OnMethodSelectListener {
        void onMethodSelected(MeditationMethod method, int position);
    }

    public SimpleMethodAdapter(Context context, OnMethodSelectListener listener) {
        this.context = context;
        this.listener = listener;
        this.methods = new ArrayList<>();
        initializeMethods();
    }

    private void initializeMethods() {
        methods.add(new MeditationMethod("shuxi", "数息观", "专注类",
            "数呼吸次数，从 1 数到 10",
            "1. 舒适坐姿，轻闭双眼\n2. 自然呼吸，吸气时数 1，呼气时数 2\n3. 数到 10 后重新开始\n4. 走神时温和地回到呼吸",
            "培养专注力，平静心绪，适合初学者", "入门", 10));

        methods.add(new MeditationMethod("suixi", "随息观", "专注类",
            "觉察呼吸，不控制不干预",
            "1. 放松身体，自然呼吸\n2. 觉察气息进出鼻孔的感觉\n3. 不控制呼吸节奏，只是观察\n4. 心念飘走时，温和地带回",
            "训练觉知力，减少执着，培养平等心", "入门", 15));

        methods.add(new MeditationMethod("zhiguan", "止观", "观照类",
            "止息妄念，观照当下",
            "1. 先数息让心安静\n2. 放下数数，只是安住\n3. 观察念头的生起和消失\n4. 不跟随不排斥，如天空观云",
            "开启智慧，照见实相，解脱烦恼", "进阶", 20));

        methods.add(new MeditationMethod("cixin", "慈心观", "慈心类",
            "发送慈爱给自己和他人",
            "1. 先祝福自己：愿我平安快乐\n2. 祝福亲人：愿你平安快乐\n3. 祝福中立的人\n4. 祝福困难的人\n5. 祝福一切众生",
            "培养慈悲心，减少嗔恨，改善人际关系", "入门", 15));

        methods.add(new MeditationMethod("saomiao", "身体扫描", "身体类",
            "觉察身体每个部位的感受",
            "1. 从脚趾开始觉察\n2. 慢慢向上移动注意力\n3. 觉察每个部位的感受\n4. 不评判，只是觉察",
            "放松身心，增强身体觉知，缓解压力", "入门", 20));

        methods.add(new MeditationMethod("huxi", "呼吸法门", "呼吸类",
            "跟随动画圆圈进行呼吸练习",
            "1. 吸气时圆圈放大\n2. 呼气时圆圈缩小\n3. 自然地跟随节奏\n4. 可选择不同呼吸模式",
            "放松身心，调节呼吸节奏", "入门", 10));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_simple_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeditationMethod method = methods.get(position);
        holder.nameText.setText(method.name);
        
        // Highlight selected
        if (position == selectedIndex) {
            holder.itemView.setBackgroundResource(R.drawable.method_card_selected);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.method_card);
        }

        holder.itemView.setOnClickListener(v -> {
            int lastSelected = selectedIndex;
            selectedIndex = position;
            if (lastSelected >= 0) {
                notifyItemChanged(lastSelected);
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onMethodSelected(method, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    public void setSelectedIndex(int index) {
        int lastSelected = selectedIndex;
        selectedIndex = index;
        if (lastSelected >= 0) {
            notifyItemChanged(lastSelected);
        }
        if (selectedIndex >= 0) {
            notifyItemChanged(selectedIndex);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public MeditationMethod getSelectedMethod() {
        if (selectedIndex >= 0 && selectedIndex < methods.size()) {
            return methods.get(selectedIndex);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.methodName);
        }
    }
}
