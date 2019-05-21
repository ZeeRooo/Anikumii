package com.zeerooo.anikumii2.services;

import android.app.IntentService;
import android.content.Intent;

import com.zeerooo.anikumii2.activities.MainActivity;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;

import org.json.JSONObject;

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
        AnikumiiConnection anikumiiConnection = new AnikumiiConnection();

        try {
            String malUserName = mPreferences.getString("MALuserName", ".");
            JSONObject jsonObject = new JSONObject(anikumiiConnection.getStringResponse("GET", "https://myanimelist.net/search/prefix.json?type=user&keyword=" + malUserName, null)).getJSONArray("categories").getJSONObject(0).getJSONArray("items").getJSONObject(0);
            mPreferences.edit().putString("MALuserName", jsonObject.getString("name")).apply();
            mPreferences.edit().putString("MALuserAvatar", jsonObject.getString("image_url")).apply();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (intent.getBooleanExtra("startMain", false))
                startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("loadHeaderInfo", true));
        }
    }
}
