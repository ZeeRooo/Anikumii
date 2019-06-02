package com.zeerooo.anikumii.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.EpisodesActivity;
import com.zeerooo.anikumii.activities.VideoPlayerActivity;
import com.zeerooo.anikumii.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii.misc.ItemsModel;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class AdapterAnimes extends AdapterMain {
    private Context context;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_anime, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ItemsModel itemsModel = animeList.get(i);
        ((AdapterAnimes.MyViewHolder) viewHolder).titleTextView.setText(itemsModel.getTitle());
        ((AdapterAnimes.MyViewHolder) viewHolder).numberTextView.setText(itemsModel.getNumber());

        GlideApp.with(context).load(Anikumii.dominium + itemsModel.getImg_url()).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(((AdapterAnimes.MyViewHolder) viewHolder).animeImageView);
        super.onBindViewHolder(viewHolder, i);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView, numberTextView;
        private ImageView animeImageView;

        MyViewHolder(View view) {
            super(view);
            numberTextView = view.findViewById(R.id.chapter_count);
            titleTextView = view.findViewById(R.id.chapter_name);
            animeImageView = view.findViewById(R.id.chapter_img);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final ItemsModel itemsModel = animeList.get(getAdapterPosition());
            if (itemsModel.getChapterUrl().contains("/ver/")) {
                Intent videoAct = new Intent(context, VideoPlayerActivity.class);
                videoAct.putExtra("chapterUrl", Anikumii.dominium + itemsModel.getChapterUrl());
                context.startActivity(videoAct);
            } else {
                Intent episodesAct = new Intent(context, EpisodesActivity.class);
                episodesAct.putExtra("animeUrl", Anikumii.dominium + itemsModel.getChapterUrl());
                context.startActivity(episodesAct);
            }
        }
    }
}
