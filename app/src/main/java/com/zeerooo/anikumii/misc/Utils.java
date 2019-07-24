package com.zeerooo.anikumii.misc;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.ImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String matcher(String string, String regex) {
        Matcher m = Pattern.compile(regex).matcher(string);
        while (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static String getTypeFromNumber(String number) {
        switch (number) {
            default: // 0
                return "Anime";
            case "1":
                return "Película";
            case "2":
                return "OVA";
            case "3":
                return "Especial";
        }
    }

    public static String encodeString(String url) {
        return url.replace("<", "%3C").replace(">", "%3E").replace("#", "%23").replace("%", "%25")
                .replace("{", "%7B").replace("}", "%7D").replace("|", "%7C").replace("\\", "%5C")
                .replace("^", "%5E").replace("~", "%7E").replace("[", "%5B").replace("]", "%5D")
                .replace("`", "%60").replace(";", "%3B").replace("/", "%2F").replace("?", "%3F")
                .replace(":", "%3A").replace("@", "%40").replace("=", "%3D").replace("&", "%26")
                .replace("$", "%24").replace("+", "%2B").replace(",", "%2C").replace(" ", "%20");
    }

    public static String removeLastNumberAndSpace(String string) {
        return string.replaceAll("( \\d+)\\D*$", "");
    }

    public static SpannableStringBuilder getBold(String string, ForegroundColorSpan foregroundColorSpan, StyleSpan styleSpan) {
        if (string == null)
            string = "";
        SpannableStringBuilder sb = new SpannableStringBuilder(string.replace("null", "-"));
        sb.setSpan(foregroundColorSpan, 0, string.indexOf(":") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(styleSpan, 0, string.indexOf(":") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    public static void setColorFilter(Drawable drawable, int color) {
        if (Build.VERSION.SDK_INT >= 29)
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        else
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setColorFilter(ImageView imageView, int color) {
        if (Build.VERSION.SDK_INT >= 29)
            imageView.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        else
            imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}
