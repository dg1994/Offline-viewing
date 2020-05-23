package com.example.moengageapp.repository;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MediatorLiveData;

import com.example.moengageapp.db.ArticleContract;
import com.example.moengageapp.db.DBManager;
import com.example.moengageapp.model.Article;
import com.example.moengageapp.network.DownloadHttpPage;
import com.example.moengageapp.network.Resource;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class DBRepository {

    private static final String TAG = "DBRepository";

    private DBManager dbManager;
    private MediatorLiveData<Resource<Article>> insertArticle;
    private MediatorLiveData<Resource<Article>> deleteArticle;
    private MediatorLiveData<Resource<List<Article>>> listArticles;

    public DBRepository(Context context) {
        dbManager = new DBManager(context);
        dbManager.open();
    }

    /*
    Streams of events -
    1. LOADING
    2. ERROR, if error in fetching from offline table db.
    3. SUCCESS, if success
     */
    public LiveData<Resource<List<Article>>> getArticles() {
        listArticles = new MediatorLiveData<>();
        listArticles.setValue(Resource.loading((List<Article>)null));
        final LiveData<Resource<List<Article>>> source = LiveDataReactiveStreams.fromPublisher(

                getArticlesFlowable()
                        .onErrorReturn(throwable -> {
                            Log.e(TAG, "apply: ", throwable);
                            return null;
                        })
                        .map( a -> {
                            if (a == null) {
                                return Resource.error("Something went wrong", (List<Article>)null);
                            }
                            return Resource.success(a);
                        })
                        .subscribeOn(Schedulers.io())
        );

        listArticles.addSource(source, articleResource -> {
            listArticles.setValue(articleResource);
            listArticles.removeSource(source);
        });

        return listArticles;
    }

    /*
    Streams of events -
    1. LOADING
    2. ERROR, if error in downloading html page or inserting in offline table db.
    3. SUCCESS, if success
     */

    public LiveData<Resource<Article>> insertArticle(Article article) {
        insertArticle = new MediatorLiveData<>();
        insertArticle.setValue(Resource.loading((Article) null));
        final LiveData<Resource<Article>> source = LiveDataReactiveStreams.fromPublisher(

                downloadHttpPage(article)
                .subscribeOn(Schedulers.io())
                .flatMap(downloadString -> {
                    if (TextUtils.isEmpty(downloadString)) {
                        return Flowable.just("").map( s -> Resource.error("Something went wrong", (Article) null));
                    } else {
                        article.setDownloadHttpPage(downloadString);
                        return insertArticleFlowable(article)
                                .onErrorReturn(throwable -> {
                                    Log.e(TAG, "apply: ", throwable);
                                    return null;
                                })
                                .map(a -> {
                                    if (a == null) {
                                        return Resource.error("Something went wrong", (Article) null);
                                    }
                                    return Resource.success(a);
                                })
                                .subscribeOn(Schedulers.io());
                    }
                }
                ).onErrorReturn(throwable -> {
                    Log.e(TAG, "apply: ", throwable);
                    return Resource.error("Something went wrong", (Article)null);
                })
                .subscribeOn(Schedulers.io())
        );

        insertArticle.addSource(source, articleResource -> {
            insertArticle.setValue(articleResource);
            insertArticle.removeSource(source);
        });

        return insertArticle;
    }

    /*
    Streams of events -
    1. ERROR, if error in deleting from offline table db.
    3. SUCCESS, if success
     */
    public LiveData<Resource<Article>> deleteArticle(Article article) {
        deleteArticle = new MediatorLiveData<>();
        final LiveData<Resource<Article>> source = LiveDataReactiveStreams.fromPublisher(

                deleteArticleFlowable(article)
                        .onErrorReturn(throwable -> {
                            Log.e(TAG, "apply: ", throwable);
                            return null;
                        })
                        .map( a -> {
                            if (a == null) {
                                return Resource.error("Something went wrong", (Article)null);
                            }
                            return Resource.success(a);
                        })
                        .subscribeOn(Schedulers.io())
        );

        deleteArticle.addSource(source, articleResource -> {
            deleteArticle.setValue(articleResource);
            deleteArticle.removeSource(source);
        });

        return deleteArticle;
    }

    /*
    It creates flowable of fetching articles from offline db.
     */
    private Flowable<List<Article>> getArticlesFlowable() {
        return Flowable.create(emitter -> {
            Cursor cursor = dbManager.queryFromOfflineTable();
            List<Article> articles = new ArrayList<>();
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_ID));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_URL));
                String urlToImage = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_URL_TO_IMAGE));
                String publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_CONTENT));
                String downloadedHttpPage = cursor.getString(cursor.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE));

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

    /*
    It creates flowable of inserting article into offline db.
     */
    private Flowable<Article> insertArticleFlowable(Article article){

        return Flowable.create(emitter -> {
            dbManager.insertIntoOfflineTable(article.getId(), article.getAuthor(), article.getTitle(), article.getDescription(),
                    article.getUrl(), article.getUrlToImage(), article.getPublishedAt(), article.getContent(),
                    article.getDownloadHttpPage());
            emitter.onNext(article);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    /*
    It creates flowable of deleting article from offline db.
     */
    private Flowable<Article> deleteArticleFlowable(Article article) {
        return Flowable.create(emitter -> {
            dbManager.deleteFromOfflineTable(article.getId());
            emitter.onNext(article);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    /*
    this method creates Flowable of downloading article's html page.
     */
    private Flowable<String> downloadHttpPage(Article article) {
        return Flowable.create(emitter -> {
            DownloadHttpPage downloadHttpPage = new DownloadHttpPage(article.getUrl().replace("http://", "https://"));
            String downloadString = downloadHttpPage.getPage();
            emitter.onNext(downloadString);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }


    public void clear() {
        dbManager.close();
    }
}
