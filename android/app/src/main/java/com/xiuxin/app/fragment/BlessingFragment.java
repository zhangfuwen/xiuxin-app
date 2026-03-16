package com.xiuxin.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiuxin.app.R;
import com.xiuxin.app.activity.BlessingDetailActivity;
import com.xiuxin.app.activity.PublishBlessingActivity;
import com.xiuxin.app.adapter.BlessingAdapter;
import com.xiuxin.app.api.BlessingsApiClient;
import com.xiuxin.app.model.Blessing;

import java.util.ArrayList;
import java.util.List;

public class BlessingFragment extends Fragment {

    private static final String TAG = "BlessingFragment";
    private RecyclerView recyclerView;
    private Spinner categorySpinner;
    private Spinner filterSpinner;
    private Button btnPublish;
    private BlessingAdapter adapter;
    private SharedPreferences prefs;
    private String selectedCategory = "全部";
    private String currentFilter = "全部"; // 全部，赞过的，收藏的
    private BlessingsApiClient apiClient;
    private View loadingView;
    private View emptyView;
    private View errorView;
    private TextView errorText, errorDetail;
    private Button btnRetry;
    
    private final String[] categories = {"全部", "禅宗", "儒家", "道家", "佛经"};
    private final String[] filters = {"全部", "❤️ 赞过的", "⭐ 收藏的"};
    private List<Blessing> allBlessings = new ArrayList<>(); // Cache all blessings

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "=== onAttach called ===");
        prefs = context.getSharedPreferences("xiuxin", Context.MODE_PRIVATE);
        apiClient = BlessingsApiClient.getInstance();
        Log.d(TAG, "SharedPreferences and ApiClient initialized");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "=== onCreateView called ===");
        View view = inflater.inflate(R.layout.fragment_blessing, container, false);
        Log.d(TAG, "Layout inflated: " + (view != null ? "success" : "FAILED"));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "=== onViewCreated called ===");

        recyclerView = view.findViewById(R.id.blessingRecyclerView);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        btnPublish = view.findViewById(R.id.btnPublish);
        loadingView = view.findViewById(R.id.loadingView);
        emptyView = view.findViewById(R.id.emptyView);
        errorView = view.findViewById(R.id.errorView);
        errorText = view.findViewById(R.id.errorText);
        errorDetail = view.findViewById(R.id.errorDetail);
        btnRetry = view.findViewById(R.id.btnRetry);
        
        Log.d(TAG, "All views initialized");
        Log.d(TAG, "recyclerView: " + (recyclerView != null ? "OK" : "NULL"));
        Log.d(TAG, "btnPublish: " + (btnPublish != null ? "OK" : "NULL"));
        Log.d(TAG, "filterSpinner: " + (filterSpinner != null ? "OK" : "NULL"));
        
        // Setup publish button
        btnPublish.setOnClickListener(v -> {
            Log.d(TAG, "Publish button clicked");
            showPublishDialog();
        });
        
        // Setup retry button
        btnRetry.setOnClickListener(v -> {
            Log.d(TAG, "Retry button clicked");
            loadBlessingsFromApi();
        });
        
        // Setup filter spinner
        setupFilterSpinner();

        // Setup RecyclerView
        Log.d(TAG, "Setting up RecyclerView...");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BlessingAdapter();
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete");

        // Setup category spinner
        Log.d(TAG, "Setting up category spinner...");
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
                Log.d(TAG, "Category changed to: " + selectedCategory);
                loadBlessingsFromApi();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                Log.d(TAG, "Nothing selected in spinner");
            }
        });
        Log.d(TAG, "Category spinner setup complete");
        
        // Setup filter spinner
        setupFilterSpinner();

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
                            // 本地保存点赞状态
                            saveLikeState(item.id, result.active);
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), item.isLiked ? "❤️ 已点赞" : "取消点赞", Toast.LENGTH_SHORT).show();
                            // 如果当前是点赞过滤，刷新列表
                            if (currentFilter.equals("❤️ 赞过的")) {
                                applyFilter();
                            }
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
                            // 本地保存收藏状态
                            saveFavoriteState(item.id, result.active);
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), item.isFavorite ? "⭐ 已收藏" : "取消收藏", Toast.LENGTH_SHORT).show();
                            // 如果当前是收藏过滤，刷新列表
                            if (currentFilter.equals("⭐ 收藏的")) {
                                applyFilter();
                            }
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
        Log.d(TAG, "Calling loadBlessingsFromApi() for the first time");
        loadBlessingsFromApi();
    }
    
    /**
     * 从 API 加载禅语
     */
    private void loadBlessingsFromApi() {
        Log.d(TAG, "=== loadBlessingsFromApi START ===");
        Log.d(TAG, "Selected category: " + selectedCategory);
        Log.d(TAG, "ApiClient instance: " + (apiClient != null ? "OK" : "NULL"));
        
        showLoading(true);
        Log.d(TAG, "Loading state shown");
        
        apiClient.getBlessings(selectedCategory.equals("全部") ? null : selectedCategory, 20, 
            new BlessingsApiClient.ApiCallback<List<Blessing>>() {
                @Override
                public void onSuccess(List<Blessing> blessings) {
                    Log.d(TAG, "[Callback] onSuccess called with " + blessings.size() + " items");
                    showLoading(false);
                    
                    // Cache all blessings
                    allBlessings = blessings != null ? new ArrayList<>(blessings) : new ArrayList<>();
                    Log.d(TAG, "Cached " + allBlessings.size() + " blessings");
                    
                    if (allBlessings.isEmpty()) {
                        Log.d(TAG, "Blessings list is empty, showing empty state");
                        showEmptyState("暂无内容", "点击上方\"发布\"按钮分享你的感悟");
                    } else {
                        Log.d(TAG, "Applying filter: " + currentFilter);
                        applyFilter();
                    }
                    Log.d(TAG, "[Callback] onSuccess END");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "[Callback] onError called: " + error);
                    showLoading(false);
                    String friendlyMessage = getErrorMessage(error);
                    Log.e(TAG, "Friendly error message: " + friendlyMessage);
                    showErrorState("加载失败", friendlyMessage);
                    Log.d(TAG, "[Callback] onError END");
                }
            });
        
        Log.d(TAG, "=== loadBlessingsFromApi END ===");
    }
    
    /**
     * 获取友好的错误消息
     */
    private String getErrorMessage(String error) {
        if (error.contains("网络") || error.contains("Network") || error.contains("timeout")) {
            return "网络连接失败，请检查网络设置";
        } else if (error.contains("404") || error.contains("not found")) {
            return "服务器未找到，请稍后重试";
        } else if (error.contains("500")) {
            return "服务器错误，请稍后重试";
        } else {
            return "加载失败：" + error;
        }
    }
    
    /**
     * 更新 Adapter 数据
     */
    /**
     * 设置过滤 Spinner
     */
    private void setupFilterSpinner() {
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filters
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                Log.d(TAG, "Filter changed to: " + currentFilter);
                applyFilter();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        Log.d(TAG, "Filter spinner setup complete");
    }
    
    /**
     * 从本地缓存获取点赞状态
     */
    private boolean isLocallyLiked(int blessingId) {
        return prefs.getBoolean("liked_" + blessingId, false);
    }
    
    /**
     * 从本地缓存获取收藏状态
     */
    private boolean isLocallyFavorited(int blessingId) {
        return prefs.getBoolean("favorited_" + blessingId, false);
    }
    
    /**
     * 本地保存点赞状态
     */
    private void saveLikeState(int blessingId, boolean liked) {
        prefs.edit().putBoolean("liked_" + blessingId, liked).apply();
        Log.d(TAG, "Saved like state for blessing " + blessingId + ": " + liked);
    }
    
    /**
     * 本地保存收藏状态
     */
    private void saveFavoriteState(int blessingId, boolean favorited) {
        prefs.edit().putBoolean("favorited_" + blessingId, favorited).apply();
        Log.d(TAG, "Saved favorite state for blessing " + blessingId + ": " + favorited);
    }
    
    /**
     * 应用过滤条件
     */
    private void applyFilter() {
        List<Blessing> filtered = new ArrayList<>();
        
        for (Blessing b : allBlessings) {
            boolean matchesCategory = selectedCategory.equals("全部") || selectedCategory.equals(b.category);
            boolean matchesFilter = true;
            
            // 使用本地缓存的状态进行过滤
            boolean isLiked = isLocallyLiked(b.id);
            boolean isFavorited = isLocallyFavorited(b.id);
            
            // 同步到 Blessing 对象
            b.isLiked = isLiked;
            b.isFavorited = isFavorited;
            
            switch (currentFilter) {
                case "❤️ 赞过的":
                    matchesFilter = isLiked;
                    break;
                case "⭐ 收藏的":
                    matchesFilter = isFavorited;
                    break;
            }
            
            if (matchesCategory && matchesFilter) {
                filtered.add(b);
            }
        }
        
        Log.d(TAG, "Filtered from " + allBlessings.size() + " to " + filtered.size() + " items (filter=" + currentFilter + ")");
        updateAdapter(filtered);
    }
    
    private void updateAdapter(List<Blessing> blessings) {
        List<BlessingAdapter.BlessingItem> items = new ArrayList<>();
        for (Blessing blessing : blessings) {
            items.add(BlessingAdapter.BlessingItem.fromApiModel(blessing));
        }
        adapter.setBlessings(items);
        showContentState();
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
        showContentState();
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * 显示空状态
     */
    private void showEmptyState(String title, String message) {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        
        if (emptyView != null) {
            TextView emptyText = emptyView.findViewById(R.id.emptyText);
            if (emptyText != null) emptyText.setText(title);
            emptyView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示错误状态
     */
    private void showErrorState(String title, String detail) {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        
        if (errorView != null) {
            if (errorText != null) errorText.setText(title);
            if (errorDetail != null) errorDetail.setText(detail);
            errorView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示内容状态
     */
    private void showContentState() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadBlessingsFromApi();
    }
    
    /**
     * 打开发布禅语 Activity
     */
    private void showPublishDialog() {
        Log.d(TAG, "Opening PublishBlessingActivity");
        Intent intent = new Intent(getContext(), PublishBlessingActivity.class);
        startActivity(intent);
    }
}
