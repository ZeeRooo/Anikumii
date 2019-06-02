package com.zeerooo.anikumii.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;

public class SplashScreen extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setWillNotDraw(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(Anikumii.userAgent);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap bitmap) {
                super.onPageStarted(view, url, bitmap);
                String cookie = CookieManager.getInstance().getCookie(url);
                if (cookie != null && cookie.contains("cf_clearance")) {
                   // ((Anikumii) getApplicationContext()).setCloudFlare(cookie.substring(cookie.indexOf("cf_clearance=") + 13).split(";")[0]);
                    loadUrl(getIntent().getDataString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.loadUrl("https://m.animeflv.net");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        loadUrl(getIntent().getDataString());
    }

    private void loadUrl(String dataString) {
        if (dataString == null) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (dataString.contains("anime/")) {
            startActivity(new Intent(this, EpisodesActivity.class).putExtra("animeUrl", dataString.split("animeflv.net")[1]));
        }
    }
}
