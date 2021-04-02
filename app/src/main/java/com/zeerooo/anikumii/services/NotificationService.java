package com.zeerooo.anikumii.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.VideoPlayerActivity;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiWebHelper;
import com.zeerooo.anikumii.misc.Utils;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by ZeeRooo on 23/01/18
 */

public class NotificationService extends Worker {
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    public NotificationService(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        final AnikumiiSharedPreferences anikumiiSharedPreferences = new AnikumiiSharedPreferences(getApplicationContext());

        int headsUpValue;
        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), "com.zeerooo.anikumii.episodes.notifications");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel("com.zeerooo.anikumii.episodes.notifications") == null) {
                final NotificationChannel notificationChannel = new NotificationChannel("com.zeerooo.anikumii.episodes.notifications", "Nuevos episodios", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        } else {
            if (anikumiiSharedPreferences.getBoolean("headsUp", false))
                headsUpValue = NotificationCompat.PRIORITY_HIGH;
            else
                headsUpValue = NotificationCompat.PRIORITY_DEFAULT;
            mBuilder.setPriority(headsUpValue);
        }

        try {
            if (anikumiiSharedPreferences.getBoolean("enable_tioanime_notifications", false))
                syncAnimes("https://tioanime.com", anikumiiSharedPreferences, "tioanime");

            if (anikumiiSharedPreferences.getBoolean("enable_tiohentai_notifications", false))
                syncAnimes("https://tiohentai.com", anikumiiSharedPreferences, "tiohentai");
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }

    private void syncAnimes(String url, AnikumiiSharedPreferences anikumiiSharedPreferences, String knownKey) throws ExecutionException, InterruptedException, IOException {
        String knownAnimesStr = anikumiiSharedPreferences.getString(knownKey, ""), title, number, image, animeUrl;

        final Elements episodes = AnikumiiWebHelper.go(url, getApplicationContext()).get().select("article.episode");
        final StringBuilder knownAnimes = new StringBuilder();

        if (anikumiiSharedPreferences.getBoolean("enableNotif", false)) {

            for (byte episodesCount = 0; episodesCount < 5; episodesCount++) {
                title = episodes.get(episodesCount).getElementsByClass("title").text();
                number = Utils.matcher(title, "(\\d+)\\D*$");
                if (number == null)
                    number = "";
                title = title.replace(number, "");

                if (!knownAnimesStr.contains(title + number)) {
                    animeUrl = url + episodes.get(episodesCount).select("a").attr("href");
                    image = url + episodes.get(episodesCount).selectFirst("a > div > figure > img").attr("src");

                    displayNotification(animeUrl, "Episodio " + number, Glide.with(getApplicationContext()).asBitmap().load(image).apply(RequestOptions.circleCropTransform()).into(128, 128).get(), title, (byte) System.currentTimeMillis());
                }

                knownAnimes.append(title).append(number);
            }

            anikumiiSharedPreferences.edit().putString(knownKey, knownAnimes.toString()).apply();
        }
    }

    private void displayNotification(String url, String number, Bitmap bitmap, String title, byte notificationId) {
        mBuilder.setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(bitmap)
                .setSmallIcon(R.mipmap.ic_notif)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                .setContentText(number)
                .setAutoCancel(true);

        final Intent videoAct = new Intent(getApplicationContext(), VideoPlayerActivity.class);
        videoAct.putExtra("chapterUrl", url);

        mBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), notificationId, videoAct, PendingIntent.FLAG_UPDATE_CURRENT));

        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}