package com.zeerooo.anikumii2.anikumiiparts;

import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class AnikumiiUiHelper {

    public static void transparentBackground(View view) {
        TypedValue typedValue = new TypedValue();
        int bg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            bg = android.R.attr.selectableItemBackgroundBorderless;
        else
            bg = android.R.attr.selectableItemBackground;

        view.getContext().getTheme().resolveAttribute(bg, typedValue, true);
        view.setBackgroundResource(typedValue.resourceId);
    }

    public static Snackbar Snackbar(View view, String content, int lenght) {
        Snackbar snackbar = Snackbar.make(view, content, lenght);

        snackbar.getView().setBackgroundColor(Color.parseColor("#B00020"));
        ((TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text)).setTextColor(Color.WHITE);

        return snackbar;
    }

}
