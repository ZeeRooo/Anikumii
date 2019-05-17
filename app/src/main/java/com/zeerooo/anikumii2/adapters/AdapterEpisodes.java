package com.zeerooo.anikumii2.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.activities.EpisodesActivity;
import com.zeerooo.anikumii2.activities.VPlayerActivity;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiDialog;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.anikumiiparts.AnimeRatingView;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.fragments.AnimesRecyclerViewFragment;
import com.zeerooo.anikumii2.misc.ItemsModel;
import com.zeerooo.anikumii2.services.AnimeFlvApiService;

import java.util.ArrayList;

/**
 * Created by ZeeRooo on 07/01/18
 */

public class AdapterEpisodes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private ArrayList<ItemsModel> animeList = new ArrayList<>();
    private String cookie, ratingStr, nextEpisodeDate, aboutStr, animeId, typeStr;
    private byte parentID = 0;//, month = 1;
    private ArrayList<String> genreList, listRel;
    private View rootViewHeader;
    private TextView number, nextEpisode, about, readMore, type;
    private ImageView img;
    private AnimeRatingView ratingView;
    private ImageButton fav, follow, pending;
    private boolean isFav, isFollowing, isPending, loadHeader = true;

    public AdapterEpisodes(Activity mActivity, String animeId, String ratingStr, String nextEpisodeDate, String aboutStr, String type, ArrayList<String> genreList, ArrayList<String> listRel, boolean isFav, boolean isFollowing, boolean isPending) {
        this.mActivity = mActivity;
        this.cookie = ((Anikumii) mActivity.getApplication()).getUserCookie();
        this.ratingStr = ratingStr;
        this.nextEpisodeDate = nextEpisodeDate;
        this.genreList = genreList;
        this.listRel = listRel;
        this.isFav = isFav;
        this.isFollowing = isFollowing;
        this.isPending = isPending;
        this.aboutStr = aboutStr;
        this.animeId = animeId;
        this.typeStr = type;
    }

    public void addAll(ArrayList<ItemsModel> arrayList) {
        animeList.addAll(arrayList);
        // notifyDataSetChanged();
        // notifyItemInserted(animeList.size());
        notifyItemRangeInserted(animeList.size(), arrayList.size());
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType != 0) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
            return new MyViewHolder(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.episodes_act_header, parent, false);
            return new Header(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ItemsModel items = animeList.get(position);
            if (items != null) {
                number.setText(items.getNumber());
                number.setTextColor(items.getTextColor());

                GlideApp.with(mActivity).load(items.getImg_url()).error(Glide.with(mActivity).asDrawable().load(items.getImg_url().replace("animeflv.net/uploads/animes/", "cdn.animeflv.net/"))).into(img);
            }
        } else if (holder instanceof Header && loadHeader) {
            ratingView.init(ratingStr);
            ratingView.setAnimatesProgress();
            TooltipCompat.setTooltipText(ratingView, ratingStr + " " + mActivity.getString(R.string.rating) + ". Toca para valorar");
            ratingView.setContentDescription(ratingStr + " " + mActivity.getString(R.string.rating) + ". Toca para valorar");

            ratingView.setOnClickListener((View view) -> {
                AnikumiiDialog anikumiiDialog = new AnikumiiDialog(mActivity);
                LinearLayout layout = new LinearLayout(mActivity);
                layout.setGravity(Gravity.CENTER);

                RatingBar starsBar = new RatingBar(mActivity);
                // starsBar.setMax(5);
                starsBar.setNumStars(5);
                starsBar.setStepSize(0.1f);
                starsBar.setRating(Float.parseFloat(ratingStr));
                starsBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                int color;
                switch ((byte) Double.parseDouble(ratingStr)) {
                    case 1:
                        color = Color.parseColor("#e32228");
                        break;
                    case 2:
                        color = Color.parseColor("#f27127");
                        break;
                    case 3:
                        color = Color.parseColor("#f7cb19");
                        break;
                    case 4:
                        color = Color.parseColor("#73b045");
                        break;
                    default:
                        color = Color.parseColor("#03804e");
                        break;
                }

                DrawableCompat.setTint(starsBar.getProgressDrawable(), color);
                starsBar.setOnRatingBarChangeListener((RatingBar ratingBar, float rating, boolean fromUser) -> {
                    Intent service = new Intent(mActivity, AnimeFlvApiService.class);
                    service.putExtra("toLoad", "https://animeflv.net/api/animes/rate").putExtra("params", "rating=" + rating + "&id=" + animeId);
                    mActivity.startService(service);

                    Snackbar.make(rootViewHeader, "Anime valorado con " + rating + " " + mActivity.getString(R.string.rating), Snackbar.LENGTH_LONG).show();
                    anikumiiDialog.cancel();
                });

                layout.addView(starsBar);
                anikumiiDialog.initialize("Valorar anime", layout);
            });
            type.setText(typeStr);
            type.setContentDescription("Tipo " + typeStr);

            for (byte genreCount = 0; genreCount < genreList.size(); genreCount++) {
                final String genre = genreList.get(genreCount);
                Chip genreChip = new Chip(mActivity);
                genreChip.setText(genre);
                genreChip.setTextSize(14);
                // genreChip.setTextColor(Color.parseColor("#00bcf2"));
                genreChip.setOnClickListener((View view) -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("toLoad", "https://tioanime.com/directorio?genero=" + genre);
                    bundle.putString("genre", genre);
                    bundle.putString("element", "article.anime");
                    FragmentManager fragmentManager = ((EpisodesActivity) mActivity).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    AnimesRecyclerViewFragment fragment = new AnimesRecyclerViewFragment();
                    fragment.setArguments(bundle);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.act_episodes_rootView, fragment);
                    fragmentTransaction.commit();
                });

                ((ChipGroup) rootViewHeader.findViewById(R.id.chipGroups)).addView(genreChip);
            }

            //if (!cookie.equals("f;f;f;f;f;f")) {
            if (!cookie.equals("f;f;f")) {
                if (isFav) {
                    fav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_favorite));
                    TooltipCompat.setTooltipText(fav, mActivity.getString(R.string.bottom_sheet_removeFav));
                    fav.setContentDescription(mActivity.getString(R.string.bottom_sheet_removeFav));
                } else {
                    fav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_favorite_border));
                    TooltipCompat.setTooltipText(fav, mActivity.getString(R.string.bottom_sheet_newFav));
                    fav.setContentDescription(mActivity.getString(R.string.bottom_sheet_newFav));
                }

                if (isPending) {
                    pending.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_pending));
                    TooltipCompat.setTooltipText(pending, mActivity.getString(R.string.bottom_sheet_removePending));
                    pending.setContentDescription(mActivity.getString(R.string.bottom_sheet_removePending));
                } else {
                    pending.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_pending_border));
                    TooltipCompat.setTooltipText(pending, mActivity.getString(R.string.bottom_sheet_newPending));
                    pending.setContentDescription(mActivity.getString(R.string.bottom_sheet_newPending));
                }

                if (isFollowing) {
                    follow.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_cross));
                    TooltipCompat.setTooltipText(follow, mActivity.getString(R.string.bottom_sheet_unfollow));
                    follow.setContentDescription(mActivity.getString(R.string.bottom_sheet_unfollow));
                } else {
                    follow.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_follow));
                    TooltipCompat.setTooltipText(follow, mActivity.getString(R.string.bottom_sheet_follow));
                    follow.setContentDescription(mActivity.getString(R.string.bottom_sheet_follow));
                }

                AnikumiiUiHelper.transparentBackground(fav);
                fav.setOnClickListener((View view) -> {
                    if (isFav) {
                        buttonHelper("favorite", "1");
                        fav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_favorite_border));
                        TooltipCompat.setTooltipText(fav, mActivity.getString(R.string.bottom_sheet_removeFav));
                        fav.setContentDescription(mActivity.getString(R.string.bottom_sheet_removeFav));
                    } else {
                        buttonHelper("favorite", "0");
                        fav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_favorite));
                        TooltipCompat.setTooltipText(fav, mActivity.getString(R.string.bottom_sheet_removeFav));
                        fav.setContentDescription(mActivity.getString(R.string.bottom_sheet_removeFav));
                    }
                    isFav = !isFav;
                });

                AnikumiiUiHelper.transparentBackground(follow);
                follow.setOnClickListener((View view) -> {
                    if (isFollowing) {
                        buttonHelper("follow", "1");
                        follow.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_follow));
                        TooltipCompat.setTooltipText(follow, mActivity.getString(R.string.bottom_sheet_unfollow));
                        follow.setContentDescription(mActivity.getString(R.string.bottom_sheet_unfollow));
                    } else {
                        buttonHelper("follow", "0");
                        follow.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_cross));
                        TooltipCompat.setTooltipText(follow, mActivity.getString(R.string.bottom_sheet_follow));
                        follow.setContentDescription(mActivity.getString(R.string.bottom_sheet_follow));
                    }
                    isFollowing = !isFollowing;
                });

                AnikumiiUiHelper.transparentBackground(pending);
                pending.setOnClickListener((View view) -> {
                    if (isPending) {
                        buttonHelper("pending", "1");
                        pending.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_pending_border));
                        TooltipCompat.setTooltipText(pending, mActivity.getString(R.string.bottom_sheet_removePending));
                        pending.setContentDescription(mActivity.getString(R.string.bottom_sheet_removePending));
                    } else {
                        buttonHelper("pending", "0");
                        pending.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_pending));
                        TooltipCompat.setTooltipText(pending, mActivity.getString(R.string.bottom_sheet_newPending));
                        pending.setContentDescription(mActivity.getString(R.string.bottom_sheet_newPending));
                    }
                    isPending = !isPending;
                });
            } else {
                fav.setVisibility(View.GONE);
                follow.setVisibility(View.GONE);
                pending.setVisibility(View.GONE);
            }

            about.setText(aboutStr);
            AnikumiiUiHelper.transparentBackground(readMore);
            about.post(() -> {
                if (about.getLineCount() > 3) {
                    byte line = (byte) about.getLineCount();
                    about.setMaxLines(3);
                    readMore.setVisibility(View.VISIBLE);
                    readMore.setPadding(0, 0, 0, 10);
                    readMore.setOnClickListener(new View.OnClickListener() {
                        boolean isFull = true;

                        @Override
                        public void onClick(View view) {
                            if (isFull) {
                                ObjectAnimator
                                        .ofInt(about, "maxLines", 3, line)
                                        .setDuration(100)
                                        .start();
                                about.setEllipsize(null);
                                readMore.setText(mActivity.getString(R.string.readLess));
                            } else {
                                ObjectAnimator
                                        .ofInt(about, "maxLines", line, 3)
                                        .setDuration(100)
                                        .start();
                                about.setEllipsize(TextUtils.TruncateAt.END);
                            }
                            isFull = !isFull;
                        }
                    });

                } else
                    readMore.setVisibility(View.GONE);
            });

            nextEpisode.setText(nextEpisodeDate);

           /* if (nextEpisodeDate.contains("Fecha Próximo"))
                nextEpisode.setTextColor(Color.parseColor("#00b046"));
            else if (nextEpisodeDate.equals("Finalizado"))
                nextEpisode.setTextColor(Color.parseColor("#fd3246"));
            else
                nextEpisode.setTextColor(Color.parseColor("#00b046"));*/
            if (nextEpisodeDate.equals("Finalizado"))
                nextEpisode.setTextColor(Color.parseColor("#fd3246"));
            else
                nextEpisode.setTextColor(Color.parseColor("#73b045"));

            for (byte listRelCount = 0; listRelCount < listRel.size(); listRelCount++) {
                final String s = listRel.get(listRelCount);
                parentID++;
                Chip chipRel = new Chip(mActivity);
                chipRel.setId(parentID);
                chipRel.setEllipsize(TextUtils.TruncateAt.END);
                chipRel.setText(s.split("-_")[0]);
                chipRel.setTextColor(Color.parseColor("#00bcf2"));
                chipRel.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#5901bcf2")));
                chipRel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                chipRel.setOnClickListener((View view) -> {
                    Intent episodesAct = new Intent(mActivity, EpisodesActivity.class);
                    episodesAct.putExtra("animeUrl", s.split("-_")[1]).putExtra("animeName", s.split("-_")[0]);
                    mActivity.startActivity(episodesAct);
                });
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                if (parentID == 1) {
                    if (fav.getVisibility() == View.VISIBLE)
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

    private void buttonHelper(String action, String doStr) {
        String params = "anime_id=" + animeId + "&action=" + action + "&do=" + doStr;
        Intent service = new Intent(mActivity, AnimeFlvApiService.class);
        service.putExtra("toLoad", "https://animeflv.net/api/animes/library").putExtra("params", params);
        mActivity.startService(service);
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    /*private RequestListener<Drawable> requestListener(final String title, final String number) {
        final Handler handler = new Handler();
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(mActivity).load("http://animekb.com/wp-content/uploads/2018/0" + month + "/" + (title.toLowerCase() + "-" + number.toLowerCase()).replace(" ", "-").replace("!", "") + ".jpg").listener(requestListener(title, number)).into(img);
                    }
                });
                month++;
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                handler.removeCallbacksAndMessages(null);
                return false;
            }
        };
    }*/

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        MyViewHolder(View view) {
            super(view);
            number = view.findViewById(R.id.episodes_number);
            img = view.findViewById(R.id.episodes_img);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ItemsModel items = animeList.get(getAdapterPosition());
            Intent videoAct = new Intent(mActivity, VPlayerActivity.class);
            videoAct.putExtra("chapterUrl", "https://tioanime.com/ver/" + items.getChapterUrl()).putExtra("chapterTitle", items.getTitle()).putExtra("chapterNumber", items.getNumber());
            mActivity.startActivity(videoAct);
        }
    }

    class Header extends RecyclerView.ViewHolder {
        Header(View view) {
            super(view);
            ratingView = view.findViewById(R.id.AnimeRatingView);
            rootViewHeader = view;
            nextEpisode = view.findViewById(R.id.nextEpisode_date);
            fav = view.findViewById(R.id.add_anime_fav);
            follow = view.findViewById(R.id.add_anime_follow);
            pending = view.findViewById(R.id.add_anime_pending);
            about = view.findViewById(R.id.anime_about);
            readMore = view.findViewById(R.id.readMore);
            type = view.findViewById(R.id.animeType);
        }
    }
}
