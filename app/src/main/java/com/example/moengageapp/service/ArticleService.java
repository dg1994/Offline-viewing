package com.example.moengageapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.moengageapp.R;
import com.example.moengageapp.db.DBManager;
import com.example.moengageapp.model.Article;
import com.example.moengageapp.network.NetworkCall;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/*
Service responsible to fetch articles from network and adding them to local database.
Shows notification while fetching data.
 */

public class ArticleService extends Service {

    private static final String TAG = "AlarmService";
    // Unique Notification ID
    private static final int NOTIFICATION_ID = 1001;

    NotificationCompat.Builder mBuilder;
    NotificationManager notificationManager;
    private DBManager dbManager;
    private NetworkCall networkCall;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        compositeDisposable = new CompositeDisposable();
        dbManager = new DBManager(this);
        dbManager.open();
        networkCall = new NetworkCall();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Sync";
            String description = "Sync notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_download_channel_id), name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(this, getString(R.string.default_download_channel_id));
        mBuilder.setContentTitle("Syncing")
                .setContentText("Loading Content")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(null)
                .setProgress(100, 0, true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        startForeground(NOTIFICATION_ID , mBuilder.build());

        startSync();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbManager.close();
        compositeDisposable.dispose();
    }

    /*
    this method takes care of fetching articles from api and adding it to local db.
     */
    private void startSync() {
        Disposable disposable = getArticlesObservable()
                .subscribeOn(Schedulers.io())
                .concatMapIterable(list -> list)
                .concatMap(this::insertArticleObservable)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    stopForeground(true);
                    stopSelf();
                }, e -> {
                    stopForeground(true);
                    stopSelf();
                });
        compositeDisposable.add(disposable);
    }

    private Observable<List<Article>> getArticlesObservable(){
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
                emitter.onError(e);
            }
        });
    }

    private Observable<Article> insertArticleObservable(Article article){
        return Observable.create(emitter -> {
            try {
                dbManager.insertIntoSyncTable(article.getId(), article.getAuthor(), article.getTitle(), article.getDescription(),
                        article.getUrl(), article.getUrlToImage(), article.getPublishedAt(), article.getContent(),
                        article.getDownloadHttpPage());
                emitter.onNext(article);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
