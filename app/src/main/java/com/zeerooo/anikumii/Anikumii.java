package com.zeerooo.anikumii;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by ZeeRooo on 04/04/18
 */

@ReportsCrashes(mailTo = "chavesjuan400@gmail.com")
public class Anikumii extends Application {

    public static String dominium;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }

}
