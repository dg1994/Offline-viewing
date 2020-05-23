package com.example.moengageapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class DBManager {
    private SQLiteDatabaseHandler databaseHandler;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public DBManager(Context context) {
        this.context = context;
    }

    public DBManager open() throws SQLException {
        databaseHandler = new SQLiteDatabaseHandler(context);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        return this;
    }

    public void close() {
        sqLiteDatabase.close();
    }

    public long insertIntoOfflineTable(long id, String author, String title, String desc, String url, String url_to_image,
                       String publishedAt, String content, String downloadedHttpPage) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_ID, id);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR, author);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_TITLE, title);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_DESCRIPTION, desc);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_URL, url);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_URL_TO_IMAGE, url_to_image);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT, publishedAt);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_CONTENT, content);
        contentValue.put(ArticleContract.ArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE, downloadedHttpPage);

        return sqLiteDatabase.insert(ArticleContract.ArticleEntry.OFFLINE_TABLE_NAME, null, contentValue);
    }

    public Cursor queryFromOfflineTable() {
        String[] columns = new String[] {
                ArticleContract.ArticleEntry.COLUMN_NAME_ID,
                ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR,
                ArticleContract.ArticleEntry.COLUMN_NAME_TITLE,
                ArticleContract.ArticleEntry.COLUMN_NAME_DESCRIPTION,
                ArticleContract.ArticleEntry.COLUMN_NAME_URL,
                ArticleContract.ArticleEntry.COLUMN_NAME_URL_TO_IMAGE,
                ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT,
                ArticleContract.ArticleEntry.COLUMN_NAME_CONTENT,
                ArticleContract.ArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE
        };
        Cursor cursor = sqLiteDatabase.query(ArticleContract.ArticleEntry.OFFLINE_TABLE_NAME, columns, null, null, null, null, null);
        return cursor;
    }

    public void deleteFromOfflineTable(long _id) {
        sqLiteDatabase.delete(ArticleContract.ArticleEntry.OFFLINE_TABLE_NAME, ArticleContract.ArticleEntry.COLUMN_NAME_ID + "=" + _id, null);
    }

    public long insertIntoSyncTable(long id, String author, String title, String desc, String url, String url_to_image,
                                       String publishedAt, String content, String downloadedHttpPage) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_ID, id);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_AUTHOR, author);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_TITLE, title);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_DESCRIPTION, desc);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_URL, url);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_URL_TO_IMAGE, url_to_image);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_PUBLISHED_AT, publishedAt);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_CONTENT, content);
        contentValue.put(ArticleContract.SyncArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE, downloadedHttpPage);

        return sqLiteDatabase.insert(ArticleContract.SyncArticleEntry.SYNC_TABLE_NAME, null, contentValue);
    }

    public Cursor queryFromSyncTable() {
        String[] columns = new String[] {
                ArticleContract.SyncArticleEntry.COLUMN_NAME_ID,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_AUTHOR,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_TITLE,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_DESCRIPTION,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_URL,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_URL_TO_IMAGE,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_PUBLISHED_AT,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_CONTENT,
                ArticleContract.SyncArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE
        };
        Cursor cursor = sqLiteDatabase.query(ArticleContract.SyncArticleEntry.SYNC_TABLE_NAME, columns, null, null, null, null, null);
        return cursor;
    }

    public void deleteFromSyncTable(long _id) {
        sqLiteDatabase.delete(ArticleContract.SyncArticleEntry.SYNC_TABLE_NAME, ArticleContract.SyncArticleEntry.COLUMN_NAME_ID + "=" + _id, null);
    }
}
