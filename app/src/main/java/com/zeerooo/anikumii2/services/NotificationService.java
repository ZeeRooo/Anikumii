package com.zeerooo.anikumii2.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.activities.VPlayerActivity;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii2.anikumiiparts.glide.GlideApp;
import com.zeerooo.anikumii2.misc.DataBaseHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZeeRooo on 23/01/18
 */

public class NotificationService extends Worker {
    private DataBaseHelper db;
    private Cursor cursor;
    private NotificationManager mNotificationManager;
    private boolean database, firstRun;
    private AnikumiiSharedPreferences mPreferences;
    private String animeFLV = "https://m.animeflv.net/";

    public NotificationService(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
        database = workerParameters.getInputData().getBoolean("updateDb", false);
        firstRun = workerParameters.getInputData().getBoolean("firstRun", false);
    }

    @NonNull
    @Override
    public Result doWork() {
        mPreferences = new AnikumiiSharedPreferences(getApplicationContext());
        try {
            if (!database) {
                Elements episodes = AnikumiiWebHelper.go(animeFLV, getApplicationContext()).get().select("li.Episode > a");
                StringBuilder knownAnimes = new StringBuilder();
                String knownAnimesStr = mPreferences.getString("last_anime", "");
                boolean saveKnown = false;

                if (mPreferences.getBoolean("enableNotif", false)) {

                    for (byte episodesCount = 0; episodesCount < 5; episodesCount++) {
                        String number = episodes.get(episodesCount).select("p").text();
                        String title = episodes.get(episodesCount).getElementsByClass("Title").text();

                        if (!knownAnimesStr.contains(title + number)) {
                            String url = "https://animeflv.net/" + episodes.get(episodesCount).select("a").attr("href");
                            String image = animeFLV + episodes.get(episodesCount).select(".Image > img").attr("src");

                            displayNotification(url, number, GlideApp.with(getApplicationContext()).asBitmap().load(image).apply(RequestOptions.circleCropTransform()).into(128, 128).get(), title, episodesCount);

                            if (number.equals("Episodio 1") || number.equals("Episodio 0")) {
                                database = true;
                            }
                        }

                        knownAnimes.append(title).append(number);
                        saveKnown = true;
                    }
                } else {
                    for (byte episodesCount = 0; episodesCount < episodes.size(); episodesCount++) {
                        String title = episodes.get(episodesCount).getElementsByClass("Title").text();
                        String number = episodes.get(episodesCount).select("li > a > p").text();

                        if ((number.equals("Episodio 1") || number.equals("Episodio 0")) && !knownAnimesStr.contains(title + number)) {
                            knownAnimes.append(title).append(number);
                            database = true;
                            break;
                        }

                        saveKnown = true;
                    }
                }

                if (saveKnown)
                    mPreferences.edit().putString("last_anime", knownAnimes.toString()).apply();
            }

            if (database)
                updateDb();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        } catch (IOException e) {
            e.printStackTrace();
            if (e.toString().contains("Status=503"))
                webView();
            else
                return Result.failure();
        } finally {
            if (database) {
                if (db != null)
                    db.close();
                if (!cursor.isClosed())
                    cursor.close();
                mNotificationManager.cancel(125);
            }
        }
        return Result.success();
    }

