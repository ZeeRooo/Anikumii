package com.zeerooo.anikumii.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.activities.MainActivity;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii.misc.DataBaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.HashMap;

/**
 * Created by ZeeRooo on 11/01/18
 */

public class AccountsService extends IntentService {
    private final HashMap<String, String> exceptionsHashMap = new HashMap<>();
    private NotificationManager mNotificationManager;

    public AccountsService() {
        super("AccountsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final AnikumiiSharedPreferences anikumiiSharedPreferences = new AnikumiiSharedPreferences(this);

        try {
            String malUserName = anikumiiSharedPreferences.getString("malUserName", null), url, parts[], lines[];

            if (malUserName == null)
                return;

            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            displayNotification();

            final AnikumiiConnection anikumiiConnection = new AnikumiiConnection();
            final DataBaseHelper dataBaseHelper = new DataBaseHelper(this);

            final JSONObject jsonObject = new JSONObject(anikumiiConnection.getStringResponse("GET", "https://api.jikan.moe/v3/user/" + malUserName, null));
//            JSONObject jsonObject = new JSONObject(anikumiiConnection.getStringResponse("GET", "https://api.jikan.moe/v3/user/" + malUserName, null)).getJSONArray("categories").getJSONObject(0).getJSONArray("items").getJSONObject(0);
            anikumiiSharedPreferences.edit().putString("malUserName", jsonObject.getString("username")).apply();
            anikumiiSharedPreferences.edit().putString("malUserAvatar", jsonObject.getString("image_url")).apply();

            final JSONArray jsonArray = new JSONObject(anikumiiConnection.getStringResponse("GET", "https://api.jikan.moe/v3/user/" + malUserName + "/animelist?order_by=last_updated&sort=asc", null)).getJSONArray("anime");

            Cursor cursor = null;

            int position;

            boolean emptyDatabase = dataBaseHelper.getDatabaseRows(cursor) == 0;

           lines = new AnikumiiConnection().getStringResponse("GET", "https://pastebin.com/raw/HHJJb4X6", null).split(" ;,;");

            for (short a = 0; a < lines.length; a++) {
                parts = lines[a].split(" > ");
                exceptionsHashMap.put(parts[0], parts[1]);
            }

            for (int a = 0; a < jsonArray.length(); a++) {
                if (jsonArray.getJSONObject(a).getString("rating").equals("Rx"))
                    url = "https://tiohentai.com/hentai/";
                else
                    url = "https://tioanime.com/anime/";

                if (!emptyDatabase) {
                    cursor = dataBaseHelper.getReadableDatabase().rawQuery("SELECT POSITION FROM AnimesDB WHERE TITLE LIKE ?", new String[]{jsonArray.getJSONObject(a).getString("title")});
                    cursor.moveToLast();
                    position = cursor.getInt(0);
                } else {
                    position = a;
                }

                dataBaseHelper.addData(jsonArray.getJSONObject(a).getString("title"), jsonArray.getJSONObject(a).getString("type"), url + malToTio(jsonArray.getJSONObject(a).getString("title")), jsonArray.getJSONObject(a).getString("image_url"), jsonArray.getJSONObject(a).getString("watch_start_date").replace("T", " / ").replace("null", "No especificado"), jsonArray.getJSONObject(a).getInt("watched_episodes"), position);
            }
            if (cursor != null)
                cursor.close();
            dataBaseHelper.close();

            if (intent.getBooleanExtra("startMain", false))
                startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("reloadMain", true));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mNotificationManager != null)
                mNotificationManager.cancel(874);
        }
    }

    private void displayNotification() {
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "com.zeerooo.anikumii.notifications");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel("com.zeerooo.anikumii.notifications") == null) {
                final NotificationChannel notificationChannel = new NotificationChannel("com.zeerooo.anikumii.notifications", "Sincronizar perfil MyAnimeList", NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        } else {
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        mBuilder.setContentTitle("Actualizando datos de MyAnimeList")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_notif)
                .setContentText("Por favor, espere")
                .setProgress(0, 0, true)
                .setAutoCancel(true);

        mNotificationManager.notify(874, mBuilder.build());
    }

    private String malToTio(String string) {
        string = string.toLowerCase();

        if (exceptionsHashMap.containsKey(string))
            string = exceptionsHashMap.get(string);
        else {
            String[] fraction = Normalizer.normalize(string, Normalizer.Form.NFKD).split("\u2044");
            if (fraction.length == 2)
                string = fraction[0] + fraction[1];

            string = string.replace(" ", "-").replace("*", "-")
                    .replaceAll("[^-A-Za-z0-9\\s]", "")
                    .replace("---", "-").replace("--", "-");

            string = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        }

        return string;
    }
}
