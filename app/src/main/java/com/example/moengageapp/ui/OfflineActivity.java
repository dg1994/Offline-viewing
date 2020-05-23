package com.example.moengageapp.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moengageapp.R;
import com.example.moengageapp.adapter.ArticleAdapterListener;
import com.example.moengageapp.adapter.ArticlesRecycleAdapter;
import com.example.moengageapp.utils.Utility;
import com.example.moengageapp.model.Article;
import com.example.moengageapp.viewmodel.DBViewModel;

/*
Displays list of stored articles by user for offline viewing.
 */

public class OfflineActivity extends AppCompatActivity implements ArticleAdapterListener {
    public static final String TAG = "OfflineActivity";
    private DBViewModel dbViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AppCompatImageView backBtn;
    private ArticlesRecycleAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        dbViewModel = ViewModelProviders.of(this).get(DBViewModel.class);
        initViews();
        subscribeObservers();
    }

    private void initViews() {
        backBtn = findViewById(R.id.image_back_button);
        backBtn.setOnClickListener(v -> onBackPressed());
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new ArticlesRecycleAdapter(this, TAG);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void subscribeObservers(){
        dbViewModel.getArticlesFromDb().observe(this, listResource -> {
            if (listResource != null) {
                switch (listResource.status) {

                    case LOADING:{
                        progressBar.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onChanged: LOADING...");
                        break;
                    }

                    case SUCCESS:{
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onChanged: got posts...");
                        adapter.setArticles(listResource.data);
                        break;
                    }

                    case ERROR:{
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onChanged: ERROR..." + listResource.message );
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onArticleClicked(Article article) {
        try {
            Intent browserIntent = new Intent(this, BrowserActivity.class);
            //Uri uri = Utility.writeToFile(article.getDownloadHttpPage());
            Utility.saveHtmlPageInSharedPreferences(this, article.getDownloadHttpPage());
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addArticleToOffline(Article article, int position) {

    }

    @Override
    public void removeArticleFromOffline(Article article, int position) {
        dbViewModel.deleteArticle(article).observe(this, resource -> {
            if(resource != null){
                switch (resource.status){
                    case SUCCESS:{
                        Log.d(TAG, "onDeleted: got article...");
                        adapter.articleDeleted(article);
                        Toast.makeText(OfflineActivity.this, "Article deleted from offline db", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case ERROR:{
                        Log.e(TAG, "onDeleted: ERROR..." + resource.message );
                        Toast.makeText(OfflineActivity.this, "Error in deleting from offline db", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
    }
}
