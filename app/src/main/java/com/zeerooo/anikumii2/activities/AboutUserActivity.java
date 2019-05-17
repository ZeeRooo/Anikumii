package com.zeerooo.anikumii2.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AboutMeAdapter;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii2.misc.ItemsModel;

import org.jsoup.select.Elements;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class AboutUserActivity extends AppCompatActivity {

    private ArrayList<ItemsModel> favList = new ArrayList<>(), watchingNowList = new ArrayList<>(), toSeeList = new ArrayList<>();
    private String profileUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_user);

        String profileName = splitString(getIntent().getStringExtra("userName"));
        profileUrl = splitString(getIntent().getStringExtra("url"));

        setReactive();

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (getIntent() != null)
                getSupportActionBar().setTitle("Perfil de " + profileName);
        }
    }

    private void setReactive() {
        Observable
                .just(true)
                .subscribeOn(Schedulers.io())
                .doOnNext((Boolean aBoolean) -> networkOperation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isDestroyed()) {
                            final Snackbar snackbar = AnikumiiUiHelper.Snackbar(findViewById(R.id.aboutUserRootView), getResources().getString(R.string.rxerror), Snackbar.LENGTH_INDEFINITE);
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

    private String splitString(String string) {
        return string.split("\\?")[0];
    }

    private void networkOperation() throws Exception {
        Elements favorites = AnikumiiWebHelper.go(profileUrl + "/favoritos", this).get().select("article.Anime.alt");
        Elements watchingNow = AnikumiiWebHelper.go(profileUrl + "/siguiendo", this).get().select("article.Anime.alt");
        Elements todo = AnikumiiWebHelper.go(profileUrl + "/lista_espera", this).get().select("article.Anime.alt");

        loop(favorites, favList);
        loop(watchingNow, watchingNowList);
        loop(todo, toSeeList);
    }

    private void loop(Elements elements, ArrayList<ItemsModel> list) {
        for (byte elementsCount = 0; elementsCount < elements.size(); elementsCount++) {
            String title = elements.get(elementsCount).select("div > div > div > strong > a").text();
            String img = elements.get(elementsCount).select("div > figure > img").attr("src");
            String type = elements.get(elementsCount).getElementsByClass("Type tv").text();
            String url = elements.get(elementsCount).select("div > div > div > strong > a").attr("href");

            list.add(new ItemsModel(title, type, img, url));
        }
    }

    private void onCompleteMethod() {
        ArrayList<ItemsModel> list;
        String section_name;
        for (byte sections = 0; sections < 3; sections++) {
            if (sections == 0) {
                section_name = "Animes favoritos";
                list = favList;
            } else if (sections == 1) {
                section_name = "Animes que veo";
                list = watchingNowList;
            } else {
                section_name = "Animes que pienso ver";
                list = toSeeList;
            }
            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setId(sections);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new AboutMeAdapter(this, list, section_name, profileUrl));
            recyclerView.setHasFixedSize(true);
            ((LinearLayout) findViewById(R.id.aboutUser_linear)).addView(recyclerView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
