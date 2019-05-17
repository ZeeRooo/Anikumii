package com.zeerooo.anikumii2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.EpisodesViewPagerAdapter;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.fragments.MALInfoFragment;
import com.zeerooo.anikumii2.fragments.TioFLVFragment;
import com.zeerooo.anikumii2.misc.Utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class EpisodesActivity extends AppCompatActivity {
    //   private boolean isFav, isFollowing, isPending;
    private String animeID, tio = "https://tioanime.com/", rawUrl, animeName;
    private ViewPager viewPager;
    private Bundle tioAnimeBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodios);

        setReactive();

        viewPager = findViewById(R.id.episodes_viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.episodesToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setReactive() {
        rawUrl = getIntent().getStringExtra("animeUrl");
        animeName = getIntent().getStringExtra("animeName");
        setCollapsingTitle();

        Observable
                .just(true)
                .subscribeOn(Schedulers.computation())
                .doOnNext((Boolean aBoolean) -> networkOperation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (!isDestroyed()) {
                            final Snackbar snackbar = AnikumiiUiHelper.Snackbar(findViewById(R.id.act_episodes_rootView), getResources().getString(R.string.rxerror), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Reintentar", (View view) -> {
                                dispose();
                                setReactive();
                                snackbar.dismiss();
                            });
                            snackbar.show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (!isDestroyed())
                            onCompleteMethod();

                        dispose();
                    }
                });
    }

    private void networkOperation() throws Exception {
        ArrayList<String> genreList = new ArrayList<>(), listRel = new ArrayList<>();
        // DataBaseHelper db = new DataBaseHelper(this);

        Element e = AnikumiiWebHelper.go(tio + rawUrl, this).get().body();

        animeID = Utils.getNumberFromString(e.selectFirst("figure.backdrop > img").attr("src"), "[0-9]+");

        // Cursor cursor = db.getReadableDatabase().rawQuery("SELECT ANIMEID, GENRES, TYPE, TITLE FROM AnimesDB WHERE URL LIKE '" + rawUrl + "'", null);
       /* if (cursor.moveToNext()) {
            animeID = cursor.getString(0);
            genreList = new ArrayList<>(Arrays.asList(cursor.getString(1).split(",")));
            animeType = cursor.getString(2);
            animeName = cursor.getString(3);
        }
        cursor.close();
        db.close();*/

        String about = e.getElementsByClass("sinopsis").text();
      /*  isFav = e.getElementById("add_favorite").attr("style").contains("none");
        isPending = e.getElementById("add_pending").attr("style").contains("none");
        isFollowing = e.getElementById("follow_anime").attr("style").contains("none");*/

        //  String ratingStr = e.getElementsByClass("vtprmd").text();
        String status = e.getElementsByClass("next-episode text-success rounded fa-calendar-alt far d-inline-flex align-items-center").text();
        if (status.isEmpty())
            status = "Finalizado";

        Elements relElements = e.getElementsByClass("anime sm");
        for (byte relElementsCount = 0; relElementsCount < relElements.size(); relElementsCount++) {
            listRel.add(relElements.get(relElementsCount).text() + "-_" + (relElements.get(relElementsCount).getElementsByTag("a").attr("href")));
        }

        Elements genresElements = e.getElementsByClass("btn btn-sm btn-light rounded-pill");
        for (byte genresCount = 0; genresCount < genresElements.size(); genresCount++) {
            genreList.add(genresElements.get(genresCount).text());
        }

        tioAnimeBundle.putString("ratingStr", "2.1");
        tioAnimeBundle.putString("nextEpisode_date", status);
        tioAnimeBundle.putString("anime_about", about);
        //    animeFlvBundle.putString("animeType", animeType);
        tioAnimeBundle.putStringArrayList("genreList", genreList);
        tioAnimeBundle.putStringArrayList("listRel", listRel);
    }

    private void onCompleteMethod() {
        // Setup Glide
        GlideApp.with(this).load("https://tioanime.com/uploads/fondos/" + animeID + ".jpg").error(Glide.with(this).asDrawable().load("https://tioanime.com/uploads/portadas/" + animeID + ".jpg")).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into((ImageView) findViewById(R.id.anime_cover));

        EpisodesViewPagerAdapter episodesViewPagerAdapter = new EpisodesViewPagerAdapter(getSupportFragmentManager());

        tioAnimeBundle.putString("animeId", animeID);
        tioAnimeBundle.putString("animeUrl", rawUrl);
        tioAnimeBundle.putString("animeName", animeName);
      /*  animeFlvBundle.putBoolean("isFav", isFav);
        animeFlvBundle.putBoolean("isFollowing", isFollowing);
        animeFlvBundle.putBoolean("isPending", isPending);*/

        TioFLVFragment tioFragment = new TioFLVFragment();
        tioFragment.setArguments(tioAnimeBundle);
        episodesViewPagerAdapter.addFragment(tioFragment, "TioAnime");

        Bundle malBundle = new Bundle();
        malBundle.putString("anime_name", animeName);
        MALInfoFragment malInfoFragment = new MALInfoFragment();
        malInfoFragment.setArguments(malBundle);
        episodesViewPagerAdapter.addFragment(malInfoFragment, "MyAnimeList");

        viewPager.setAdapter(episodesViewPagerAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setReactive();
    }

    private void setCollapsingTitle() {
        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(animeName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.episodes_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shareAnime:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, tio + rawUrl);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.commentsAnime:
                startActivity(new Intent(this, CommentsActivity.class).putExtra("rawUrl", tio + rawUrl));
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}