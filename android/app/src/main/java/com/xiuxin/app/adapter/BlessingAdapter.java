package com.xiuxin.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_blessing_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlessingItem item = blessings.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return blessings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTag, blessingText, blessingSource, blessingPractice;
        TextView likeCount, favoriteCount, commentCount;
        ImageButton likeBtn, favoriteBtn, commentBtn;
        LinearLayout commentsSection;
        TextView firstCommentText, showMoreComments;
        View commentDivider;

        ViewHolder(View itemView) {
            super(itemView);
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
    }
}
