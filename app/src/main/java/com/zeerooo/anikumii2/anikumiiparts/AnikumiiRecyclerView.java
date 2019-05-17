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
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterAnimes;
import com.zeerooo.anikumii2.adapters.AdapterEpisodes;
import com.zeerooo.anikumii2.misc.ItemsModel;
import com.zeerooo.anikumii2.misc.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnikumiiRecyclerView extends RecyclerView {
    private ArrayList<ItemsModel> arrayList = new ArrayList<>();
    private short page;
    private int findFirstVisibleItemPosition;
    private String toLoad, elementClass;
    private boolean loading;
    private Disposable disposable;
    private OnScrollListener listener;
    private Observable<Short> observable;

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

    public void exit() {
        if (!disposable.isDisposed())
            disposable.dispose();
    }

    public void clearArray() {
        arrayList.clear();
    }

    public void setDynamicListener(final byte maxDisplayedItems) {
        page = 1;
        loading = false;

        observable = Observable
                .create(new ObservableOnSubscribe<Short>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Short> emitter) {
                        emitter.onNext((short) 0);

                        listener = new OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                if (getLayoutManager().getItemCount() > maxDisplayedItems) {

                                    if (toLoad.startsWith("https://tioanime.com/anime/"))
                                        findFirstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                                    else
                                        findFirstVisibleItemPosition = ((GridLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

                                    if (!loading && (getLayoutManager().getChildCount() + findFirstVisibleItemPosition) >= getLayoutManager().getItemCount()) {
                                        emitter.onNext(page);
                                        loading = true;
                                    }
                                } else {
                                    removeOnScrollListener(this);
                                    emitter.onComplete();
                                }
                            }
                        };

                        addOnScrollListener(listener);
                    }
                });

        setDisposable();
    }

    private void setDisposable() {
        disposable = observable
                .distinct()
                .observeOn(Schedulers.io())
                .doOnNext((Short short1) -> {
                    loading = true;
                    setArrayList(toLoad, elementClass);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Short short1) -> {
                            loading = false;
                            if (toLoad.startsWith("https://tioanime.com/anime/"))
                                ((AdapterEpisodes) getAdapter()).addAll(arrayList);
                            else
                                ((AdapterAnimes) Objects.requireNonNull(getAdapter())).addAll(arrayList);
                        },
                        (Throwable throwable) -> {
                            throwable.printStackTrace();
                            final Snackbar snackbar = AnikumiiUiHelper.Snackbar(this, getResources().getString(R.string.rxerror), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Reintentar", (View view) -> {
                                disposable.dispose();
                                setDisposable();
                                snackbar.dismiss();
                            });
                            snackbar.show();

                        },
                        this::exit);
    }

    private void setArrayList(String toLoad, String elementClass) throws Exception {
        arrayList.clear();
        String title, number = "", img_url, chapterUrl, titleNumber, episodesStr;
        Document doc;
        int textColor;
System.out.println(toLoad + "&p=" + page);
        doc = AnikumiiWebHelper.go(toLoad + "&p=" + page, getContext()).get();

        try {
            if (toLoad.startsWith("https://tioanime.com/anime/")) {

                title = doc.selectFirst("h1.title").text();

                episodesStr = doc.select("body > script:containsData(anime_info)").toString();

                if (page == 1) {
                    arrayList.add(0, null); //Blank space for the header

                    page = Short.parseShort(Utils.getNumberFromString(episodesStr, "\\[(\\d+)").replace("[", ""));
                }

                for (short episodesCount = page; (episodesCount > page - 12) && (episodesCount > 0); episodesCount--) {
                    //if (!episodes.get(episodesCount - 1).select("i.Svg-ic").isEmpty())
                    //  textColor = Color.parseColor("#00bcf2");
                    /* else*/
                    textColor = -1275068417;

                    arrayList.add(new ItemsModel(title, "Episodio " + episodesCount, "https://tioanime.com/uploads/portadas/" + elementClass.split("---")[1] + ".jpg", episodesStr.split("\",\"")[1] + "-" + episodesCount, textColor));

                    page--;
                }
            } else {
                Elements episodes = doc.select(elementClass);

                for (short episodesCount = 0; episodesCount < episodes.size(); episodesCount++) {

                    titleNumber = episodes.get(episodesCount).getElementsByClass("title").text();

                    title = titleNumber.split(" [0-9]")[0];
                    if (elementClass.equals(".episodes > li > article"))
                        number = "Episodio " + Utils.getNumberFromString(titleNumber, "[0-9]+");

                    img_url = episodes.get(episodesCount).getElementsByTag("img").attr("src");
                    chapterUrl = episodes.get(episodesCount).select("a").attr("href");

                    arrayList.add(new ItemsModel(title, number, img_url, chapterUrl));
                }
                page++;
            }
        } catch (IndexOutOfBoundsException e) {
            removeOnScrollListener(listener);
        } finally {
            if (arrayList.isEmpty())
                Snackbar.make(AnikumiiRecyclerView.this, getContext().getString(R.string.search_failed), Snackbar.LENGTH_LONG).show();
        }
    }
}