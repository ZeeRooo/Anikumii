package com.zeerooo.anikumii.anikumiiparts;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.AdapterMain;
import com.zeerooo.anikumii.misc.DataBaseHelper;
import com.zeerooo.anikumii.misc.ItemsModel;
import com.zeerooo.anikumii.misc.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AnikumiiRecyclerView extends RecyclerView {
    private final ArrayList<ItemsModel> arrayList = new ArrayList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final PublishProcessor<Short> publishProcessor = PublishProcessor.create();
    private short page;
    private int findFirstVisibleItemPosition;
    private boolean loading;
    private byte maxDisplayedItems;
    private String toLoad, elementClass, title, number, img_url, chapterUrl, episodesStr;
    private Document document;
    private View rootView;
    private Cursor cursor;
    private DataBaseHelper dataBaseHelper;

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

    public void setRootView(View view) {
        rootView = view;
    }

    public void setMaxDisplayedItems(byte maxDisplayedItems) {
        this.maxDisplayedItems = maxDisplayedItems;

        if (page == 0)
            setDisposable();

        page = 1;

        publishProcessor.onNext(page);
    }

    public void initDB() {
        dataBaseHelper = new DataBaseHelper(getContext());
    }

    public void closeDB() {
        if (dataBaseHelper != null)
            dataBaseHelper.close();
        if (cursor != null && !cursor.isClosed())
            cursor.close();
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

                    if (toLoad.equals("history") || toLoad.contains("/anime/") || toLoad.contains("/hentai/"))
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
    }

    private void setDisposable() {
        compositeDisposable.add(
                publishProcessor
                        .onBackpressureDrop()
                        .concatMapSingle(page -> dataFromNetwork())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(arrayList -> {
                                    loading = false;

                                    ((AdapterMain) getAdapter()).appendArray(arrayList);

                                    if (arrayList.isEmpty() && getLayoutManager().getItemCount() < 1) {
                                        Snackbar.make(rootView, getResources().getString(R.string.search_failed), Snackbar.LENGTH_LONG).show();
                                    }
                                },
                                throwable -> {
                                    // throwable.printStackTrace();
                                    AnikumiiUiHelper.errorSnackbar(rootView, Snackbar.LENGTH_INDEFINITE, throwable.toString(), view -> {
                                        compositeDisposable.clear();
                                        page = 0;
                                        setMaxDisplayedItems(maxDisplayedItems);
                                        AnikumiiUiHelper.snackbar.dismiss();
                                    }).show();
                                }));
    }

    private Single<ArrayList<ItemsModel>> dataFromNetwork() {
        return Single
                .just(true)
                .subscribeOn(Schedulers.io())
                .map(aBoolean -> {
                    arrayList.clear();
                    loading = true;

                    if (toLoad.startsWith("history")) {
                        if (page == 1)
                            page = 0;

                        cursor = dataBaseHelper.getReadableDatabase().rawQuery("SELECT ID, TITLE, TYPE, IMAGE, LASTEPISODE, DATE FROM AnimesDB ORDER BY POSITION DESC LIMIT 24 OFFSET " + 24 * page, null);

                        if (page == 0)
                            page = 1;

                        while (cursor.moveToNext()) {
                            arrayList.add(new ItemsModel(cursor.getString(1), cursor.getString(2) + " - Episodio " + cursor.getInt(4), cursor.getString(3), cursor.getString(0), cursor.getString(5)));
                        }

                        page++;
                    } else if (toLoad.contains("/anime/") || toLoad.contains("/hentai/")) {
                        document = AnikumiiWebHelper.go(toLoad + "&p=" + page, getContext()).get();

                        title = document.selectFirst("h1.title").text();

                        episodesStr = document.select("body > script:containsData(anime_info)").toString();

                        arrayList.add(0, null); //Blank space for the header

                        page = Short.parseShort(Utils.matcher(episodesStr, "\\[(\\d+)"));

                        initDB();

                        short lastEpisode;
                        int textColor;

                        try {
                            cursor = dataBaseHelper.getReadableDatabase().rawQuery("SELECT LASTEPISODE FROM AnimesDB WHERE TITLE LIKE ?", new String[]{"%" + title + "%"});
                            cursor.moveToLast();
                            lastEpisode = (short) cursor.getInt(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            lastEpisode = 0;
                        } finally {
                            closeDB();
                        }

                        for (short episodesCount = page; episodesCount > 0; episodesCount--) {
                            if (episodesCount <= lastEpisode)
                                textColor = -16728846;
                            else
                                textColor = -1275068417;

                            arrayList.add(new ItemsModel(title, "Episodio " + episodesCount, null, Anikumii.dominium + "/ver/" + episodesStr.split("\",\"")[1] + "-" + episodesCount, textColor));

                            page--;
                        }
                    } else {
                        document = AnikumiiWebHelper.go("https://tioanime.com/"/*toLoad + "&p=" + page*/, getContext()).get();

                        final Elements episodes = document.select(elementClass);

                        for (short episodesCount = 0; episodesCount < episodes.size(); episodesCount++) {
                            title = episodes.get(episodesCount).getElementsByClass("title").text();

                            if (elementClass.equals("article.episode")) {
                                number = Utils.matcher(title, "( \\d+)\\D*$");

                                if (number == null)
                                    number = "";

                                title = title.replace(number, "");
                                number = "Episodio" + number;
                            } else
                                number = "";

                            img_url = Anikumii.dominium + episodes.get(episodesCount).getElementsByTag("img").attr("src");
                            chapterUrl = episodes.get(episodesCount).select("a").attr("href");

                            arrayList.add(new ItemsModel(title, number, img_url, Anikumii.dominium + chapterUrl));
                        }
                        page++;
                    }
                    return arrayList;
                });
    }
}