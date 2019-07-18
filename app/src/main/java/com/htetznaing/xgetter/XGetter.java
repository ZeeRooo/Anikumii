package com.htetznaing.xgetter;

import android.util.Base64;

import com.htetznaing.xgetter.Core.Fruits;
import com.zeerooo.anikumii.misc.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *      xGetter
 *         By
 *   Khun Htetz Naing
 * Repo => https://github.com/KhunHtetzNaing/xGetter
 * Openload,Google GDrive,Google Photos,MediafireStreamango,StreamCherry,Mp4Upload,RapidVideo,SendVid,VidCloud,MegaUp,VK,Ok.Ru,Youtube,Twitter,SolidFils Stream/Download URL Finder!
 *
 */

public class XGetter {
    private final String agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.99 Safari/537.36";
    private String js = "ZnVuY3Rpb24gZ2V0T3BlbmxvYWRVUkwoZW5jcnlwdFN0cmluZywga2V5MSwga2V5MikgewogICAgdmFyIHN0cmVhbVVybCA9ICIiOwogICAgdmFyIGhleEJ5dGVBcnIgPSBbXTsKICAgIGZvciAodmFyIGkgPSAwOyBpIDwgOSAqIDg7IGkgKz0gOCkgewogICAgICAgIGhleEJ5dGVBcnIucHVzaChwYXJzZUludChlbmNyeXB0U3RyaW5nLnN1YnN0cmluZyhpLCBpICsgOCksIDE2KSk7CiAgICB9CiAgICBlbmNyeXB0U3RyaW5nID0gZW5jcnlwdFN0cmluZy5zdWJzdHJpbmcoOSAqIDgpOwogICAgdmFyIGl0ZXJhdG9yID0gMDsKICAgIGZvciAodmFyIGFyckl0ZXJhdG9yID0gMDsgaXRlcmF0b3IgPCBlbmNyeXB0U3RyaW5nLmxlbmd0aDsgYXJySXRlcmF0b3IrKykgewogICAgICAgIHZhciBtYXhIZXggPSA2NDsKICAgICAgICB2YXIgdmFsdWUgPSAwOwogICAgICAgIHZhciBjdXJySGV4ID0gMjU1OwogICAgICAgIGZvciAodmFyIGJ5dGVJdGVyYXRvciA9IDA7IGN1cnJIZXggPj0gbWF4SGV4OyBieXRlSXRlcmF0b3IgKz0gNikgewogICAgICAgICAgICBpZiAoaXRlcmF0b3IgKyAxID49IGVuY3J5cHRTdHJpbmcubGVuZ3RoKSB7CiAgICAgICAgICAgICAgICBtYXhIZXggPSAweDhGOwogICAgICAgICAgICB9CiAgICAgICAgICAgIGN1cnJIZXggPSBwYXJzZUludChlbmNyeXB0U3RyaW5nLnN1YnN0cmluZyhpdGVyYXRvciwgaXRlcmF0b3IgKyAyKSwgMTYpOwogICAgICAgICAgICB2YWx1ZSArPSAoY3VyckhleCAmIDYzKSA8PCBieXRlSXRlcmF0b3I7CiAgICAgICAgICAgIGl0ZXJhdG9yICs9IDI7CiAgICAgICAgfQogICAgICAgIHZhciBieXRlcyA9IHZhbHVlIF4gaGV4Qnl0ZUFyclthcnJJdGVyYXRvciAlIDldIF4ga2V5MSBeIGtleTI7CiAgICAgICAgdmFyIHVzZWRCeXRlcyA9IG1heEhleCAqIDIgKyAxMjc7CiAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCA0OyBpKyspIHsKICAgICAgICAgICAgdmFyIHVybENoYXIgPSBTdHJpbmcuZnJvbUNoYXJDb2RlKCgoYnl0ZXMgJiB1c2VkQnl0ZXMpID4+IDggKiBpKSAtIDEpOwogICAgICAgICAgICBpZiAodXJsQ2hhciAhPSAiJCIpIHsKICAgICAgICAgICAgICAgIHN0cmVhbVVybCArPSB1cmxDaGFyOwogICAgICAgICAgICB9CiAgICAgICAgICAgIHVzZWRCeXRlcyA9IHVzZWRCeXRlcyA8PCA4OwogICAgICAgIH0KICAgIH0KICAgIC8vY29uc29sZS5sb2coc3RyZWFtVXJsKQogICAgcmV0dXJuIHN0cmVhbVVybDsKfQp2YXIgZW5jcnlwdFN0cmluZyA9ICJIdGV0ekxvbmdTdHJpbmciOwp2YXIga2V5TnVtMSA9ICJIdGV0ektleTEiOwp2YXIga2V5TnVtMiA9ICJIdGV0ektleTIiOwp2YXIga2V5UmVzdWx0MSA9IDA7CnZhciBrZXlSZXN1bHQyID0gMDsKdmFyIG9ob3N0ID0gIkh0ZXR6SG9zdCI7Ci8vY29uc29sZS5sb2coZW5jcnlwdFN0cmluZywga2V5TnVtMSwga2V5TnVtMik7CnRyeSB7CiAgICB2YXIga2V5TnVtMV9PY3QgPSBwYXJzZUludChrZXlOdW0xLm1hdGNoKC9wYXJzZUludFwoJyguKiknLDhcKS8pWzFdLCA4KTsKICAgIHZhciBrZXlOdW0xX1N1YiA9IHBhcnNlSW50KGtleU51bTEubWF0Y2goL1wpXC0oW15cK10qKVwrLylbMV0pOwogICAgdmFyIGtleU51bTFfRGl2ID0gcGFyc2VJbnQoa2V5TnVtMS5tYXRjaCgvXC9cKChbXlwtXSopXC0vKVsxXSk7CiAgICB2YXIga2V5TnVtMV9TdWIyID0gcGFyc2VJbnQoa2V5TnVtMS5tYXRjaCgvXCsweDRcLShbXlwpXSopXCkvKVsxXSk7CiAgICBrZXlSZXN1bHQxID0gKGtleU51bTFfT2N0IC0ga2V5TnVtMV9TdWIgKyA0IC0ga2V5TnVtMV9TdWIyKSAvIChrZXlOdW0xX0RpdiAtIDgpOwogICAgdmFyIGtleU51bTJfT2N0ID0gcGFyc2VJbnQoa2V5TnVtMi5tYXRjaCgvXCgnKFteJ10qKScsLylbMV0sIDgpOwogICAgdmFyIGtleU51bTJfU3ViID0gcGFyc2VJbnQoa2V5TnVtMi5zdWJzdHIoa2V5TnVtMi5pbmRleE9mKCIpLSIpICsgMikpOwogICAga2V5UmVzdWx0MiA9IGtleU51bTJfT2N0IC0ga2V5TnVtMl9TdWI7CiAgICBjb25zb2xlLmxvZyhrZXlOdW0xLCBrZXlOdW0yKTsKfSBjYXRjaCAoZSkgewogICAgLy9jb25zb2xlLmVycm9yKGUuc3RhY2spOwogICAgdGhyb3cgRXJyb3IoIktleSBOdW1iZXJzIG5vdCBwYXJzZWQhIik7Cn0KdmFyIHNyYyA9IG9ob3N0ICsgJy9zdHJlYW0vJyArIGdldE9wZW5sb2FkVVJMKGVuY3J5cHRTdHJpbmcsIGtleVJlc3VsdDEsIGtleVJlc3VsdDIpOwp4R2V0dGVyLmZ1Y2soc3JjKTs=";

