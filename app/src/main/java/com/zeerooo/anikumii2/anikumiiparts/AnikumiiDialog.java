package com.zeerooo.anikumii2.anikumiiparts;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.zeerooo.anikumii2.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class AnikumiiDialog extends AlertDialog {

    public AnikumiiDialog(@NonNull Context context) {
        super(context);
    }

    public void initialize(String title, View content) {
        if (title != null) {
            TextView customTitleView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_title, null).findViewById(R.id.dialigTitleTextView);
            customTitleView.setText(title);
            setCustomTitle(customTitleView);
        } else
            requestWindowFeature(Window.FEATURE_NO_TITLE);

        setView(content);

        show();

        getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.celestito));
        getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.celestito));
        getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getContext().getResources().getColor(R.color.celestito));
    }

    public void addCancelButton() {
        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", (DialogInterface dialogInterface, int i) -> dismiss());
    }
}
