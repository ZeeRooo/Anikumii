package com.zeerooo.anikumii.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.EpisodesActivity;
import com.zeerooo.anikumii.activities.VideoPlayerActivity;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.anikumiiparts.AnimeRatingView;
import com.zeerooo.anikumii.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii.fragments.AnimesRecyclerViewFragment;
import com.zeerooo.anikumii.misc.ItemsModel;

import java.util.ArrayList;

/**
 * Created by ZeeRooo on 07/01/18
 */

public class AdapterEpisodes extends AdapterMain {
    private byte parentID = 0;
    private boolean isFav, loadHeader = true;
    private Context context;
    private String ratingStr, nextEpisodeDate, aboutStr, typeStr;
    private View rootViewHeader;
    private TextView numberTextView, statusTextView, aboutTextView, readMore, typeTextView;
    private ImageView animeImageView;
    private AnimeRatingView ratingView;
    private ImageButton favBtn;
    private SearchView episodesSearchView;
    private ArrayList<ItemsModel> prevAnimeList;
    private ArrayList<String> genreList, listRel;
    private EpisodesFilter episodesFilter;

    public AdapterEpisodes(String ratingStr, String nextEpisodeDate, String aboutStr, String type, ArrayList<String> genreList, ArrayList<String> listRel, boolean isFav) {
        this.ratingStr = ratingStr;
        this.nextEpisodeDate = nextEpisodeDate;
        this.genreList = genreList;
        this.listRel = listRel;
        this.isFav = isFav;
        this.aboutStr = aboutStr;
        this.typeStr = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView;
        context = viewGroup.getContext();
        if (i != 0) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_episode, viewGroup, false);
            return new MyViewHolder(itemView);
        } else {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_episodes, viewGroup, false);
            return new Header(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ItemsModel items = animeList.get(position);
            if (items != null) {
                numberTextView.setText(items.getNumber());
                numberTextView.setTextColor(items.getTextColor());

                GlideApp.with(context).load(items.getImg_url()).into(animeImageView);
            }
        } else if (holder instanceof Header && loadHeader) {
            ratingView.init(ratingStr);
            ratingView.setAnimatesProgress();
            TooltipCompat.setTooltipText(ratingView, ratingStr + " " + context.getString(R.string.rating));
            ratingView.setContentDescription(ratingStr + " " + context.getString(R.string.rating));

            typeTextView.setText(typeStr);
            typeTextView.setContentDescription(typeStr);

            for (byte genreCount = 0; genreCount < genreList.size(); genreCount++) {
                final String genre = genreList.get(genreCount);
                Chip genreChip = new Chip(context);
                genreChip.setText(genre);
                genreChip.setTextSize(14);
                genreChip.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("toLoad", Anikumii.dominium + "/directorio?genero=" + genre.replace(" ", "-"));
                    bundle.putString("genre", genre);
                    bundle.putString("element", "article.anime");
                    FragmentManager fragmentManager = ((EpisodesActivity) context).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    AnimesRecyclerViewFragment fragment = new AnimesRecyclerViewFragment();
                    fragment.setArguments(bundle);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.act_episodes_rootView, fragment);
                    fragmentTransaction.commit();
                });

                ((ChipGroup) rootViewHeader.findViewById(R.id.chipGroups)).addView(genreChip);
            }

            if (isFav) {
                favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite));
                TooltipCompat.setTooltipText(favBtn, context.getString(R.string.bottom_sheet_removeFav));
                favBtn.setContentDescription(context.getString(R.string.bottom_sheet_removeFav));
            } else {
                favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border));
                TooltipCompat.setTooltipText(favBtn, context.getString(R.string.bottom_sheet_newFav));
                favBtn.setContentDescription(context.getString(R.string.bottom_sheet_newFav));
            }

            AnikumiiUiHelper.transparentBackground(favBtn);
            favBtn.setOnClickListener(view -> {
               /* if (isFav) {
                    //buttonHelper("favorite", "1");
                    favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border));
                    TooltipCompat.setTooltipText(favBtn, context.getString(R.string.bottom_sheet_removeFav));
                    favBtn.setContentDescription(context.getString(R.string.bottom_sheet_removeFav));
                } else {
                    // buttonHelper("favorite", "0");
                    favBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite));
                    TooltipCompat.setTooltipText(favBtn, context.getString(R.string.bottom_sheet_removeFav));
                    favBtn.setContentDescription(context.getString(R.string.bottom_sheet_removeFav));
                }*/
                isFav = !isFav;
            });

            episodesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    episodesFilter.filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    episodesFilter.filter(newText);
                    return true;
                }
            });
            episodesSearchView.setOnCloseListener(() -> false);

            aboutTextView.setText(aboutStr);
            AnikumiiUiHelper.transparentBackground(readMore);
            aboutTextView.post(() -> {
                if (aboutTextView.getLineCount() > 3) {
                    byte line = (byte) aboutTextView.getLineCount();
                    aboutTextView.setMaxLines(3);
                    readMore.setVisibility(View.VISIBLE);
                    readMore.setPadding(0, 0, 0, 10);
                    readMore.setOnClickListener(new View.OnClickListener() {
                        boolean isFull = true;

                        @Override
                        public void onClick(View view) {
                            if (isFull) {
                                ObjectAnimator
                                        .ofInt(aboutTextView, "maxLines", 3, line)
                                        .setDuration(100)
                                        .start();
                                aboutTextView.setEllipsize(null);
                                readMore.setText(context.getString(R.string.readLess));
                            } else {
                                ObjectAnimator
                                        .ofInt(aboutTextView, "maxLines", line, 3)
                                        .setDuration(100)
                                        .start();
                                aboutTextView.setEllipsize(TextUtils.TruncateAt.END);
                                readMore.setText(context.getString(R.string.readMore));
                            }
                            isFull = !isFull;
                        }
                    });

                } else
                    readMore.setVisibility(View.GONE);
            });

            statusTextView.setText(nextEpisodeDate);

            if (nextEpisodeDate.equals("Finalizado"))
                statusTextView.setTextColor(Color.parseColor("#fd3246"));
            else
                statusTextView.setTextColor(Color.parseColor("#03804e"));

            for (byte listRelCount = 0; listRelCount < listRel.size(); listRelCount++) {
                final String s = listRel.get(listRelCount);
                parentID++;
                Chip chipRel = new Chip(context);
                chipRel.setId(parentID);
                chipRel.setEllipsize(TextUtils.TruncateAt.END);
                chipRel.setText(s.split("-_")[0]);
                chipRel.setTextColor(Color.parseColor("#00bcf2"));
                chipRel.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#5901bcf2")));
                chipRel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                chipRel.setOnClickListener(view -> {
                    Intent episodesAct = new Intent(context, EpisodesActivity.class);
                    episodesAct.putExtra("animeUrl", Anikumii.dominium + s.split("-_")[1]);
                    context.startActivity(episodesAct);
                });
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                if (parentID == 1) {
                    if (favBtn.getVisibility() == View.VISIBLE)
                        param.addRule(RelativeLayout.BELOW, R.id.add_anime_fav);
                    else if (readMore.getVisibility() == View.VISIBLE)
                        param.addRule(RelativeLayout.BELOW, R.id.readMore);
                    else
                        param.addRule(RelativeLayout.BELOW, R.id.anime_about);
                } else
                    param.addRule(RelativeLayout.BELOW, parentID - 1);
                if (parentID == listRel.size())
                    param.setMargins(10, 5, 10, 16);
                else
                    param.setMargins(10, 5, 10, 5);
                chipRel.setLayoutParams(param);
                ((RelativeLayout) rootViewHeader).addView(chipRel);
            }

            loadHeader = false;
        }
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        MyViewHolder(View view) {
            super(view);
            numberTextView = view.findViewById(R.id.episodes_number);
            animeImageView = view.findViewById(R.id.episodes_img);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ItemsModel items = animeList.get(getAdapterPosition());
            Intent videoAct = new Intent(context, VideoPlayerActivity.class);
            videoAct.putExtra("chapterUrl", Anikumii.dominium + "/ver/" + items.getChapterUrl());
            context.startActivity(videoAct);
        }
    }

    class Header extends RecyclerView.ViewHolder {
        Header(View view) {
            super(view);
            ratingView = view.findViewById(R.id.AnimeRatingView);
            rootViewHeader = view;
            statusTextView = view.findViewById(R.id.nextEpisode_date);
            favBtn = view.findViewById(R.id.add_anime_fav);
            episodesFilter = new EpisodesFilter();
            aboutTextView = view.findViewById(R.id.anime_about);
            readMore = view.findViewById(R.id.readMore);
            typeTextView = view.findViewById(R.id.animeType);
            episodesSearchView = view.findViewById(R.id.searchEpisodes);
        }
    }

    class EpisodesFilter extends Filter {

        EpisodesFilter() {
            prevAnimeList = animeList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            animeList = new ArrayList<>();

            if (constraint.length() != 0) {
                animeList.add(0, null);

                for (int position = 1; position < prevAnimeList.size(); position++) {
                    if (prevAnimeList.get(position).getNumber().contains(constraint)) {
                        animeList.add(prevAnimeList.get(position));
                    }
                }
            } else {
                animeList = prevAnimeList;
            }

            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyItemRangeRemoved(1, prevAnimeList.size());
        }
    }
}
