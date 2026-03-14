package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiuxin.app.R;
import com.xiuxin.app.activity.BlessingDetailActivity;
import com.xiuxin.app.adapter.BlessingAdapter;
import com.xiuxin.app.api.BlessingsApiClient;
import com.xiuxin.app.dialog.PublishBlessingDialog;
import com.xiuxin.app.model.Blessing;

import java.util.ArrayList;
import java.util.List;

public class BlessingFragment extends Fragment {

    private RecyclerView recyclerView;
    private Spinner categorySpinner;
    private Button btnPublish;
    private BlessingAdapter adapter;
    private SharedPreferences prefs;
    private String selectedCategory = "全部";
    private BlessingsApiClient apiClient;
    private View loadingView;
    
    private final String[] categories = {"全部", "禅宗", "儒家", "道家", "佛经"};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
        apiClient = BlessingsApiClient.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blessing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.blessingRecyclerView);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        btnPublish = view.findViewById(R.id.btnPublish);
        loadingView = view.findViewById(R.id.loadingView);
        
        if (loadingView == null) {
            // 如果布局中没有 loadingView，创建一个
            loadingView = new View(getContext());
            loadingView.setVisibility(View.VISIBLE);
        }
        
        // Setup publish button
        btnPublish.setOnClickListener(v -> showPublishDialog());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BlessingAdapter();
        recyclerView.setAdapter(adapter);

        // Setup category spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
                loadBlessingsFromApi();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Adapter click listener
        adapter.setOnItemClickListener(new BlessingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BlessingAdapter.BlessingItem item, int position) {
                // Open detail activity
                Intent intent = new Intent(getContext(), BlessingDetailActivity.class);
                intent.putExtra("text", item.text);
                intent.putExtra("source", item.source);
                intent.putExtra("practice", item.practice);
                intent.putExtra("category", item.category);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(BlessingAdapter.BlessingItem item, int position) {
                if (item.id > 0) {
                    // API 模式：调用点赞接口
                    apiClient.toggleLike(item.id, new BlessingsApiClient.ApiCallback<BlessingsApiClient.InteractionResult>() {
                        @Override
                        public void onSuccess(BlessingsApiClient.InteractionResult result) {
                            item.isLiked = result.active;
                            item.likeCount = result.count;
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), item.isLiked ? "❤️ 已点赞" : "取消点赞", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "点赞失败：" + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 离线模式
                    Toast.makeText(getContext(), "❤️ 感恩", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFavoriteClick(BlessingAdapter.BlessingItem item, int position) {
                if (item.id > 0) {
                    // API 模式：调用收藏接口
                    apiClient.toggleFavorite(item.id, new BlessingsApiClient.ApiCallback<BlessingsApiClient.InteractionResult>() {
                        @Override
                        public void onSuccess(BlessingsApiClient.InteractionResult result) {
                            item.isFavorite = result.active;
                            item.favoriteCount = result.count;
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), item.isFavorite ? "⭐ 已收藏" : "取消收藏", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "收藏失败：" + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 离线模式
                    Toast.makeText(getContext(), "⭐ 已收藏", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load blessings from API
        loadBlessingsFromApi();
    }
    
    /**
     * 从 API 加载禅语
     */
    private void loadBlessingsFromApi() {
        showLoading(true);
        
        apiClient.getBlessings(selectedCategory.equals("全部") ? null : selectedCategory, 20, 
            new BlessingsApiClient.ApiCallback<List<Blessing>>() {
                @Override
                public void onSuccess(List<Blessing> blessings) {
                    showLoading(false);
                    updateAdapter(blessings);
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    Toast.makeText(getContext(), "加载失败：" + error, Toast.LENGTH_LONG).show();
                    // 降级：显示本地数据
                    loadLocalBlessings();
                }
            });
    }
    
    /**
     * 更新 Adapter 数据
     */
    private void updateAdapter(List<Blessing> blessings) {
        List<BlessingAdapter.BlessingItem> items = new ArrayList<>();
        for (Blessing blessing : blessings) {
            items.add(BlessingAdapter.BlessingItem.fromApiModel(blessing));
        }
        adapter.setBlessings(items);
    }
    
    /**
     * 加载本地禅语（降级方案）
     */
    private void loadLocalBlessings() {
        List<BlessingAdapter.BlessingItem> blessings = new ArrayList<>();

        blessings.add(new BlessingAdapter.BlessingItem(
            "心本无生因境有，境若无时心亦无。",
            "《楞严经》",
            "心随境转是凡夫，境随心转是圣贤。今日练习：观察一个升起的念头，不跟随，不压抑，只是看着它来去。",
            "禅宗"
        ));

        blessings.add(new BlessingAdapter.BlessingItem(
            "应无所住而生其心。",
            "《金刚经》",
            "不执着于任何事物，心才能自由。今日练习：做一件事时，全然投入；做完后，全然放下。",
            "禅宗"
        ));

        blessings.add(new BlessingAdapter.BlessingItem(
            "一切有为法，如梦幻泡影，如露亦如电，应作如是观。",
            "《金刚经》",
            "世间万物皆无常，如露如电。今日练习：当烦恼生起时，默念'这也是无常的'，观察它的变化。",
            "禅宗"
        ));

        blessings.add(new BlessingAdapter.BlessingItem(
            "菩提本无树，明镜亦非台。本来无一物，何处惹尘埃。",
            "六祖慧能",
            "自性本净，无需外求。今日练习：静坐五分钟，问自己'正在烦恼的是谁？'",
            "禅宗"
        ));

        blessings.add(new BlessingAdapter.BlessingItem(
            "行到水穷处，坐看云起时。",
            "王维",
            "绝境处自有转机。今日练习：遇到困难时，停下来，深呼吸三次，再问'还有另一种可能吗？'",
            "禅宗"
        ));

        adapter.setBlessings(blessings);
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadBlessingsFromApi();
    }
    
    /**
     * 显示发布对话框
     */
    private void showPublishDialog() {
        PublishBlessingDialog dialog = new PublishBlessingDialog(getContext());
        dialog.setOnPublishListener(new PublishBlessingDialog.OnPublishListener() {
            @Override
            public void onPublishSuccess(Blessing blessing) {
                // Refresh the list
                loadBlessingsFromApi();
            }

            @Override
            public void onPublishError(String error) {
                // Error already shown in dialog
            }
        });
        dialog.show();
    }
}
