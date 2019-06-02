package com.zeerooo.anikumii.anikumiiparts;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.services.AccountsService;

public class AnikumiiDialog extends AlertDialog {

    private Context context;
    private AnikumiiSharedPreferences anikumiiSharedPreferences;

    public AnikumiiDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public void initialize(String title, View content) {
        if (title != null) {
            View header = getLayoutInflater().inflate(R.layout.dialog_header, null);
            TextView titleTextView = header.findViewById(R.id.dialogTitleTextView);
            titleTextView.setText(title);

            ImageButton closeImageButton = header.findViewById(R.id.dialog_close_button);
            AnikumiiUiHelper.transparentBackground(closeImageButton);
            closeImageButton.setOnClickListener(v -> dismiss());

            setCustomTitle(header);
        } else
            requestWindowFeature(Window.FEATURE_NO_TITLE);

        setView(content);

        show();

        getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.celestito));
        getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.celestito));
        getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getContext().getResources().getColor(R.color.celestito));
    }

    public void loginDialog() {
        anikumiiSharedPreferences = new AnikumiiSharedPreferences(context);
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_webview);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        WebView webView = dialog.findViewById(R.id.dialog_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(), "HTMLOUT");
        webView.getSettings().setUserAgentString(Anikumii.userAgent);

        View loginRootView = getLayoutInflater().inflate(R.layout.dialog_log_in, null);

        AppCompatButton malButton = loginRootView.findViewById(R.id.dialog_log_in_mal_btn);
        malButton.setSupportBackgroundTintList(ContextCompat.getColorStateList(context, R.color.myanimelist));
        setLoginButton(malButton, "https://myanimelist.net/login.php?from=%2F", webView, dialog);

        FloatingActionButton facebookFab = loginRootView.findViewById(R.id.dialog_log_in_facebook_btn);
        setLoginButton(facebookFab, "https://myanimelist.net/sns/login/facebook?from=%2F", webView, dialog);

        FloatingActionButton twitterFab = loginRootView.findViewById(R.id.dialog_log_in_twitter_btn);
        setLoginButton(twitterFab, "https://myanimelist.net/sns/login/twitter?from=%2F", webView, dialog);

        FloatingActionButton googleFab = loginRootView.findViewById(R.id.dialog_log_in_google_btn);
        setLoginButton(googleFab, "https://myanimelist.net/sns/login/google?from=%2F", webView, dialog);

        initialize(context.getString(R.string.log_in) + " en MyAnimeList", loginRootView);
    }

    private void setLoginButton(View buttonView, String url, WebView webView, Dialog dialog) {
        buttonView.setOnClickListener(view -> {
            webView.loadUrl(url);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (url.equals("https://myanimelist.net/")) {
                        view.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByClassName('header-profile-link')[0].text, false);");
                        view.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByName('csrf_token')[0].getAttribute('content'), true);");
                        dialog.dismiss();
                        dismiss();
                    }
                }
            });

            dialog.show();
        });
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public void processHTML(String token, boolean cookie) {
            if (cookie) {
                StringBuilder cookiesBuilder = new StringBuilder();
                String[] strings = CookieManager.getInstance().getCookie("https://myanimelist.net/").split(";");
                for (int i = 0; i < strings.length; i++) {
                    if (strings[i].contains("MALHLOGSESSID") || strings[i].contains("MALSESSIONID"))
                        cookiesBuilder.append(strings[i]).append(";");
                }
                cookiesBuilder.append(" csrf_token=").append(token);
                anikumiiSharedPreferences.encrypt("malCookie", cookiesBuilder.toString());

                context.startService(new Intent(context, AccountsService.class).putExtra("startMain", true));
            } else {
                anikumiiSharedPreferences.edit().putString("malUserName", token).apply();
            }
        }
    }

    public ChipGroup serverDialog(String selectedServer) {
        ChipGroup chipGroup = new ChipGroup(context);
        chipGroup.setPadding(30, 30, 30, 30);
        chipGroup.setChipSpacing(10);
        chipGroup.setSingleSelection(true);

        Chip streamangoChip = new Chip(context);
        streamangoChip.setText(context.getString(R.string.streamango));
        streamangoChip.setCheckable(true);
        streamangoChip.setTextColor(Color.WHITE);
        streamangoChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#664bf1")));
        streamangoChip.setChecked(selectedServer.equals("Streamango"));
        chipGroup.addView(streamangoChip);

        Chip zippyshareChip = new Chip(context);
        zippyshareChip.setText(context.getString(R.string.zippyshare));
        zippyshareChip.setCheckable(true);
        zippyshareChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#fffdd1")));
        zippyshareChip.setTextColor(Color.BLACK);
        zippyshareChip.setChecked(selectedServer.equals("Zippyshare"));
        chipGroup.addView(zippyshareChip);

        Chip mediafireChip = new Chip(context);
        mediafireChip.setText(context.getString(R.string.mediafire));
        mediafireChip.setCheckable(true);
        mediafireChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#0077ff")));
        mediafireChip.setTextColor(Color.WHITE);
        mediafireChip.setChecked(selectedServer.equals("MediaFire"));
        chipGroup.addView(mediafireChip);

        Chip okruChip = new Chip(context);
        okruChip.setText(context.getString(R.string.okru));
        okruChip.setCheckable(true);
        okruChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#ee8208")));
        okruChip.setTextColor(Color.BLACK);
        okruChip.setChecked(selectedServer.equals("ok.ru"));
        chipGroup.addView(okruChip);

        Chip rapidvideoChip = new Chip(context);
        rapidvideoChip.setText(context.getString(R.string.rapidvideo));
        rapidvideoChip.setCheckable(true);
        rapidvideoChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4574AE")));
        rapidvideoChip.setTextColor(Color.WHITE);
        rapidvideoChip.setChecked(selectedServer.equals("Rapidvideo"));
        chipGroup.addView(rapidvideoChip);

        initialize(context.getString(R.string.changeServer), chipGroup);

        return chipGroup;
    }
}
