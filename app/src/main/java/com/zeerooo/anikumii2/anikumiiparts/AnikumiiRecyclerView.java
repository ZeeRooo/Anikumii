package com.zeerooo.anikumii2.anikumiiparts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterMain;
import com.zeerooo.anikumii2.misc.ItemsModel;
import com.zeerooo.anikumii2.misc.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

public class AnikumiiRecyclerView extends RecyclerView {
    private ArrayList<ItemsModel> arrayList = new ArrayList<>();
    private short page;
    private int findFirstVisibleItemPosition, textColor;
    private String toLoad, elementClass, title, number, img_url, chapterUrl, episodesStr;
    private boolean loading;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Document document;
    private PublishProcessor<Short> publishProcessor = PublishProcessor.create();
    private byte maxDisplayedItems;

    public AnikumiiRecyclerView(@NonNull Context context) {
        super(context, null);
    }

    public AnikumiiRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public AnikumiiRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setToLoad(String toLoad) {
        this.toLoad = toLoad;
    }

    public void setElementClass(String elementClass) {
        this.elementClass = elementClass;
    }

    public void setMaxDisplayedItems(byte maxDisplayedItems) {
        this.maxDisplayedItems = maxDisplayedItems;

        page = 1;

        publishProcessor.onNext((short) 0);
    }

    public void exit() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
        loading = false;
    }

    public void setDynamicListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (getLayoutManager().getItemCount() > maxDisplayedItems) {

                    if (toLoad.contains("/anime/") || toLoad.contains("/hentai/"))
                        findFirstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                    else
                        findFirstVisibleItemPosition = ((GridLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

                    if (!loading && (getLayoutManager().getChildCount() + findFirstVisibleItemPosition) >= getLayoutManager().getItemCount()) {
                        publishProcessor.onNext(page);
                    }
                } else {
                    loading = false;
                }
            }
        });

        setDisposable();
    }

    private void setDisposable() {
        compositeDisposable.add(publishProcessor
                .onBackpressureDrop()
                .concatMapSingle(page -> dataFromNetwork()
                        .subscribeOn(Schedulers.io()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                            loading = false;

                            ((AdapterMain) getAdapter()).appendArray(arrayList);

                            if (arrayList.isEmpty() && getLayoutManager().getItemCount() < 1)
                                Snackbar.make(AnikumiiRecyclerView.this, getResources().getString(R.string.search_failed), Snackbar.LENGTH_LONG).show();
                        },
                        (Throwable throwable) -> {
                            final Snackbar snackbar = AnikumiiUiHelper.Snackbar(AnikumiiRecyclerView.this, getResources().getString(R.string.rxerror), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Reintentar", (View view) -> {
                                compositeDisposable.clear();
                                setDisposable();
                                snackbar.dismiss();
                            });
                            snackbar.show();
                        }));

        publishProcessor.onNext(page);
    }

    private Single<ArrayList<ItemsModel>> dataFromNetwork() {
        return Single
                .just(true)
                .map(new Function<Boolean, ArrayList<ItemsModel>>() {
                    @Override
                    public ArrayList<ItemsModel> apply(Boolean aBoolean) throws Exception {
                        arrayList.clear();
                        loading = true;

                        document = AnikumiiWebHelper.go(toLoad + "&p=" + page, getContext()).get();

                        if (toLoad.contains("/anime/") || toLoad.contains("/hentai/")) {

                            title = document.selectFirst("h1.title").text();

                            episodesStr = document.select("body > script:containsData(anime_info)").toString();

                            if (page == 1) {
                                arrayList.add(0, null); //Blank space for the header

                                page = Short.parseShort(Utils.matcher(episodesStr, "\\[(\\d+)"));
                            }

                            for (short episodesCount = page; (episodesCount > page - 12) && (episodesCount > 0); episodesCount--) {
                                /*if (!episodes.get(episodesCount - 1).select("i.Svg-ic").isEmpty())
                                  textColor = Color.parseColor("#00bcf2");
                                 else*/
                                textColor = -1275068417;

                                arrayList.add(new ItemsModel(title, "Episodio " + episodesCount, Anikumii.dominium + "/uploads/portadas/" + elementClass.split("---")[1] + ".jpg", episodesStr.split("\",\"")[1] + "-" + episodesCount, textColor));

                                page--;
                            }
                        } else {
                            Elements episodes = document.select(elementClass);

                            for (short episodesCount = 0; episodesCount < episodes.size(); episodesCount++) {
                                title = episodes.get(episodesCount).getElementsByClass("title").text();

                                if (elementClass.equals("article.episode")) {
                                    number = Utils.matcher(title, "(\\d+)\\D*$");
                                    title = title.replace(number, "");
                                } else
                                    number = "Anime / OVA / Pelicula / Especial";

                                img_url = episodes.get(episodesCount).getElementsByTag("img").attr("src");
                                chapterUrl = episodes.get(episodesCount).select("a").attr("href");

                                arrayList.add(new ItemsModel(title, "Episodio " + number, img_url, chapterUrl));
                            }
                            page++;
                        }

                        return arrayList;
                    }
                });
    }
}