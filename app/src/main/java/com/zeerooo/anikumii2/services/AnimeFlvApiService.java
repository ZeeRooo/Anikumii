package com.zeerooo.anikumii2.services;

import android.app.IntentService;
import android.content.Intent;

import com.zeerooo.anikumii2.Anikumii;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ZeeRooo on 18/2/2018
 */

public class AnimeFlvApiService extends IntentService {

    public AnimeFlvApiService() {
        super("AnimeFlvApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(intent.getStringExtra("toLoad")).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cookie", "PHPSESSID=" + ((Anikumii) getApplicationContext()).getUserCookie().split(";")[0] + "; __cfduid=" + ((Anikumii) getApplicationContext()).getUserCookie().split(";")[2] + "; cf_clearance=" + ((Anikumii) getApplicationContext()).getCloudFlare());
            conn.setRequestProperty("User-Agent", Anikumii.userAgent);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(intent.getStringExtra("params"));

            conn.getResponseCode();

            wr.flush();
            wr.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
