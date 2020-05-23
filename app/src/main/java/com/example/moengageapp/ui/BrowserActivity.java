package com.example.moengageapp.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.example.moengageapp.R;
import com.example.moengageapp.utils.Utility;

/*
Activity to that loads url/html page in WebView.
 */

public class BrowserActivity extends AppCompatActivity {

    private WebView webView;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        webView = findViewById(R.id.web_view);

        url = getIntent().getStringExtra("url");

        initWebView();
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url.replace("http://", "https://"));
        } else if (!TextUtils.isEmpty(Utility.getHtmlPageFromSharedPreferences(this))) {
            String htmlPage = Utility.getHtmlPageFromSharedPreferences(this);
            webView.loadData(htmlPage, "text/html; charset=UTF-8", null);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        AppCompatImageView backBtn = toolbar.findViewById(R.id.image_back_button);
        backBtn.setOnClickListener(v -> finish());
    }

    private void initWebView() {
        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                super.shouldOverrideUrlLoading(view, url);
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        };
        webView.setWebViewClient(client);
    }
}
