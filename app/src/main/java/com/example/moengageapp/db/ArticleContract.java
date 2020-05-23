package com.example.moengageapp.db;

public final class ArticleContract {
    private ArticleContract() {}
    public static class ArticleEntry {
        public static final String OFFLINE_TABLE_NAME = "offline_articles";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_URL_TO_IMAGE = "url_to_image";
        public static final String COLUMN_NAME_PUBLISHED_AT = "published_at";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_DOWNLOADED_HTTP_PAGE = "downloaded_http_page";
    }

    public static class SyncArticleEntry {
        public static final String SYNC_TABLE_NAME = "sync_articles";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_URL_TO_IMAGE = "url_to_image";
        public static final String COLUMN_NAME_PUBLISHED_AT = "published_at";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_DOWNLOADED_HTTP_PAGE = "downloaded_http_page";
    }
}
