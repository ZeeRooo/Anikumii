package com.zeerooo.anikumii2.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ZeeRooo on 22/02/18
 */

public class MALApiService extends IntentService {
    private HttpsURLConnection conn;
    private String cookie;

    public MALApiService() {
        super("MALApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String params = intent.getStringExtra("params");
            int malID = intent.getIntExtra("malID", (short) 0);
            cookie = new AnikumiiSharedPreferences(this).decrypt("mal", "a;a;a");

           /* if (malID == null)
                malID = Jsoup.connect("https://myanimelist.net/search/prefix.json?type=anime&keyword=" + intent.getStringExtra("animeName")).ignoreContentType(true).execute().body().split(",\"type\"")[0].replace("{\"categories\":[{\"type\":\"anime\",\"items\":[{\"id\":", "");*/

            if (!intent.getBooleanExtra("delete", false)) {
                Log.i("onHandleIntent 1", params);
                Log.i("onHandleIntent 2", cookie.split(";")[2]);


                stuff("https://myanimelist.net/ownlist/anime/add?selected_series_id=" + malID, params + cookie.split(";")[2]);
                stuff("https://myanimelist.net/ownlist/anime/" + malID + "/edit", params + cookie.split(";")[2]);
            } else {
                stuff("https://myanimelist.net/ownlist/anime/" + malID + "/delete", cookie.split(";")[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();
    }

    private void stuff(String url, String params) throws IOException {
        conn = (HttpsURLConnection) new URL(url).openConnection();
        // conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(new AnikumiiSharedPreferences(this).decrypt("mal", "").getBytes(), Base64.NO_WRAP));
        conn.setRequestProperty("Host", "myanimelist.net");
        conn.setRequestProperty("User-Agent", Anikumii.userAgent);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("DNT", "1");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Cookie", cookie.replace("&csrf_token", " csrf_token") + "; is_logged_in=1; anime_update_advanced=1");
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("TE", "Trailers");
        conn.setRequestMethod("POST");

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params);

        System.out.println("MyAnimeList: " + conn.getResponseCode());

        wr.flush();
        wr.close();
    }

}
