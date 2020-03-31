package com.zeerooo.anikumii.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zeerooo.anikumii.BuildConfig;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.UpdateActivity;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UpdateService extends Worker {
    private final boolean triggered;
    private Intent updaterActivity;

    public UpdateService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        triggered = workerParams.getInputData().getBoolean("triggered", false);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            final JSONObject jsonObject = new JSONObject(new AnikumiiConnection().getStringResponse("GET", "https://api.github.com/repos/ZeeRooo/Anikumii-releases/releases/latest", null));

            final String tagName = jsonObject.getString("tag_name");

            if (tagName.contains(BuildConfig.VERSION_NAME)) {
                if (triggered)
                    displayNotification("Última versión instalada de Anikumii!!", BuildConfig.VERSION_NAME);

                return Result.success();
            } else {
                updaterActivity = new Intent(getApplicationContext(), UpdateActivity.class)
                        .putExtra("downloadUrl", jsonObject.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"))
                        .putExtra("publishDate", jsonObject.getString("published_at").replace("T", " ").replace("Z", ""))
                        .putExtra("changelog", jsonObject.getString("body"))
                        .putExtra("tagName", tagName);

                displayNotification("Actualización disponible de Anikumii!!", "Toca para actualizar a " + tagName);
            }

            return Result.success();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void displayNotification(String title, String subtitle) {
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "com.zeerooo.anikumii.notifications");
        final NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel("com.zeerooo.anikumii.notifications") == null) {
                final NotificationChannel notificationChannel = new NotificationChannel("com.zeerooo.anikumii.notifications", "Actualizaciones", NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        } else {
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        mBuilder.setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_notif)
                .setContentText(subtitle)
                .setAutoCancel(true);

        if (updaterActivity != null) {
            mBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 123, updaterActivity, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        mNotificationManager.notify(123, mBuilder.build());
    }
}
