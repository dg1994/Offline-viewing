package com.example.moengageapp.adapter;

import com.example.moengageapp.model.Article;

public interface ArticleAdapterListener {
    void onArticleClicked(Article article);
    void addArticleToOffline(Article article, int position);
    void removeArticleFromOffline(Article article, int position);
}
