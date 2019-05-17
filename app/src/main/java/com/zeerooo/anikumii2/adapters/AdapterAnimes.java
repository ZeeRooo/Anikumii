package com.zeerooo.anikumii2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.activities.EpisodesActivity;
import com.zeerooo.anikumii2.activities.VPlayerActivity;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.misc.ItemsModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class AdapterAnimes extends RecyclerView.Adapter<AdapterAnimes.MyViewHolder> {
    private Context context;
    private ArrayList<ItemsModel> animeList = new ArrayList<>();
    private boolean miniCard;

    public AdapterAnimes(Context context, boolean miniCard) {
        this.context = context;
        this.miniCard = miniCard;
    }

    public void addAll(ArrayList<ItemsModel> arrayList) {
        animeList.addAll(arrayList);
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (miniCard)
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_about_user_anime, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_anime, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        ItemsModel items = animeList.get(position);
        holder.title.setText(items.getTitle());
        if (!miniCard)
            holder.number.setText(items.getNumber());

        GlideApp.with(context).load("https://tioanime.com" + items.getImg_url()).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(holder.img);
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
            if (!miniCard) {
                number = view.findViewById(R.id.chapter_count);
                title = view.findViewById(R.id.chapter_name);
                img = view.findViewById(R.id.chapter_img);
            } else {
                title = view.findViewById(R.id.anime_name);
                img = view.findViewById(R.id.anime_image);
            }
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final ItemsModel items = animeList.get(getAdapterPosition());
            if (items.getChapterUrl().contains("/ver/")) {
                Intent videoAct = new Intent(context, VPlayerActivity.class);
                videoAct.putExtra("chapterUrl", "https://tioanime.com/" + items.getChapterUrl()).putExtra("chapterTitle", items.getTitle()).putExtra("chapterNumber", items.getNumber());
                context.startActivity(videoAct);
            } else {
                Intent episodesAct = new Intent(context, EpisodesActivity.class);
                episodesAct.putExtra("animeUrl", items.getChapterUrl()).putExtra("animeName", items.getTitle());
                context.startActivity(episodesAct);
            }
        }
    }
}
