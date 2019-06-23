package com.zeerooo.anikumii.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.ViewPagerAdapter;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii.fragments.MALInfoFragment;
import com.zeerooo.anikumii.fragments.TioEpisodesFragment;
import com.zeerooo.anikumii.misc.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class EpisodesActivity extends AppCompatActivity {
    private String animeID, url, animeName;
    private int malID;
    private byte malPosition;
    private ViewPager viewPager;
    private Bundle tioAnimeBundle = new Bundle();
    private ArrayList<String> genreList = new ArrayList<>(), listRel = new ArrayList<>();
    private Element element;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodios);

        MaterialToolbar materialToolbar = findViewById(R.id.episodesToolbar);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            materialToolbar.setPadding(0, 24, 0, 0);
        }

        viewPager = findViewById(R.id.episodes_viewPager);
        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(viewPager);

        setSupportActionBar(materialToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setReactive();
    }

    private void setReactive() {
        if (getIntent().getDataString() == null)
            url = getIntent().getStringExtra("animeUrl");
        else {
            url = getIntent().getDataString();
            Anikumii.dominium = Utils.matcher(url, "(https://.*?/)");
        }

        Observable
                .just(true)
                .observeOn(Schedulers.io())
                .doOnNext(aBoolean -> {
                    element = AnikumiiWebHelper.go(url, this).get().body();
                    animeName = element.selectFirst("h1.title").text();
                    ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(animeName);
                    animeID = Utils.matcher(element.selectFirst("figure.backdrop > img").attr("src"), "/([0-9]+)");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aBoolean -> GlideApp.with(EpisodesActivity.this).load(Anikumii.dominium + "/uploads/fondos/" + animeID + ".jpg").error(Glide.with(EpisodesActivity.this).asDrawable().load("https://tioanime.com/uploads/portadas/" + animeID + ".jpg")).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into((ImageView) findViewById(R.id.anime_cover)))
                .observeOn(Schedulers.computation())
                .doOnNext(aBoolean -> networkOperation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (!isDestroyed()) {
                            AnikumiiUiHelper.Snackbar(findViewById(R.id.act_episodes_rootView), Snackbar.LENGTH_INDEFINITE, e.toString(), view -> {
                                dispose();
                                setReactive();
                                AnikumiiUiHelper.snackbar.dismiss();
                            }).show();
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
        String about = element.getElementsByClass("sinopsis").text();

        String status = element.getElementsByClass("next-episode text-success rounded fa-calendar-alt far d-inline-flex align-items-center").text();
        if (status.isEmpty())
            status = "Finalizado";

        Elements relElements = element.getElementsByClass("anime sm");
        for (byte relElementsCount = 0; relElementsCount < relElements.size(); relElementsCount++) {
            listRel.add(relElements.get(relElementsCount).text() + "-_" + (relElements.get(relElementsCount).getElementsByTag("a").attr("href")));
        }

        Elements genresElements = element.getElementsByClass("btn btn-sm btn-light rounded-pill");
        for (byte genresCount = 0; genresCount < genresElements.size(); genresCount++) {
            genreList.add(genresElements.get(genresCount).text());
        }

        try {
            MALInfoFragment.MAL = new JSONObject(new AnikumiiConnection().getStringResponse("GET", "https://api.jikan.moe/v3/search/anime/?q=" + Utils.encodeString(animeName), null));
            JSONArray jsonArray = MALInfoFragment.MAL.getJSONArray("results");

            for (byte i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("title").toLowerCase().replace(" ", "").equals(animeName.toLowerCase().replace(" ", ""))) {
                    malID = jsonArray.getJSONObject(i).getInt("mal_id");
                    malPosition = i;
                    break;
                }
            }

            if (malID == 0) {
                malID = jsonArray.getJSONObject(0).getInt("mal_id");
                malPosition = 0;
            }

            tioAnimeBundle.putString("animeType", jsonArray.getJSONObject(malPosition).getString("type"));
            tioAnimeBundle.putString("malUrl", jsonArray.getJSONObject(malPosition).getString("url"));

            String episodes = jsonArray.getJSONObject(malPosition).getString("score");
            if (!episodes.equals("0"))
                episodes = episodes.substring(0, 3);
            tioAnimeBundle.putString("ratingStr", episodes);
        } catch (Exception e) {
            tioAnimeBundle.putString("ratingStr", "0.0");
            tioAnimeBundle.putString("animeType", "-");
            tioAnimeBundle.putString("malUrl", "");
            Snackbar.make(findViewById(R.id.act_episodes_rootView), getResources().getString(R.string.jikan_exception), Snackbar.LENGTH_LONG).show();
        }

        tioAnimeBundle.putString("nextEpisodeDate", status);
        tioAnimeBundle.putString("animeAbout", about);
        tioAnimeBundle.putStringArrayList("genreList", genreList);
        tioAnimeBundle.putStringArrayList("listRel", listRel);
    }

    private void onCompleteMethod() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 1);

        tioAnimeBundle.putString("animeId", animeID);
        tioAnimeBundle.putString("animeUrl", url);
        tioAnimeBundle.putString("animeName", animeName);
        tioAnimeBundle.putBoolean("isFav", false);

        TioEpisodesFragment tioEpisodesFragment = new TioEpisodesFragment();
        tioEpisodesFragment.setArguments(tioAnimeBundle);
        viewPagerAdapter.addFragment(tioEpisodesFragment, "Episodios");

        Bundle malBundle = new Bundle();
        malBundle.putInt("malID", malID);
        MALInfoFragment malInfoFragment = new MALInfoFragment();
        malInfoFragment.setArguments(malBundle);
        viewPagerAdapter.addFragment(malInfoFragment, "MyAnimeList");

        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setReactive();
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.commentsAnime:
                startActivity(new Intent(this, CommentsActivity.class).putExtra("rawUrl", url));
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}