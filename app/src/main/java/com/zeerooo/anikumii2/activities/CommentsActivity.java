package com.zeerooo.anikumii2.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zeerooo.anikumii2.R;

public class CommentsActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.comments));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        webView = findViewById(R.id.commentsWV);
        progressBar = findViewById(R.id.commentsActivityProgressBar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        String rawUrl = getIntent().getStringExtra("rawUrl"), data;

        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT >= 21)
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                view.getSettings().setSupportMultipleWindows(true);
                //view.loadData("javascript:(function() {var parent = document.getElementsByTagName('head').item(0);var style = document.createElement('style');style.type = 'text/css';style.innerHTML = window.atob('Ll84bywgLl84byAuaW1nIHtib3JkZXItcmFkaXVzOiA1MHB4O31ib2R5LnBsdWdpbiB7YmFja2dyb3VuZDogIzJGMzUzQTt9Ll80dXlsIC5fMWNiLCAuXzV0cjYsIC5fNXNnZSwgLl81eWN0IHtiYWNrZ3JvdW5kOiAjMjMyODJCO30uXzUwZjcge2NvbG9yOiB3aGl0ZTt9Ll81bWRkLCAuXzFtaiB7Y29sb3I6ICNmY2ZjZmM7fS5fcHVwIHtjb2xvcjogIzkwOTQ5Yzt9');parent.appendChild(style)})()",  "text/html", "UTF-8");
                //String css = "._8o, ._8o .img {border-radius: 50px;}" + "body.plugin {background: #2F353A; overflow: scroll;}" + "._4uyl ._1cb, ._5tr6, ._5sge, ._5yct {background: #23282B;}" + "._50f7 {color: white;}" + "._5mdd, ._1mj {color: #fcfcfc;}" + "._pup {color: #90949c;}";
            }

            @Override
            @SuppressLint("NewApi")
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                final WebView loginWebView = new WebView(CommentsActivity.this);
                final Dialog dialog = new Dialog(CommentsActivity.this);

                loginWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);

                        if (url.contains("close_popup.php")) {
                            webView.reload();
                            dialog.dismiss();
                            loginWebView.clearHistory();
                            loginWebView.removeAllViews();
                            loginWebView.destroy();
                        }
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(loginWebView);
                resultMsg.sendToTarget();

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(transport.getWebView());
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
            }
        });

        data = "<div id=\"fb-root\"></div><script>(function(d, s, id) {var js, fjs = d.getElementsByTagName(s)[0];if (d.getElementById(id)) return;js = d.createElement(s); js.id = id;js.src = 'https://connect.facebook.net/es_LA/sdk.js#xfbml=1&version=v3.1';fjs.parentNode.insertBefore(js, fjs);}(document, 'script', 'facebook-jssdk'));</script><div class=\"fb-comments\" data-href='" + rawUrl + "' data-numposts=\"5\"></div>";

        webView.loadDataWithBaseURL("https://facebook.com", data, "text/html", "UTF-8", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.clearHistory();
        webView.removeAllViews();
        webView.destroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