  /*  private final String openload = "https?:\\/\\/(www\\.)?(openload|oload)\\.[^\\/,^\\.]{2,}\\/(embed|f)\\/.+";
    private final String fruits = "https?:\\/\\/(www\\.)?(streamango|fruitstreams|streamcherry|fruitadblock|fruithosts)\\.[^\\/,^\\.]{2,}\\/(f|embed)\\/.+";
    private final String megaup = "https?:\\/\\/(www\\.)?(megaup)\\.[^\\/,^\\.]{2,}\\/.+";
    private final String rapidvideo = "https?:\\/\\/(www\\.)?rapidvideo\\.[^\\/,^\\.]{2,}\\/(\\?v=[^&\\?]*|e\\/.+|v\\/.+)";
    private final String okru = "https?:\\/\\/(www.|m.)?(ok)\\.[^\\/,^\\.]{2,}\\/(video|videoembed)\\/.+";*/

    /*public void find(String url, OnTaskCompleted onComplete) {
        this.onComplete = onComplete;
        boolean run = false, oload = false, isOkRu = false, isRapidVideo = false, fruit = false, mfire = false;
        if (url.contains("openload") || url.contains("oload")) {
            //Openload
            run = true;
            oload = true;
        } else if (url.contains("streamango") || url.contains("fruitstreams") || url.contains("streamcherry") || url.contains("fruitadblock") || url.contains("fruithosts")) {
            //Fruits
            fruit = true;
            run = true;
        } else if (url.contains("rapidvideo.")) {
            //rapidvideo
            run = true;
            isRapidVideo = true;
            if (url.contains("/e/")) {
                url = url.replace("/e/", "/v/");
            }
        } else if (url.contains("ok.")) {
            run = true;
            isOkRu = true;
            if (!url.startsWith("https")) {
                url = url.replace("http", "https");
            }

            if (url.contains("m.")) {
                url = url.replace("m.", "");
            }

            if (url.contains("/video/")) {
                url = url.replace("/video/", "/videoembed/");
            }

        }

        if (run) {
            if (isOkRu) {
                okru(url);
            } else if (isRapidVideo) {
                rapidVideo(url);
            }
        }
    }*/

