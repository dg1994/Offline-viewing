package com.example.moengageapp.network;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

public class ArticleSyncTask extends AsyncTask<String, Void, AsyncTaskResult<String>> {

    private SyncTaskListener syncTaskListener;
    private NetworkCall networkCall;

    public ArticleSyncTask(SyncTaskListener syncTaskListener) {
        this.syncTaskListener = syncTaskListener;
        networkCall = new NetworkCall();
    }

    @Override
    protected AsyncTaskResult<String> doInBackground(String... strings) {

        try {
            String articles = networkCall.getContentFromUrl(strings[0]);
            return new AsyncTaskResult<>(articles);
        } catch (IOException e) {
            e.printStackTrace();
            return new AsyncTaskResult<>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<String> result) {
        super.onPostExecute(result);
        if (result.getException() != null) {
            syncTaskListener.onArticlesFailedToLoad(result.getException());
        } else {
            syncTaskListener.onArticlesLoaded(result.getResult());
        }
    }
}
