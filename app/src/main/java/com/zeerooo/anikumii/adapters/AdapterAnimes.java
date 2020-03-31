package com.zeerooo.anikumii.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.EpisodesActivity;
import com.zeerooo.anikumii.activities.VideoPlayerActivity;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiBottomSheetDialog;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.misc.ItemsModel;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class AdapterAnimes extends AdapterMain {
    private final boolean isHistory;
    private final RequestOptions requestOptions;
    private Context context;
    private ItemsModel itemsModel;

    public AdapterAnimes(boolean isHistory) {
        this.isHistory = isHistory;

        requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        if (isHistory)
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_history, viewGroup, false));
        else
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_card_anime, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        itemsModel = animeList.get(i);
        ((AdapterAnimes.MyViewHolder) viewHolder).titleTextView.setText(itemsModel.getTitle());
        ((AdapterAnimes.MyViewHolder) viewHolder).numberTextView.setText(itemsModel.getNumber());

        Glide.with(context).load(itemsModel.getImgUrl()).apply(requestOptions).into(((AdapterAnimes.MyViewHolder) viewHolder).animeImageView);
        super.onBindViewHolder(viewHolder, i);
    }

    private void bottomSheetAction(String title, String animeName, View.OnClickListener onClickListener) {
        final AnikumiiBottomSheetDialog anikumiiBottomSheetDialog = new AnikumiiBottomSheetDialog(context);

        final MaterialButton materialButton = new MaterialButton(context);
        materialButton.setText(title);
        materialButton.setStrokeWidth(1);
        materialButton.setStrokeColor(ColorStateList.valueOf(context.getResources().getColor(R.color.celestito)));
        materialButton.setTextColor(-4276546);
        materialButton.setOnClickListener(onClickListener);

        anikumiiBottomSheetDialog.initialize(animeName, materialButton, R.color.colorPrimary);
        anikumiiBottomSheetDialog.getViewGroup().setPadding(10, 10, 10, 10);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView titleTextView;
        private final TextView numberTextView;
        private final ImageView animeImageView;

        MyViewHolder(View view) {
            super(view);
            numberTextView = view.findViewById(R.id.item_chapter_count);
            titleTextView = view.findViewById(R.id.item_chapter_name);
            animeImageView = view.findViewById(R.id.item_chapter_img);
            view.setOnClickListener(this);

            view.setOnLongClickListener(v -> {
                final ItemsModel itemsModel = animeList.get(getAdapterPosition());
                if (itemsModel.getChapterUrl().contains("/ver/")) {
                    bottomSheetAction("Ir a lista de episodios", itemsModel.getTitle(), view1 -> {
                        String type;
                        if (Anikumii.dominium.startsWith("https://tioanime.com"))
                            type = "/anime/";
                        else
                            type = "/hentai/";
                        context.startActivity(new Intent(context, EpisodesActivity.class).putExtra("animeUrl", itemsModel.getChapterUrl().replace("/ver/", type).split("(-\\d+)\\D*$")[0]));

                        //context.startActivity(new Intent(context, EpisodesActivity.class).putExtra("animeUrl", itemsModel.getChapterUrl().replace("/ver/", type).replaceAll("(-\\d+)\\D*$", "")));
                    });
                } else if (isHistory) {
                    bottomSheetAction("Ir al último episodio", itemsModel.getTitle(), view1 -> context.startActivity(new Intent(context, VideoPlayerActivity.class).putExtra("chapterUrl", itemsModel.getChapterUrl().replace("/anime/", "/ver/").replace("/hentai/", "/ver/") + "-" + itemsModel.getNumber().split("- Episodio ")[1])));
                }
                return false;
            });

            if (isHistory) {
                final ImageButton dateImageButton = view.findViewById(R.id.item_history_about);
                AnikumiiUiHelper.transparentBackground(dateImageButton);
                TooltipCompat.setTooltipText(dateImageButton, "Información");
                dateImageButton.setOnClickListener(view2 -> {
                    final AnikumiiBottomSheetDialog anikumiiBottomSheetDialog = new AnikumiiBottomSheetDialog(context);

                    final TextView dateTextView = new TextView(context);
                    dateTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    dateTextView.setTextAppearance(context, R.style.TextAppearance_Material_Body);
                    dateTextView.setText((animeList.get(getAdapterPosition()).getDate()));

                    anikumiiBottomSheetDialog.initialize("Fecha", dateTextView, R.color.colorPrimary);
                    anikumiiBottomSheetDialog.getViewGroup().setPadding(15, 15, 15, 15);
                });
            }
        }

        @Override
        public void onClick(View view) {
            final ItemsModel itemsModel = animeList.get(getAdapterPosition());
            if (itemsModel.getChapterUrl().contains("/ver/")) {
                final Intent videoAct = new Intent(context, VideoPlayerActivity.class);
                videoAct.putExtra("chapterUrl", itemsModel.getChapterUrl());
                context.startActivity(videoAct);
            } else {
                final Intent episodesAct = new Intent(context, EpisodesActivity.class);
                episodesAct.putExtra("animeUrl", itemsModel.getChapterUrl());
                context.startActivity(episodesAct);
            }
        }
    }
}
