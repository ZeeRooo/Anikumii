package com.zeerooo.anikumii2;

import android.app.Application;
import android.content.Context;

import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;

/**
 * Created by ZeeRooo on 04/04/18
 */

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "chavesjuan400@gmail.com")
public class Anikumii extends Application {
    private String userCookie, cloudFlare = "";
    private AnikumiiSharedPreferences anikumiiSharedPreferences;
    public static String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

     //   ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        anikumiiSharedPreferences = new AnikumiiSharedPreferences(this);

        userCookie = anikumiiSharedPreferences.decrypt("UserCookie", "f;f;f");
    }

    public String getUserCookie() {
        return userCookie;
    }

    public void setUserCookie(String userCookie) {
        this.userCookie = userCookie;
        anikumiiSharedPreferences.encrypt("UserCookie", userCookie);
    }

    public String getCloudFlare() {
        return cloudFlare;
    }

    public void setCloudFlare(String cloudFlare) {
        this.cloudFlare = cloudFlare;
    }
}
