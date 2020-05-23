package com.example.moengageapp.viewmodel;

import android.app.Application;
import android.database.Cursor;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.moengageapp.db.ArticleContract;
import com.example.moengageapp.db.DBManager;
import com.example.moengageapp.model.Article;
import com.example.moengageapp.network.NetworkCall;
import com.example.moengageapp.network.Resource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "MainViewModel";

    private NetworkCall networkCall;
    private DBManager dbManager;

    private MediatorLiveData<Resource<List<Article>>> articles;

    public MainViewModel(Application application) {
        super(application);
        dbManager = new DBManager(application.getApplicationContext());
        dbManager.open();
        networkCall = new NetworkCall();
    }

    /*
    this method fetches articles from local db and network and emit following events -
    1. LOADING for fetching from db
    2. if db fetch successful, CACHE else ERROR
    3. LOADING for fetching from network
    4. if network fetch successful, SUCCESS else ERROR
     */

    public LiveData<Resource<List<Article>>> observeArticles(){
        if (articles == null) {
            articles = new MediatorLiveData<>();
            articles.setValue(Resource.loading((List<Article>)null));

            final LiveData<Resource<List<Article>>> source = LiveDataReactiveStreams.fromPublisher(

                    getArticlesFromDbFlowable()
                            .onErrorReturn(throwable -> {
                                Log.e(TAG, "apply: ", throwable);
                                return null;
                            })
                            .map( articles -> {
                                if (articles == null || articles.size() == 0) {
                                    return Resource.error("", (List<Article>)null);
                                }
                                return Resource.cache(articles);
                            })
                            .subscribeOn(Schedulers.io())
            );

            articles.addSource(source, new Observer<Resource<List<Article>>>() {
                @Override
                public void onChanged(Resource<List<Article>> listResource) {
                    articles.setValue(listResource);
                    articles.removeSource(source);
                    fetchFromNetwork();
                }
            });
        }
        return articles;
    }

    public void fetchFromNetwork() {
        articles.setValue(Resource.loading((List<Article>) null));

        final LiveData<Resource<List<Article>>> source = LiveDataReactiveStreams.fromPublisher(
                getArticlesFromNetworkObservable()
                        .toFlowable(BackpressureStrategy.BUFFER)
                        .onErrorReturn(throwable -> {
                            Log.e(TAG, "apply: ", throwable);
                            return null;
                        })
                        .map( articles -> {
                            if (articles == null || articles.size() == 0) {
                                return Resource.error("Error in fetching articles", (List<Article>)null);
                            }
                            return Resource.success(articles);
                        })
                        .subscribeOn(Schedulers.io())
        );

        articles.addSource(source, new Observer<Resource<List<Article>>>() {
            @Override
            public void onChanged(Resource<List<Article>> listResource) {
                articles.setValue(listResource);
                articles.removeSource(source);
            }
        });
    }

    private Observable<List<Article>> getArticlesFromNetworkObservable(){
        return Observable.create(emitter -> {
            try {
                String response = networkCall.getContentFromUrl("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json");

                JSONObject jsonObject;
                jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("articles");
                Gson gson = new Gson();
                Type type = new TypeToken<List<Article>>(){}.getType();
                List<Article> articles = gson.fromJson(String.valueOf(jsonArray), type);
                for (int i = 1; i <= articles.size(); i++) {
                    articles.get(i-1).setId((long) i);
                }
                emitter.onNext(articles);
                emitter.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onNext(new ArrayList<Article>());
            }
        });
    }

    private Flowable<List<Article>> getArticlesFromDbFlowable() {
        return Flowable.create(emitter -> {
            Cursor cursor = dbManager.queryFromSyncTable();
            List<Article> articles = new ArrayList<>();
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_ID));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_AUTHOR));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_URL));
                String urlToImage = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_URL_TO_IMAGE));
                String publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_PUBLISHED_AT));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_CONTENT));
                String downloadedHttpPage = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.SyncArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE));

                Article article = new Article();
                article.setId(id);
                article.setAuthor(author);
                article.setTitle(title);
                article.setDescription(description);
                article.setUrl(url);
                article.setUrlToImage(urlToImage);
                article.setPublishedAt(publishedAt);
                article.setContent(content);
                article.setDownloadHttpPage(downloadedHttpPage);
                articles.add(article);
            }
            emitter.onNext(articles);
            emitter.onComplete();
            cursor.close();
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dbManager.close();
    }
}
