package com.zeerooo.anikumii.anikumiiparts;

import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii.R;

public class AnikumiiUiHelper {
    public static Snackbar snackbar;

    public static void transparentBackground(View view) {
        final TypedValue typedValue = new TypedValue();
        int bg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            bg = android.R.attr.selectableItemBackgroundBorderless;
        else
            bg = android.R.attr.selectableItemBackground;

        view.getContext().getTheme().resolveAttribute(bg, typedValue, true);
        view.setBackgroundResource(typedValue.resourceId);
    }

    public static Snackbar errorSnackbar(View view, int length, String cause, View.OnClickListener onClickListener) {
        snackbar = Snackbar.make(view, null, length);

        if (cause.contains("UnknownHostException")) {
            snackbar.setText(view.getContext().getString(R.string.rxerror_no_connection));
            if (onClickListener != null)
                snackbar.setAction("Reintentar", onClickListener);
        } else if (cause.contains("SQL"))
            snackbar.setText(view.getContext().getString(R.string.sqlite_exception));
        else if (cause.contains("permission_denied"))
            snackbar.setText(view.getContext().getString(R.string.permission_denied));
        else if (cause.equals("videoPlayer")) {
            snackbar.setText(view.getContext().getString(R.string.server_not_found));
            if (onClickListener != null)
                snackbar.setAction(view.getContext().getString(R.string.changeServer), onClickListener);
        } else
            snackbar.setText(view.getContext().getString(R.string.rxerror));

        snackbar.getView().setBackgroundColor(Color.parseColor("#B00020"));
        ((TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text)).setTextColor(Color.WHITE);

        return snackbar;
    }
}
