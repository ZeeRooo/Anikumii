package com.zeerooo.anikumii.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.AnimeActivity;
import com.zeerooo.anikumii.activities.EpisodesActivity;
import com.zeerooo.anikumii.activities.VideoPlayerActivity;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiBottomSheetDialog;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.anikumiiparts.AnimeRatingView;
import com.zeerooo.anikumii.misc.ItemsModel;
import com.zeerooo.anikumii.misc.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by ZeeRooo on 07/01/18
 */

public class AdapterEpisodes extends AdapterMain {
    private final String ratingStr;
    private final String nextEpisodeDate;
    private final String aboutStr;
    private final String typeStr;
    private final String malUrl;
    private final String tioId;
    private final ArrayList<String> genreList;
    private final ArrayList<String> listRel;
    private byte parentID = 0;
    private boolean loadHeader = true;
    private Context context;
    private View rootViewHeader;
    private TextView numberTextView, statusTextView, aboutTextView, readMore, typeTextView;
    // private ImageView animeImageView;
    private AnimeRatingView ratingView;
    private SearchView episodesSearchView;
    private ArrayList<ItemsModel> prevAnimeList;
    private EpisodesFilter episodesFilter;
    private ItemsModel items;

    public AdapterEpisodes(String tioId, String malUrl, String ratingStr, String nextEpisodeDate, String aboutStr, String type, ArrayList<String> genreList, ArrayList<String> listRel) {
        this.ratingStr = ratingStr;
        this.nextEpisodeDate = nextEpisodeDate;
        this.genreList = genreList;
        this.listRel = listRel;
        this.aboutStr = aboutStr;
        this.typeStr = type;
        this.malUrl = malUrl;
        this.tioId = tioId;
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
            items = animeList.get(position);
            numberTextView.setText(items.getNumber());
            numberTextView.setTextColor(items.getTextColor());

            //Glide.with(context).load(items.getImgUrl()).into(animeImageView);
        } else if (holder instanceof Header && loadHeader) {
            ratingView.init(ratingStr);
            ratingView.setAnimatesProgress();
            TooltipCompat.setTooltipText(ratingView, ratingStr + " " + context.getString(R.string.rating));
            ratingView.setContentDescription(ratingStr + " " + context.getString(R.string.rating));

            typeTextView.setText(typeStr);
            typeTextView.setContentDescription(typeStr);

            for (byte genreCount = 0; genreCount < genreList.size(); genreCount++) {
                final String genre = genreList.get(genreCount);
                final Chip genreChip = new Chip(context);
                genreChip.setText(genre);
                genreChip.setTextSize(14);
                genreChip.setOnClickListener(view -> context.startActivity(new Intent(context, AnimeActivity.class)
                        .putExtra("toLoad", Anikumii.dominium + "/directorio?genero=" + genre.replace(" ", "-"))
                        .putExtra("title", genre)
                        .putExtra("element", "article.anime")));

                ((ChipGroup) rootViewHeader.findViewById(R.id.chipGroups)).addView(genreChip);
            }

            Utils.setColorFilter(episodesSearchView.findViewById(R.id.search_button), -4276546);//secondary_text_light_nodisable
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

            aboutTextView.setText(Html.fromHtml(aboutStr));
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
                final Chip chipRel = new Chip(context);
                chipRel.setId(parentID);
                chipRel.setEllipsize(TextUtils.TruncateAt.END);
                chipRel.setText(s.split("-_")[0]);
                chipRel.setTextColor(Color.parseColor("#00bcf2"));
                chipRel.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#5901bcf2")));
                chipRel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                chipRel.setOnClickListener(view -> {
                    final Intent episodesAct = new Intent(context, EpisodesActivity.class);
                    episodesAct.putExtra("animeUrl", Anikumii.dominium + s.split("-_")[1]);
                    context.startActivity(episodesAct);
                });
                final RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                if (parentID == 1) {
                    if (episodesSearchView.getVisibility() == View.VISIBLE)
                        param.addRule(RelativeLayout.BELOW, R.id.searchEpisodes);
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
        private Element element;
        private String synopsis, title, imageUrl, aired, duration;

        MyViewHolder(View view) {
            super(view);

            numberTextView = view.findViewById(R.id.item_episodes_number);

            final ImageButton aboutEpisode = view.findViewById(R.id.item_episode_about);
            AnikumiiUiHelper.transparentBackground(aboutEpisode);
            TooltipCompat.setTooltipText(aboutEpisode, "Información");
            aboutEpisode.setOnClickListener(v ->
                    Observable
                            .just(true)
                            .observeOn(Schedulers.io())
                            .doOnNext(aBoolean -> {
                                element = Jsoup.connect(malUrl + animeList.get(getAdapterPosition()).getNumber().replace("Episodio ", "/episode/")).get().selectFirst("div.js-scrollfix-bottom-rel");

                                try {
                                    imageUrl = element.selectFirst("div.contents-video-embed > div > a > img").attr("src");
                                } catch (NullPointerException npe) {
                                    imageUrl = Anikumii.dominium + "/uploads/portadas/" + tioId + ".jpg";
                                }

                                synopsis = element.selectFirst("div.pt8.pb8").text();
                                title = element.selectFirst("h2.fs18.lh11").text();
                                aired = Utils.matcher(element.selectFirst("div.di-tc.pt4.pb4.pl8.pr8.ar.fn-grey2").text(), "Aired: (.*)");
                                duration = Utils.matcher(element.selectFirst("div.di-tc.pt4.pb4.pl8.pr8.ar.fn-grey2").text(), "Duration: (.*) Aired");
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<Boolean>() {
                                @Override
                                public void onNext(Boolean aBoolean) {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (e instanceof NullPointerException) {
                                        synopsis = "Sinopsis: No hay sinopsis disponible para este capítulo.";
                                        title = animeList.get(getAdapterPosition()).getTitle();
                                        aired = "Desconocido.";
                                        duration = "Desconocido.";
                                        onComplete();
                                    } else e.printStackTrace();
                                }

                                @Override
                                public void onComplete() {
                                    final AnikumiiBottomSheetDialog anikumiiBottomSheetDialog = new AnikumiiBottomSheetDialog(context);
                                    final View specificView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_episodes_about, null);

                                    final ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.rgb(66, 104, 179));
                                    final StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);

                                    ((TextView) specificView.findViewById(R.id.episodes_bottom_sheet_about_aired)).setText(Utils.getBold("Emitido: " + aired, foregroundColorSpan, styleSpan));

                                    ((TextView) specificView.findViewById(R.id.episodes_bottom_sheet_about_duration)).setText(Utils.getBold("Duración: " + duration, foregroundColorSpan, styleSpan));

                                    Glide.with(context).load(imageUrl).into((ImageView) specificView.findViewById(R.id.episodes_bottom_sheet_about_header));

                                    ((TextView) specificView.findViewById(R.id.episodes_bottom_sheet_about_synopsis)).setText(Utils.getBold(synopsis.replace("Synopsis", "Sinopsis:"), foregroundColorSpan, styleSpan));

                                    anikumiiBottomSheetDialog.initialize(title, specificView, R.color.colorPrimary);

                                    if (!isDisposed())
                                        dispose();
                                }
                            }));
            // animeImageView = view.findViewById(R.id.episodes_img);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final ItemsModel items = animeList.get(getAdapterPosition());
            Intent videoAct = new Intent(context, VideoPlayerActivity.class);
            videoAct.putExtra("chapterUrl", items.getChapterUrl());
            context.startActivity(videoAct);
        }
    }

    class Header extends RecyclerView.ViewHolder {
        Header(View view) {
            super(view);
            ratingView = view.findViewById(R.id.AnimeRatingView);
            rootViewHeader = view;
            statusTextView = view.findViewById(R.id.nextEpisode_date);
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
