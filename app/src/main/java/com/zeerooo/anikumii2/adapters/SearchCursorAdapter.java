package com.zeerooo.anikumii2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

/**
 * Created by ZeeRooo on 10/02/18
 */

public class SearchCursorAdapter extends SimpleCursorAdapter {

    public SearchCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView title = view.findViewById(R.id.suggerencesTitle);
        title.setText(cursor.getString(1));

        TextView type = view.findViewById(R.id.suggerenceType);
        type.setText(cursor.getString(2));

        ImageView icon = view.findViewById(R.id.suggerenceIcon);
        GlideApp.with(context).load("https://tioanime.com/uploads/portadas/" + cursor.getString(3) + ".jpg").apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()).into(icon);
    }
}