    private void webView() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Integer> emitter) {
                        WebView webView = new WebView(getApplicationContext());
                        webView.setWillNotDraw(true);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setUserAgentString(Anikumii.userAgent);
                        webView.loadUrl("https://m.animeflv.net");
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                super.onPageStarted(view, url, favicon);
                                String cookie = CookieManager.getInstance().getCookie(view.getUrl());
                                if (cookie != null && cookie.contains("cf_clearance")) {
                                    ((Anikumii) getApplicationContext()).setCloudFlare(cookie.substring(cookie.indexOf("cf_clearance=") + 13).split(";")[0]);
                                    emitter.onComplete();
                                    view.destroy();
                                    webView.destroy();
                                }
                            }
                        });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnComplete(() -> doWork())
                .subscribe();
    }

    private void updateDb() throws ExecutionException, IOException, InterruptedException {
        db = new DataBaseHelper(getApplicationContext());
        cursor = db.getListContents();
        String type, title, url, animeId;
        StringBuilder genres = new StringBuilder();
        Document doc;
        Elements dBepisodes;
        Element inside;
        List<String> localList = new ArrayList<>();

        displayNotification(null, null, Glide.with(getApplicationContext()).asBitmap().load(R.mipmap.ic_launcher).into(128, 128).get(), getApplicationContext().getString(R.string.app_name), (byte) 125);

        if (firstRun) {
            dBepisodes = Jsoup.parse(getApplicationContext().getAssets().open("database.anikumii"), "UTF-8", "").getElementsByTag("a");
            for (int lineCount = 0; lineCount < dBepisodes.size(); lineCount++)
                db.addData(dBepisodes.get(lineCount).getElementsByClass("Title").text(), dBepisodes.get(lineCount).getElementsByClass("Type").text(), dBepisodes.get(lineCount).attr("href"), dBepisodes.get(lineCount).getElementsByClass("Genres").text(), dBepisodes.get(lineCount).getElementsByClass("id").text());
        }

        while (cursor.moveToNext())
            if (cursor.getString(cursor.getColumnIndex("URL")) != null)
                localList.add(cursor.getString(cursor.getColumnIndex("URL")));

        mPreferences.edit().putString("lastDbUpdate", "Versión de: " + DateFormat.getDateTimeInstance().format(new Date())).apply();
        mPreferences.edit().putBoolean("firstRun", false).apply();

        for (int page = 1; page <= 4; page++) {
            doc = AnikumiiWebHelper.go(animeFLV + "browse?order=added&page=" + page, getApplicationContext()).get();
            dBepisodes = doc.getElementsByClass("Anime");

            for (byte episodesCount = 0; episodesCount < dBepisodes.size(); episodesCount++) {
                url = dBepisodes.get(episodesCount).getElementsByTag("a").attr("href");

                if (!localList.toString().contains(url)) {
                    type = dBepisodes.get(episodesCount).select("a > figure > span").text();
                    title = dBepisodes.get(episodesCount).getElementsByClass("Title").text();
                    inside = AnikumiiWebHelper.go(animeFLV + url, getApplicationContext()).get();
                    animeId = inside.select("div.Anm-Bg > img").attr("src").split("/uploads/animes/banners/")[1].replace(".jpg", "");

                    Elements genresElements = inside.getElementsByClass("Tag");
                    for (byte genresCount = 0; genresCount < genresElements.size(); genresCount++) {
                        genres.append(genresElements.get(genresCount).text());
                        if (genresCount != genresElements.size() - 1)
                            genres.append(",");
                    }
                    db.addData(title, type, url, genres.toString(), animeId);
                }
                genres.delete(0, genres.length());
            }
        }
    }

    private void displayNotification(String url, String number, Bitmap bitmap, String title, byte notificationId) {
        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "com.zeerooo.anikumii2.notif");
        boolean headsUp = mPreferences.getBoolean("headsUp", false);
        byte headsUpValue;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("com.zeerooo.anikumii2.notif", "Nuevos episodios", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(notificationChannel);
        } else {
            if (headsUp)
                headsUpValue = NotificationCompat.PRIORITY_HIGH;
            else
                headsUpValue = NotificationCompat.PRIORITY_DEFAULT;
            mBuilder.setPriority(headsUpValue);

        }
        mBuilder.setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.ic_notif);

        if (notificationId != 125) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                    .setContentText(number)
                    .setAutoCancel(true);

            Intent videoAct = new Intent(getApplicationContext(), VPlayerActivity.class);
            videoAct.putExtra("chapterUrl", url).putExtra("chapterTitle", title).putExtra("chapterNumber", number);

            mBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), notificationId, videoAct, PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            mBuilder.setProgress(0, 0, true)
                    .setContentText("Actualizando base de datos")
                    .setOngoing(true);
        }

        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}