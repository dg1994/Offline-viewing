package com.example.moengageapp.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moengageapp.R;
import com.example.moengageapp.model.Article;
import com.example.moengageapp.ui.MainActivity;
import com.example.moengageapp.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArticlesRecycleAdapter extends RecyclerView.Adapter<ArticlesRecycleAdapter.ArticleViewHolder>{
    private List<Article> articles = new ArrayList<>();
    private ArticleAdapterListener articleAdapterListener;
    private ArrayList<Long> offlineStoredArticleIds;
    private ArrayList<Long> progressArticleIds;
    private String origin;

    public ArticlesRecycleAdapter(ArticleAdapterListener articleAdapterListener, String TAG) {
        this.articleAdapterListener = articleAdapterListener;
        offlineStoredArticleIds = new ArrayList<>();
        progressArticleIds = new ArrayList<>();
        origin = TAG;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article_list_item, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);

        if (!TextUtils.isEmpty(article.getAuthor())) {
            holder.author.setVisibility(View.VISIBLE);
            holder.author.setText(article.getAuthor());
        } else {
            holder.author.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext())
                .load(article.getUrlToImage())
                .error(R.mipmap.ic_launcher)
                .into(holder.image);
        holder.title.setText(article.getTitle());
        holder.rootLayout.setOnClickListener(v -> {
            articleAdapterListener.onArticleClicked(article);
        });

        if (!TextUtils.isEmpty(article.getPublishedAt()))
            holder.time.setText(Utility.getPolishedDate(article.getPublishedAt()));

        if (MainActivity.TAG.equals(origin)) {
            if (article.getId() != null && offlineStoredArticleIds.contains(article.getId())) {
                holder.offline.setImageResource(R.drawable.select);
                holder.offline.setOnClickListener(v -> {
                    articleAdapterListener.removeArticleFromOffline(article, position);
                });
            } else {
                holder.offline.setImageResource(R.drawable.add_offline_icon);
                holder.offline.setOnClickListener(v -> {
                    articleAdapterListener.addArticleToOffline(article, position);
                });
            }
        } else {
            holder.offline.setImageResource(R.drawable.ic_delete_black_36dp);
            holder.offline.setOnClickListener(v -> {
                articleAdapterListener.removeArticleFromOffline(article, position);
            });
        }

        if (progressArticleIds.contains(article.getId())) {
            holder.bar.setVisibility(View.VISIBLE);
            holder.offline.setEnabled(false);
        } else {
            holder.offline.setEnabled(true);
            holder.bar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<Article> articles){
        this.articles = articles;
        notifyDataSetChanged();
    }

    public void showProgress(Article article) {
        int index = getArticlePosition(article.getId());
        progressArticleIds.add(article.getId());
        notifyItemChanged(index);
    }

    public void hideProgress(Article article) {
        int index = getArticlePosition(article.getId());
        progressArticleIds.remove(article.getId());
        notifyItemChanged(index);
    }

    public void articleAdded(Article article) {
        int index = getArticlePosition(article.getId());
        progressArticleIds.remove(article.getId());
        offlineStoredArticleIds.add(article.getId());
        articles.set(index, article);
        notifyItemChanged(index);
    }

    public void articleRemoved(Article article) {
        int index = getArticlePosition(article.getId());
        progressArticleIds.remove(article.getId());
        offlineStoredArticleIds.remove(article.getId());
        notifyItemChanged(index);
    }

    public void articleDeleted(Article article) {
        int index = getArticlePosition(article.getId());
        articles.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, articles.size());
    }

    public void updateOfflineViewOfArticles(List<Article> articles) {
        offlineStoredArticleIds.clear();
        for (Article article : articles) {
            offlineStoredArticleIds.add(article.getId());
        }
        notifyDataSetChanged();
    }

    public void sortInAscendingOrder() {
        Collections.sort(articles, (o1, o2) -> (int) (Utility.convertDateStringToEpochMillis(o1.getPublishedAt()) -
                Utility.convertDateStringToEpochMillis(o2.getPublishedAt())));
        notifyDataSetChanged();
    }

    public void sortInDescendingOrder() {
        Collections.sort(articles, (o1, o2) -> (int)(Utility.convertDateStringToEpochMillis(o2.getPublishedAt()) -
                Utility.convertDateStringToEpochMillis(o1.getPublishedAt()))
        );
        notifyDataSetChanged();
    }

    public int getArticlePosition(long articleId) {
        int pos = 0;
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).getId() == articleId) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder{

        View rootLayout;
        AppCompatImageView image;
        AppCompatTextView title;
        AppCompatTextView time;
        AppCompatImageView offline;
        ProgressBar bar;
        AppCompatTextView author;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.article_layout);
            image = itemView.findViewById(R.id.article_img);
            title = itemView.findViewById(R.id.article_title);
            time = itemView.findViewById(R.id.article_time);
            offline = itemView.findViewById(R.id.offline_img);
            bar = itemView.findViewById(R.id.progress_bar);
            author = itemView.findViewById(R.id.article_author);
        }
    }
}
