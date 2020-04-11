package com.zeerooo.anikumii.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.misc.Utils;

public class UpdateActivity extends AppCompatActivity {
    private String url, tagName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        final WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        getWindow().setAttributes(layoutParams);

        setTitle("Nueva versión disponible");

        tagName = getIntent().getStringExtra("tagName");

        final ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(66, 104, 179));
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);

        final TextView newVersionTextView = findViewById(R.id.update_new_version);
        newVersionTextView.setText(Utils.getBold("Código de versión: " + tagName, foregroundColorSpan, styleSpan));

        final TextView dateTextView = findViewById(R.id.update_date);
        dateTextView.setText(Utils.getBold("Fecha: " + getIntent().getStringExtra("publishDate"), foregroundColorSpan, styleSpan));

        final TextView changelogTextView = findViewById(R.id.update_changelog);
        changelogTextView.setText(getIntent().getStringExtra("changelog"));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        findViewById(R.id.update_all).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/ZeeRooo/Anikumii/releases"))));

        findViewById(R.id.update_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.update_ok).setOnClickListener(v -> {
            url = getIntent().getStringExtra("downloadUrl");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            final Snackbar snackbar = Snackbar.make(findViewById(R.id.update_root_view), getString(R.string.snackback_download_confirmation, "versiones"), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getString(android.R.string.ok), view -> {
                snackbar.dismiss();
                finish();
            });
            snackbar.show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            final DownloadManager mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir("/Anikumii!!/versiones/", tagName + ".apk");
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            mDownloadManager.enqueue(request);
        } else
            AnikumiiUiHelper.errorSnackbar(findViewById(R.id.update_root_view), Snackbar.LENGTH_LONG, "permission_denied", null).show();
    }
}
