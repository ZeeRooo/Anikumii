package com.zeerooo.anikumii.services;

import android.app.IntentService;
import android.content.Intent;

import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ZeeRooo on 22/02/18
 */

public class MALApiService extends IntentService {
    private String cookie;
    private byte times;
    private HttpsURLConnection conn;

    public MALApiService() {
        super("MALApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra("malID", (short) 0);

        try {
            cookie = new AnikumiiSharedPreferences(this).decrypt("malCookie", "a;a;a");

            switch (intent.getByteExtra("action", (byte) 0)) {
                default: //delete
                    stuff("https://myanimelist.net/ownlist/anime/" + id + "/delete", cookie.split(";")[2]);
                    break;
                case 1: //basic
                    stuff("https://myanimelist.net/ownlist/anime/edit.json", intent.getStringExtra("params") + cookie.split(";")[2].replace("csrf_token=", ",\"csrf_token\":\"") + "\"}");
                    break;
                case 2: //advanced
                    String url;
                    if (intent.getBooleanExtra("isInList", false))
                        url = "https://myanimelist.net/ownlist/anime/" + id + "/edit";
                    else
                        url = "https://myanimelist.net/ownlist/anime/add?selected_series_id=" + id;

                    stuff(url, intent.getStringExtra("params") + "&" + cookie.split(";")[2]);
                    break;
                case 3: //refresh
                    stuff("https://myanimelist.net/", null);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stuff(String url, String params) throws IOException {
        times++;
        conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Host", "myanimelist.net");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("DNT", "1");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Cookie", cookie + "; is_logged_in=1; anime_update_advanced=1");
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("TE", "Trailers");
        DataOutputStream wr = null;

        if (params != null) {
            conn.setRequestMethod("POST");

            wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(params);
        }

        conn.connect();

        if (conn.getResponseCode() == 500 && times < 2) // El anime no esta en la lista, hay que agregarlo
            stuff("https://myanimelist.net/ownlist/anime/add.json", params);

        times = 0;
        if (wr != null) {
            wr.flush();
            wr.close();
        }
        System.out.println(conn.getResponseCode());

        conn.disconnect();
    }

}
