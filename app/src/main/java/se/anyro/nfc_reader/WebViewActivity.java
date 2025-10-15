package com.weijian.li;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WebViewActivity extends Activity {

    private WebView mWebView;
    private EditText mUrlField;
    private LinearLayout mControlBar;
    private ConfigManager mConfigManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);
        
        // 初始化配置管理器
        mConfigManager = ConfigManager.getInstance();
        mConfigManager.loadConfig(this);
        
        // 根据配置决定是否显示顶栏
        if (mConfigManager.shouldShowActionBar()) {
            // 设置标题居中
            android.app.ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setCustomView(R.layout.actionbar_title_layout);
            }
        } else {
            // 隐藏顶栏
            android.app.ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
        
        mWebView = findViewById(R.id.web_view);
        mUrlField = findViewById(R.id.url_field);
        Button btnBack = findViewById(R.id.btn_back);
        Button btnRefresh = findViewById(R.id.btn_refresh);
        mControlBar = findViewById(R.id.control_bar);
        
        // 根据配置控制地址栏显示
        if (!mConfigManager.shouldShowAddressBar()) {
            mControlBar.setVisibility(View.GONE);
        }
        
        // 配置WebView
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setDomStorageEnabled(true); // 启用DOM存储
        webSettings.setLoadWithOverviewMode(true); // 适应屏幕
        webSettings.setUseWideViewPort(true); // 使用宽视图
        
        // 设置WebViewClient以在应用内打开链接
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                mUrlField.setText(url);
                return true;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mUrlField.setText(url);
            }
        });
        
        // 加载默认URL或从Intent获取的URL
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            mUrlField.setText(url);
            mWebView.loadUrl(url);
        } else {
            mWebView.loadUrl(mConfigManager.getDefaultUrl());
        }
        
        // 返回按钮点击事件 - 直接执行系统返回
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接调用系统返回
                onBackPressed();
            }
        });
        
        // 刷新按钮点击事件
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mUrlField.getText().toString().trim();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                mWebView.loadUrl(url);
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        // 直接执行系统返回，不处理WebView的返回逻辑
        super.onBackPressed();
    }
}