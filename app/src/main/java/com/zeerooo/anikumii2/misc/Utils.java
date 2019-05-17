package com.zeerooo.anikumii2.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String getNumberFromString(String string, String regex) {
        Matcher m = Pattern.compile(regex).matcher(string);
        while (m.find()) {
            return m.group(0);
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
}