    public String fruits(String url) throws IOException {
        return Fruits.fetch(url);
    }

    public String mediafire(String url) throws IOException {
        final String regex = "aria-label=\"Download file\" href=\"(.*)\"";
        final Pattern pattern = Pattern.compile(regex);
        String html = Jsoup.connect(url).userAgent(agent).get().body().toString();
        final Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String zippyshare(String url) throws IOException {
        String es = Jsoup.connect(url).get().selectFirst("script:containsData(document.getElementById('dlbutton'))").toString();
        try {
            String[] parts = es.replace(" ", "")
                    .split("\\+\\(")[1].split("\\)\\+")[0].split("\\+");
            int a = Integer.parseInt(parts[0].split("%")[0]);
            int b = Integer.parseInt(parts[0].split("%")[1]);
            int c = Integer.parseInt(parts[1].split("%")[0]);
            int d = Integer.parseInt(parts[1].split("%")[1]);
            return url.substring(0, url.indexOf(".")) + ".zippyshare.com" + es.substring(es.indexOf("/d/")).split("\";")[0].replaceAll("\".*?\"", Integer.toString((a % b) + (c % d)));
        } catch (Exception e) {
            int a = Integer.parseInt(es.split("= ")[1].split(";")[0]);
            int b = Integer.valueOf(es.split("= ")[2].split(";")[0]);
            int c = (int) Math.floor(a / 3);
            return url.substring(0, url.indexOf(".")) + ".zippyshare.com" + es.substring(es.indexOf("/d/")).split("\";")[0].replaceAll("\".*?\"", String.valueOf(c + (a % b)));
        }
    }

    private void openload(final String url) throws IOException {
        String html = Jsoup.connect(url).userAgent(agent).get().body().toString();
        String longString = getLongEncrypt(html);
        if (longString == null) {
            longString = getLongEncrypt2(html);
        }
        String key1 = getKey1(html);
        String key2 = getKey2(html);
        js = base64Decode(js);
        js = js.replace("HtetzLongString", longString);
        js = js.replace("HtetzKey1", key1);
        js = js.replace("HtetzKey2", key2);
        js = js.replace("HtetzHost", "https://oladblock.me");
        js = base64Encode(js);
    }

    public String okru(String url) {
        try {
            String html = Jsoup.connect(url).userAgent("Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19").get().getElementsByClass("vid-card_cnt h-mod").attr("data-options");
            String json = new JSONObject(html.replace("&quot;", "\"")).getJSONObject("flashvars").getString("metadata");
            JSONArray jsonArray = new JSONObject(json).getJSONArray("videos");

            for (int i = 0; i < jsonArray.length(); i++) {
                url = jsonArray.getJSONObject(i).getString("url");
            }
        } catch (IOException | JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return url;
    }

    public String rapidVideo(String mUrl) throws IOException {
        if (mUrl.contains("&q="))
            mUrl = mUrl.replaceAll("=(.*)p", "=720p");

        String line;

        URL url = new URL(mUrl);
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        while ((line = br.readLine()) != null) {
            result.append(line);
        }

        is.close();
        br.close();

       /* final String regex = "<source src=\"(.*?)\"";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(result.toString());
        if (matcher.find()) {
            return matcher.group(1);
        }*/
        return Utils.matcher(result.toString(), "<source src=\"(.*?)\"");
    }

    private String getLongEncrypt(String string) {
        final String regex = "<p id=[^>]*>([^<]*)<\\/p>";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getLongEncrypt2(String string) {
        final String regex = "<p style=\"\" id=[^>]*>([^<]*)<\\/p>";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getKey1(String string) {
        final String regex = "\\_0x45ae41\\[\\_0x5949\\('0xf'\\)\\]\\(_0x30725e,(.*)\\),\\_1x4bfb36";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getKey2(String string) {
        final String regex = "\\_1x4bfb36=(.*);";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    private String base64Encode(String text) throws UnsupportedEncodingException {
        return Base64.encodeToString(text.getBytes("UTF-8"), Base64.DEFAULT);
    }

    private String base64Decode(String text) throws UnsupportedEncodingException {
        return new String(Base64.decode(text, Base64.DEFAULT), "UTF-8");
    }
}
