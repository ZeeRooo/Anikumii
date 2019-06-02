package com.zeerooo.anikumii.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.glide.GlideApp;

/**
 * Created by ZeeRooo on 10/02/18
 */

public class SearchCursorAdapter extends SimpleCursorAdapter {

    public SearchCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView = view.findViewById(R.id.suggerencesTitle);
        titleTextView.setText(cursor.getString(1));

        TextView typeTextView = view.findViewById(R.id.suggerenceType);
        typeTextView.setText(cursor.getString(2));

        GlideApp.with(context).load(Anikumii.dominium + "/uploads/portadas/" + cursor.getString(3) + ".jpg").apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into((ImageView) view.findViewById(R.id.suggerenceIcon));
    }
}
