package com.zeerooo.anikumii2.services;

import android.app.IntentService;
import android.content.Intent;

import com.zeerooo.anikumii2.activities.MainActivity;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiWebHelper;

import org.json.JSONObject;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ZeeRooo on 11/01/18
 */

public class AccountsService extends IntentService {

    public AccountsService() {
        super("AccountsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AnikumiiSharedPreferences mPreferences = new AnikumiiSharedPreferences(this);

        try {
            if (mPreferences.getString("mal", null) != null) {
                String malUserName = mPreferences.getString("MALuserName", ".");
                HttpsURLConnection mal = (HttpsURLConnection) new URL("https://myanimelist.net/search/prefix.json?type=user&keyword=" + malUserName).openConnection();
                mal.setRequestMethod("GET");
                mal.setUseCaches(false);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mal.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    JSONObject jsonObject = new JSONObject(line).getJSONArray("categories").getJSONObject(0).getJSONArray("items").getJSONObject(0);
                    mPreferences.edit().putString("MALuserName", jsonObject.getString("name")).apply();
                    mPreferences.edit().putString("MALuserAvatar", jsonObject.getString("image_url")).apply();
                }
                bufferedReader.close();
                mal.disconnect();
            }

            Element e = AnikumiiWebHelper.go("https://animeflv.net/", this).get().selectFirst("span.fa-chevron-down");
            if (e != null) {
                mPreferences.edit().putString("UserAvatar", "https://m.animeflv.net/" + e.getElementsByTag("img").attr("src")).apply();
                mPreferences.edit().putString("userName", e.getElementsByTag("strong").text()).apply();

                if (intent.getBooleanExtra("startMain", false))
                    startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("loadHeaderInfo", true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
