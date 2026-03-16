package com.xiuxin.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.xiuxin.app.R;
import com.xiuxin.app.model.Blessing;
import com.xiuxin.app.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlessingAdapter extends RecyclerView.Adapter<BlessingAdapter.ViewHolder> {

    // 中文字体列表
    private static final String[] CHINESE_FONTS = {
        "fonts/hongleibanshujianti_2.ttf",
        "fonts/pangmenzhengdaozhenguikaiti_2.ttf",
        "fonts/qiantubifengshouxieti_2.ttf",
        "fonts/qiantuxianmoti_2.ttf",
        "fonts/sanjixingkaijianti_cu_2.ttf",
        "fonts/tanugo_round_regular.otf",
        "fonts/yangrendongzhushiti_light_2.ttf",
        "fonts/yanshixiaxingkai_2.ttf",
        "fonts/zhenzongshengdiankaishu.ttf"
    };
    
    // 背景图资源 ID 缓存（避免重复查找）
    private static final int[] BG_RESOURCE_IDS = {
        R.drawable.paper_2,
        R.drawable.paper_3,
        R.drawable.paper_low_1,
        R.drawable.paper_low_2,
        R.drawable.paper_low_3,
        R.drawable.paper_low_4,
        R.drawable.paper_low_5,
        R.drawable.paper_low_6,
        R.drawable.paper_low_7,
        R.drawable.paper_low_8,
        R.drawable.paper_low_9
    };
    
    private final Random random = new Random();
    private final Context context;

    public BlessingAdapter(Context context) {
        this.context = context;
    }

    public static class BlessingItem {
        public String text;
        public String source;
        public String practice;
        public String category;
        public int likeCount;
        public int favoriteCount;
        public boolean isLiked;
        public boolean isFavorite;
        public int id; // API ID
        public List<Comment> comments; // 评论列表
        public String fontPath; // 字体路径
        public String bgPath; // 背景图路径

        public BlessingItem(String text, String source, String practice, String category) {
            this.text = text;
            this.source = source;
            this.practice = practice;
            this.category = category;
            this.likeCount = (int) (Math.random() * 2000) + 100;
            this.favoriteCount = (int) (Math.random() * 1000) + 50;
            this.isLiked = false;
            this.isFavorite = false;
            this.id = 0;
            this.comments = new ArrayList<>();
        }
        
        /**
         * 从 API 模型创建
         */
        public static BlessingItem fromApiModel(Blessing blessing) {
            BlessingItem item = new BlessingItem(blessing.text, blessing.source, blessing.practice, blessing.category);
            item.id = blessing.id;
            item.likeCount = blessing.likeCount;
            item.favoriteCount = blessing.favoriteCount;
            item.isLiked = blessing.isLiked;
            item.isFavorite = blessing.isFavorited;
            item.comments = blessing.comments != null ? blessing.comments : new ArrayList<>();
            item.fontPath = blessing.fontPath;
            item.bgPath = blessing.bgPath;
            return item;
        }
    }

    private final List<BlessingItem> blessings = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BlessingItem item, int position);
        void onLikeClick(BlessingItem item, int position);
        void onFavoriteClick(BlessingItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setBlessings(List<BlessingItem> blessings) {
        this.blessings.clear();
        this.blessings.addAll(blessings);
        notifyDataSetChanged();
    }

    public List<BlessingItem> getBlessings() {
        return blessings;
    }

    @Override
    public int getItemCount() {
        return blessings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        // Grid layout views
        ImageView cardBackground;
        TextView cardText;
        
        // List layout views
        TextView categoryTag, blessingText, blessingSource, blessingPractice;
        TextView likeCount, favoriteCount, commentCount;
        ImageButton likeBtn, favoriteBtn, commentBtn;
        LinearLayout commentsSection;
        TextView firstCommentText, showMoreComments;
        View commentDivider;

        ViewHolder(View itemView) {
            super(itemView);
            
            // Try to find grid layout views
            cardBackground = itemView.findViewById(R.id.cardBackground);
            cardText = itemView.findViewById(R.id.cardText);
            
            // Try to find list layout views
            categoryTag = itemView.findViewById(R.id.categoryTag);
            blessingText = itemView.findViewById(R.id.blessingText);
            blessingSource = itemView.findViewById(R.id.blessingSource);
            blessingPractice = itemView.findViewById(R.id.blessingPractice);
            likeCount = itemView.findViewById(R.id.likeCount);
            favoriteCount = itemView.findViewById(R.id.favoriteCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            favoriteBtn = itemView.findViewById(R.id.favoriteBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            commentsSection = itemView.findViewById(R.id.commentsSection);
            firstCommentText = itemView.findViewById(R.id.firstCommentText);
            showMoreComments = itemView.findViewById(R.id.showMoreComments);
            commentDivider = itemView.findViewById(R.id.commentDivider);
        }

        void bind(BlessingItem item, int position) {
            // Check if this is a grid layout or list layout
            if (cardBackground != null && cardText != null) {
                // Grid layout - show card with background
                bindGrid(item, position);
            } else {
                // List layout - show full details
                bindList(item, position);
            }
        }
        
        void bindGrid(BlessingItem item, int position) {
            // 使用 Glide 加载背景图（支持缓存和异步加载）
            int bgResId;
            try {
                if (item.bgPath != null && !item.bgPath.isEmpty()) {
                    String bgName = item.bgPath.replace("drawable/", "").replace(".png", "").replace(".jpg", "");
                    int bgIndex = getBgIndex(bgName);
                    bgResId = (bgIndex >= 0 && bgIndex < BG_RESOURCE_IDS.length) 
                        ? BG_RESOURCE_IDS[bgIndex] 
                        : BG_RESOURCE_IDS[0];
                } else {
                    bgResId = BG_RESOURCE_IDS[0];
                }
            } catch (Exception e) {
                bgResId = BG_RESOURCE_IDS[0];
            }
            
            // Glide 加载：自动内存/磁盘缓存，异步加载不卡顿
            Glide.with(context)
                .load(bgResId)
                .transition(DrawableTransitionOptions.withCrossFade()) // 淡入过渡
                .centerCrop()
                .into(cardBackground);
            
            // 提取禅语文字（最多 100 字，最多 10 行）
            String text = item.text;
            if (text.length() > 100) {
                text = text.substring(0, 100) + "...";
            }
            cardText.setText(text);
            
            // 应用字体
            try {
                if (item.fontPath != null && !item.fontPath.isEmpty()) {
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), item.fontPath);
                    cardText.setTypeface(typeface);
                }
            } catch (Exception e) {
                cardText.setTypeface(null);
            }
            
            // Click listener for grid card
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, position);
            });
        }
        
        void bindList(BlessingItem item, int position) {
            categoryTag.setText(item.category);
            blessingText.setText(item.text);
            
            // 随机应用中文字体
            try {
                String randomFont = CHINESE_FONTS[random.nextInt(CHINESE_FONTS.length)];
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), randomFont);
                blessingText.setTypeface(typeface);
            } catch (Exception e) {
                // 如果字体加载失败，使用默认字体
                blessingText.setTypeface(null);
            }
            
            blessingSource.setText("—— " + item.source);
            
            // Only show practice section if it has content
            if (item.practice != null && !item.practice.trim().isEmpty()) {
                blessingPractice.setText("💡 " + item.practice);
                blessingPractice.setVisibility(View.VISIBLE);
            } else {
                blessingPractice.setVisibility(View.GONE);
            }
            
            likeCount.setText(formatCount(item.likeCount));
            favoriteCount.setText(formatCount(item.favoriteCount));
            commentCount.setText(String.valueOf(item.comments.size()));

            // Update button states
            likeBtn.setImageResource(item.isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            favoriteBtn.setImageResource(item.isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

            // Show/hide comments section
            if (item.comments != null && !item.comments.isEmpty()) {
                commentDivider.setVisibility(View.VISIBLE);
                commentsSection.setVisibility(View.VISIBLE);
                
                // Show first comment
                Comment firstComment = item.comments.get(0);
                firstCommentText.setText(firstComment.userName + ": " + firstComment.content);
                
                // Show "show more" if there are more comments
                if (item.comments.size() > 1) {
                    showMoreComments.setVisibility(View.VISIBLE);
                    showMoreComments.setText("查看全部 " + item.comments.size() + " 条评论");
                } else {
                    showMoreComments.setVisibility(View.GONE);
                }
            } else {
                commentDivider.setVisibility(View.GONE);
                commentsSection.setVisibility(View.GONE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, position);
            });

            likeBtn.setOnClickListener(v -> {
                item.isLiked = !item.isLiked;
                item.likeCount += item.isLiked ? 1 : -1;
                notifyItemChanged(position);
                if (listener != null) listener.onLikeClick(item, position);
            });

            favoriteBtn.setOnClickListener(v -> {
                item.isFavorite = !item.isFavorite;
                item.favoriteCount += item.isFavorite ? 1 : -1;
                notifyItemChanged(position);
                if (listener != null) listener.onFavoriteClick(item, position);
            });
            
            commentBtn.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, position);
            });
        }

        private String formatCount(int count) {
            if (count >= 1000) {
                return String.format("%.1fk", count / 1000.0);
            }
            return String.valueOf(count);
        }
        
        /**
         * 根据背景图名称获取索引
         */
        private int getBgIndex(String bgName) {
            switch (bgName) {
                case "paper_2": return 0;
                case "paper_3": return 1;
                case "paper_low_1": return 2;
                case "paper_low_2": return 3;
                case "paper_low_3": return 4;
                case "paper_low_4": return 5;
                case "paper_low_5": return 6;
                case "paper_low_6": return 7;
                case "paper_low_7": return 8;
                case "paper_low_8": return 9;
                case "paper_low_9": return 10;
                default: return 0;
            }
        }
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_blessing_card_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlessingItem item = blessings.get(position);
        holder.bind(item, position);
    }
}
