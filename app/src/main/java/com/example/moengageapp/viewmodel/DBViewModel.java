package com.example.moengageapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moengageapp.model.Article;
import com.example.moengageapp.network.Resource;
import com.example.moengageapp.repository.DBRepository;

import java.util.List;

public class DBViewModel extends AndroidViewModel {

    private static final String TAG = "DBViewModel";
    private DBRepository dbRepository;

    public DBViewModel(@NonNull Application application) {
        super(application);
        dbRepository = new DBRepository(application.getApplicationContext());
    }

    public LiveData<Resource<Article>> insertArticle(Article article) {
        return dbRepository.insertArticle(article);
    }

    public LiveData<Resource<Article>> deleteArticle(Article article) {
        return dbRepository.deleteArticle(article);
    }

    public LiveData<Resource<List<Article>>> getArticlesFromDb() {
        return dbRepository.getArticles();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dbRepository.clear();
    }
}
