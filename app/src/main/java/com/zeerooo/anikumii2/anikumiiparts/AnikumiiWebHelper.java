package com.zeerooo.anikumii2.anikumiiparts;

import android.content.Context;

import com.zeerooo.anikumii2.Anikumii;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;

public class AnikumiiWebHelper {
   // private static Map<String, String> userCookiesHashMap = new HashMap<>();

    public static Connection go(String url, Context context) throws IOException {
        return connection(url)/*.cookies(getUserCookiesHashMap(((Anikumii) context.getApplicationContext()).getUserCookie(), ((Anikumii) context.getApplicationContext()).getCloudFlare()))*/;
    }

    private static Connection connection(String url) {
        return HttpConnection.connect(url).userAgent(Anikumii.userAgent)/*.cookies(userCookiesHashMap)*/;
    }

  /*  private static Map<String, String> getUserCookiesHashMap(String userCookie, String cloudFlareCookie) throws IOException {
        userCookiesHashMap.put("PHPSESSID", userCookie.split(";")[0]);
        userCookiesHashMap.put("login", userCookie.split(";")[1]);
        userCookiesHashMap.put("__cfduid", userCookie.split(";")[2]);
        userCookiesHashMap.put("cf_clearance", cloudFlareCookie);

        userCookiesHashMap.putAll(connection("https://m.animeflv.net/").cookies(userCookiesHashMap).execute().cookies());

        return userCookiesHashMap;
    }*/
}

