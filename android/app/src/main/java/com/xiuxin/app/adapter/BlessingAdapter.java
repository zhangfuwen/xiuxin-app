package com.xiuxin.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiuxin.app.R;
import com.xiuxin.app.model.Blessing;

import java.util.ArrayList;
import java.util.List;

public class BlessingAdapter extends RecyclerView.Adapter<BlessingAdapter.ViewHolder> {

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
            item.isFavorited = blessing.isFavorited;
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
        TextView likeCount, favoriteCount;
        ImageButton likeBtn, favoriteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            categoryTag = itemView.findViewById(R.id.categoryTag);
            blessingText = itemView.findViewById(R.id.blessingText);
            blessingSource = itemView.findViewById(R.id.blessingSource);
            blessingPractice = itemView.findViewById(R.id.blessingPractice);
            likeCount = itemView.findViewById(R.id.likeCount);
            favoriteCount = itemView.findViewById(R.id.favoriteCount);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            favoriteBtn = itemView.findViewById(R.id.favoriteBtn);
        }

        void bind(BlessingItem item, int position) {
            categoryTag.setText(item.category);
            blessingText.setText(item.text);
            blessingSource.setText("—— " + item.source);
            blessingPractice.setText("💡 " + item.practice);
            likeCount.setText(formatCount(item.likeCount));
            favoriteCount.setText(formatCount(item.favoriteCount));

            // Update button states
            likeBtn.setImageResource(item.isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            favoriteBtn.setImageResource(item.isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

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
        }

        private String formatCount(int count) {
            if (count >= 1000) {
                return String.format("%.1fk", count / 1000.0);
            }
            return String.valueOf(count);
        }
    }
}
