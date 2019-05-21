package com.zeerooo.anikumii2.anikumiiparts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii2.R;

public class AnikumiiDialog extends AlertDialog {

    private Context context;

    public AnikumiiDialog(@NonNull Context context) {
        super(context);
        this.context = context;
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
