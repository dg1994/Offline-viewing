package com.example.moengageapp.network;

public interface SyncTaskListener {
    void onArticlesLoaded(String articles);

    void onArticlesFailedToLoad(Throwable e);
}
