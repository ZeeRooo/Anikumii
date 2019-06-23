package com.zeerooo.anikumii.anikumiiparts;

import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;

public class AnikumiiWebHelper {
    // private static Map<String, String> userCookiesHashMap = new HashMap<>();

    public static Connection go(String url, Context context) throws IOException {
        return connection(url)/*.cookies(getUserCookiesHashMap(((Anikumii) context.getApplicationContext()).getUserCookie(), ((Anikumii) context.getApplicationContext()).getCloudFlare()))*/;
    }

    private static Connection connection(String url) {
        return HttpConnection.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0")/*.cookies(userCookiesHashMap)*/;
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

