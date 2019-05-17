package com.htetznaing.xgetter.Core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class Fruits {
    public static String fetch(String url) throws IOException {
        URL obj = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
        InputStream is = conn.getInputStream();
        int ptr = 0;
        StringBuilder response = new StringBuilder();
        while ((ptr = is.read()) != -1) {
            response.append((char) ptr);
        }

        is.close();

        String start = getStart(response.toString());
        String end = getEnd(response.toString());

        if (null != start && null != end) {
            return "https:" + getStreamURL(start, Integer.parseInt(end));
        }
        return null;
    }

    private static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    private static String getStreamURL(String hashCode, int intVal) {
        String chars = "=/+9876543210zyxwvutsrqponmlkjihgfedcbaZYXWVUTSRQPONMLKJIHGFEDCBA";
        StringBuilder retVal = new StringBuilder();
        int hashCharCode_0, hashCharCode_1, hashCharCode_2, hashCharCode_3;
        hashCode = hashCode.replace("[^A-Za-z0-9\\+\\/\\=]", "");
        for (int hashIndex = 0; hashIndex < hashCode.length(); hashIndex += 4) {
            hashCharCode_0 = chars.indexOf(hashCode.charAt(hashIndex));
            hashCharCode_1 = chars.indexOf(hashCode.charAt(hashIndex + 1));
            hashCharCode_2 = chars.indexOf(hashCode.charAt(hashIndex + 2));
            hashCharCode_3 = chars.indexOf(hashCode.charAt(hashIndex + 3));
            retVal.append(fromCharCode((((hashCharCode_0 << 0x2) | (hashCharCode_1 >> 0x4)) ^ intVal)));
            if (hashCharCode_2 != 0x40) {
                retVal.append(fromCharCode(((hashCharCode_1 & 0xf) << 0x4) | (hashCharCode_2 >> 0x2)));
            }
            if (hashCharCode_3 != 0x40) {
                retVal.append(fromCharCode(((hashCharCode_2 & 0x3) << 0x6) | hashCharCode_3));
            }
        }
        return retVal.toString();
    }


    private static String getStart(String string) {
        final String regex = "src:d\\('([^']*)',([^\\)]*)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String getEnd(String string) {
        final String regex = "src:d\\('([^']*)',([^\\)]*)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }
}
