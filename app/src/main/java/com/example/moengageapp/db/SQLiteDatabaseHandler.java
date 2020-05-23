package com.example.moengageapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Article.db";

    private static final String SQL_CREATE_OFFLINE_ENTRIES =
            "CREATE TABLE " + ArticleContract.ArticleEntry.OFFLINE_TABLE_NAME + " (" +
                    ArticleContract.ArticleEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_TITLE + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_URL + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_URL_TO_IMAGE + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_CONTENT + " TEXT," +
                    ArticleContract.ArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE + " TEXT)";

    private static final String SQL_DELETE_OFFLINE_ENTRIES =
            "DROP TABLE IF EXISTS " + ArticleContract.ArticleEntry.OFFLINE_TABLE_NAME;

    private static final String SQL_CREATE_SYNC_ENTRIES =
            "CREATE TABLE " + ArticleContract.SyncArticleEntry.SYNC_TABLE_NAME + " (" +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_TITLE + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_URL + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_URL_TO_IMAGE + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_PUBLISHED_AT + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_CONTENT + " TEXT," +
                    ArticleContract.SyncArticleEntry.COLUMN_NAME_DOWNLOADED_HTTP_PAGE + " TEXT)";

    private static final String SQL_DELETE_SYNC_ENTRIES =
            "DROP TABLE IF EXISTS " + ArticleContract.SyncArticleEntry.SYNC_TABLE_NAME;

    public SQLiteDatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_OFFLINE_ENTRIES);
        db.execSQL(SQL_CREATE_SYNC_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_OFFLINE_ENTRIES);
        db.execSQL(SQL_DELETE_SYNC_ENTRIES);
        onCreate(db);
    }
}
