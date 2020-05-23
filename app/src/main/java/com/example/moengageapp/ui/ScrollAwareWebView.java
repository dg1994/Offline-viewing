package com.example.moengageapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.annotation.Nullable;

public class ScrollAwareWebView extends WebView {

    @Nullable
    private GestureDetector gestureDetector;

    public ScrollAwareWebView(Context context) {
        super(context);
    }
    public ScrollAwareWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ScrollAwareWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            return gestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }

    public void setGestureDetector(@Nullable GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }
}