package com.example.moengageapp.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moengageapp.R;
import com.example.moengageapp.adapter.ArticleAdapterListener;
import com.example.moengageapp.adapter.ArticlesRecycleAdapter;
import com.example.moengageapp.broadcastreceiver.AlarmReceiver;
import com.example.moengageapp.model.Article;
import com.example.moengageapp.viewmodel.DBViewModel;
import com.example.moengageapp.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/*
  Main activity which displays list of articles to user.
 */


public class MainActivity extends AppCompatActivity
        implements ArticleAdapterListener,
        View.OnClickListener, AuthorFilterDialogFragment.MyListener{

    public static final String TAG = "MainActivity";
    private MainViewModel mainViewModel;
    private DBViewModel dbViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AppCompatTextView offline;
    private ArticlesRecycleAdapter adapter;
    private AppCompatImageView descendSort;
    private AppCompatImageView ascendSort;
    private AppCompatImageView filter;

    private int OFFLINE_REQ_CODE = 10;
    private int ALARM_REQ_CODE = 11;

    private List<Article> articles; // stores articles retrieved from db/network
    private HashSet<String> authorNames = new HashSet<>(); // stores unique author names
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        dbViewModel = ViewModelProviders.of(this).get(DBViewModel.class);
        initViews();
        subscribeObservers();
        setAlarm();
    }

    private void initViews() {
        offline = findViewById(R.id.offline_txt);
        offline.setOnClickListener(this);
        progressBar = findViewById(R.id.progress_bar);
        filter = findViewById(R.id.filter);
        descendSort = findViewById(R.id.descend_sort);
        ascendSort = findViewById(R.id.ascend_sort);
        filter.setOnClickListener(this);
        descendSort.setOnClickListener(this);
        ascendSort.setOnClickListener(this);
        filter.setOnClickListener(this);

        recyclerView = findViewById(R.id.recycler_view);
        adapter = new ArticlesRecycleAdapter(this, TAG);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    /*
    this method observes events emitted by MainViewModel.
    States -
    1. LOADING - shows progress bar on the app.
    2. CACHE - removes progress bar and displays articles retrieved from local database.
    3. ERROR - removes progress bar and show toast message to user about the error.
    4. SUCCESS - removes progress bar and displays articles retrieved from network.
     */
    private void subscribeObservers(){
        mainViewModel.observeArticles().observe(this, listResource -> {
            if(listResource != null){
                switch (listResource.status){

                    case LOADING:{
                        progressBar.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onChanged: LOADING...");
                        break;
                    }

                    case SUCCESS:{
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onChanged: got posts...");
                        adapter.setArticles(listResource.data);
                        articles = listResource.data;
                        addAuthorNamesToSet(listResource.data);
                        break;
                    }

                    case ERROR:{
                        progressBar.setVisibility(View.GONE);
                        if (!TextUtils.isEmpty(listResource.message))
                            Toast.makeText(MainActivity.this, listResource.message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onChanged: ERROR..." + listResource.message );
                        break;
                    }

                    case CACHE: {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onChanged: CACHE got posts...");
                        adapter.setArticles(listResource.data);
                        articles = listResource.data;
                        addAuthorNamesToSet(listResource.data);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onArticleClicked(Article article) {
        Intent browserIntent = new Intent(this, BrowserActivity.class);
        browserIntent.putExtra("url", article.getUrl());
        startActivity(browserIntent);
    }

    /*
    adds article to local db for offline reading.
     */
    @Override
    public void addArticleToOffline(Article article, int position) {
        dbViewModel.insertArticle(article).observe(this, resource -> {
            if(resource != null){
                switch (resource.status) {

                    case LOADING: {
                        adapter.showProgress(article);
                        break;
                    }

                    case SUCCESS:{
                        Log.d(TAG, "onInserted: got article...");
                        adapter.articleAdded(article);
                        Toast.makeText(MainActivity.this, "Article added to offline db", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case ERROR:{
                        Log.e(TAG, "onInserted: ERROR..." + resource.message );
                        adapter.hideProgress(article);
                        Toast.makeText(MainActivity.this, "Error in adding to offline db", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
    }

    /*
    removes article from local db.
     */
    @Override
    public void removeArticleFromOffline(Article article, int position) {
        dbViewModel.deleteArticle(article).observe(this, resource -> {
            if(resource != null){
                switch (resource.status) {

                    case LOADING: {
                        adapter.showProgress(article);
                        break;
                    }

                    case SUCCESS:{
                        Log.d(TAG, "onDeleted: got article...");
                        adapter.articleRemoved(article);
                        Toast.makeText(MainActivity.this, "Article deleted from offline db", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case ERROR:{
                        Log.e(TAG, "onDeleted: ERROR..." + resource.message );
                        adapter.hideProgress(article);
                        Toast.makeText(MainActivity.this, "Error in deleting from offline db", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.offline_txt : {
                Intent intent = new Intent(this, OfflineActivity.class);
                startActivityForResult(intent, OFFLINE_REQ_CODE);
                break;
            }
            case R.id.filter : {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment prev = fragmentManager.findFragmentByTag("dialog");
                if (prev != null) {
                    transaction.remove(prev);
                }
                transaction.addToBackStack(null);
                AuthorFilterDialogFragment dialogFragment = AuthorFilterDialogFragment.newInstance(new ArrayList<>(authorNames));
                dialogFragment.show(transaction, "dialog");
                break;
            }

            case R.id.descend_sort : {
                adapter.sortInDescendingOrder();
                break;
            }
            case R.id.ascend_sort : {
                adapter.sortInAscendingOrder();
                break;
            }
        }
    }

    /*
    re-updating articles list.
    User can delete some articles from OfflineActivity, so updating plus/tick button in articles' list accordingly
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OFFLINE_REQ_CODE) {
            dbViewModel.getArticlesFromDb().observe(this, listResource -> {
                switch (listResource.status) {
                    case SUCCESS:{
                        adapter.updateOfflineViewOfArticles(listResource.data);
                        break;
                    }

                }
            });
        }
    }

    /*
    this sets the repeating alarm to fetch articles periodically after 1 hour.
     */
    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent  = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQ_CODE, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            PendingIntent pi = PendingIntent.getBroadcast(this, ALARM_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 60 * 60 * 1000, 60 * 60 * 1000, pi);
        }
    }


    private void addAuthorNamesToSet(List<Article> data) {
        authorNames.clear();
        for (Article article : data) {
            if (!TextUtils.isEmpty(article.getAuthor())) {
                authorNames.add(article.getAuthor());
            }
        }
    }

    /*
    update articles' adapter with list of filtered articles according to selected author names.
     */
    @Override
    public void doPositiveClick(List<String> checkedAuthors) {
        if (checkedAuthors.size() == 0) {
            adapter.setArticles(articles);
        } else {
            List<Article> filteredList = new ArrayList<>();
            for (String author : checkedAuthors) {
                for (Article article : articles) {
                    if (author.equals(article.getAuthor())) {
                        filteredList.add(article);
                    }
                }
            }
            adapter.setArticles(filteredList);
        }
    }

    @Override
    public void doNegativeClick() {

    }
}
