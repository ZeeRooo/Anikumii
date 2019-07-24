package com.zeerooo.anikumii.anikumiiparts;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.services.AccountsService;

public class AnikumiiBottomSheetDialog extends BottomSheetDialog {

    private AnikumiiSharedPreferences anikumiiSharedPreferences;
    private ViewGroup viewGroup;

    public AnikumiiBottomSheetDialog(@NonNull Context context) {
        super(context, R.style.BottomSheetDialogTheme);
    }

    @Override
    public void setContentView(View view) {
        viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        viewGroup.addView(view);
        super.setContentView(viewGroup);
    }

    public ViewGroup getViewGroup() {
        return viewGroup;
    }

    public void initialize(String title, View content, int navigationBarColor) {
        setContentView(content);

        ((TextView) findViewById(R.id.dialogTitleTextView)).setText(title);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setNavigationBarColor(content.getResources().getColor(navigationBarColor));

        show();
    }

    public void loginDialog() {
        anikumiiSharedPreferences = new AnikumiiSharedPreferences(getContext());

        final Dialog dialog = new Dialog(getContext());

        LinearLayout linearLayout = new LinearLayout(getContext());

        WebView webView = new WebView(getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(), "HTMLOUT");
        webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0");

        View loginRootView = getLayoutInflater().inflate(R.layout.bottom_sheet_log_in, null);

        AppCompatButton malButton = loginRootView.findViewById(R.id.bottom_sheet_log_in_mal_btn);
        malButton.setSupportBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.myanimelist));
        setLoginButton(malButton, "https://myanimelist.net/login.php?from=%2F", webView, dialog);

        FloatingActionButton facebookFab = loginRootView.findViewById(R.id.bottom_sheet_log_in_facebook_btn);
        setLoginButton(facebookFab, "https://myanimelist.net/sns/login/facebook?from=%2F", webView, dialog);

        FloatingActionButton twitterFab = loginRootView.findViewById(R.id.bottom_sheet_log_in_twitter_btn);
        setLoginButton(twitterFab, "https://myanimelist.net/sns/login/twitter?from=%2F", webView, dialog);

        FloatingActionButton googleFab = loginRootView.findViewById(R.id.bottom_sheet_log_in_google_btn);
        setLoginButton(googleFab, "https://myanimelist.net/sns/login/google?from=%2F", webView, dialog);

        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        linearLayout.addView(webView);
        dialog.setContentView(linearLayout);

        initialize(getContext().getString(R.string.log_in) + " en MyAnimeList", loginRootView, R.color.colorPrimary);
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

    public ChipGroup serverDialog(String selectedServer) {
        if (selectedServer == null)
            selectedServer = "Zippyshare";

        ChipGroup chipGroup = new ChipGroup(getContext());
        chipGroup.setPadding(30, 30, 30, 30);
        chipGroup.setChipSpacing(10);
        chipGroup.setSingleSelection(true);

        Chip streamangoChip = new Chip(getContext());
        streamangoChip.setText(getContext().getString(R.string.streamango));
        streamangoChip.setCheckable(true);
        streamangoChip.setTextColor(Color.WHITE);
        streamangoChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#664bf1")));
        streamangoChip.setChecked(selectedServer.equals("Streamango"));
        chipGroup.addView(streamangoChip);

        Chip zippyshareChip = new Chip(getContext());
        zippyshareChip.setText(getContext().getString(R.string.zippyshare));
        zippyshareChip.setCheckable(true);
        zippyshareChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#fffdd1")));
        zippyshareChip.setTextColor(Color.BLACK);
        zippyshareChip.setChecked(selectedServer.equals("Zippyshare"));
        chipGroup.addView(zippyshareChip);

        Chip mediafireChip = new Chip(getContext());
        mediafireChip.setText(getContext().getString(R.string.mediafire));
        mediafireChip.setCheckable(true);
        mediafireChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#0077ff")));
        mediafireChip.setTextColor(Color.WHITE);
        mediafireChip.setChecked(selectedServer.equals("MediaFire"));
        chipGroup.addView(mediafireChip);

        Chip okruChip = new Chip(getContext());
        okruChip.setText(getContext().getString(R.string.okru));
        okruChip.setCheckable(true);
        okruChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#ee8208")));
        okruChip.setTextColor(Color.BLACK);
        okruChip.setChecked(selectedServer.equals("ok.ru"));
        chipGroup.addView(okruChip);

        initialize(getContext().getString(R.string.changeServer), chipGroup, R.color.colorPrimary);

        return chipGroup;
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

                getContext().startService(new Intent(getContext(), AccountsService.class).putExtra("startMain", true));
            } else {
                anikumiiSharedPreferences.edit().putString("malUserName", token).apply();
            }
        }
    }
}
