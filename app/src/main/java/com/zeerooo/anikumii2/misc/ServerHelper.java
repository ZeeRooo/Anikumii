package com.zeerooo.anikumii2.misc;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;

public class ServerHelper extends NanoHTTPD {

    private String url, title;

    public ServerHelper() {
        super(5050);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return NanoHTTPD.newFixedLengthResponse("<head>\n" +
                "<link rel=\"icon\" type=\"image/png\" href=\"https://i.imgur.com/5UQdpiW.png\" />\n" +
                "<title>" + title + "</title>\n" +
                "</head>\n" +
                "\n" +
                "<video height=\"500dp\" width=\"500dp\" controls autoplay><source src=\"" + url + "\" type=\"video/mp4\"></video>");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMyIp(Context context) {
        return Formatter.formatIpAddress((((WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress())) + ":5050";
    }
}
