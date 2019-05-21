package com.zeerooo.anikumii2.adapters;

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
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.activities.EpisodesActivity;
import com.zeerooo.anikumii2.activities.VPlayerActivity;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.misc.ItemsModel;

import java.util.ArrayList;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class AdapterAnimes extends RecyclerView.Adapter<AdapterAnimes.MyViewHolder> {
    private Context context;
    private ArrayList<ItemsModel> animeList = new ArrayList<>();

    public AdapterAnimes(Context context) {
        this.context = context;
    }

    public void addAll(ArrayList<ItemsModel> arrayList) {
        animeList.addAll(arrayList);
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_anime, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        ItemsModel items = animeList.get(position);
        holder.title.setText(items.getTitle());
        holder.number.setText(items.getNumber());

        GlideApp.with(context).load(Anikumii.dominium + items.getImg_url()).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title, number;
        private ImageView img;

        MyViewHolder(View view) {
            super(view);
            number = view.findViewById(R.id.chapter_count);
            title = view.findViewById(R.id.chapter_name);
            img = view.findViewById(R.id.chapter_img);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final ItemsModel items = animeList.get(getAdapterPosition());
            if (items.getChapterUrl().contains("/ver/")) {
                Intent videoAct = new Intent(context, VPlayerActivity.class);
                videoAct.putExtra("chapterUrl", Anikumii.dominium + items.getChapterUrl());
                context.startActivity(videoAct);
            } else {
                Intent episodesAct = new Intent(context, EpisodesActivity.class);
                episodesAct.putExtra("animeUrl", Anikumii.dominium + items.getChapterUrl());
                context.startActivity(episodesAct);
            }
        }
    }
}
