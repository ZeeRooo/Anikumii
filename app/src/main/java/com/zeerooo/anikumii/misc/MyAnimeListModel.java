package com.zeerooo.anikumii.misc;

import android.content.Context;
import android.content.Intent;

import com.zeerooo.anikumii.anikumiiparts.AnikumiiConnection;
import com.zeerooo.anikumii.services.MALApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyAnimeListModel {
    private int id, seenEpisodes, totalEpisodes;
    private byte score, priority, rewatched, rewatchValue, storageType, status;
    private boolean isInList;
    private String comment;
    private String endYear;
    private String startYear;
    private String[] strings = new String[0];
    private final StringBuilder params = new StringBuilder();
    private final List<String> tagsList = new ArrayList<>();
    private JSONObject jsonObject;

    public MyAnimeListModel(String name, String username, boolean fetchFullUserData) throws IOException {

        try {
            jsonObject = new JSONObject(new AnikumiiConnection().getStringResponse("GET", "https://api.jikan.moe/v3/user/" + username + "/animelist?q=" + name, null)).getJSONArray("anime").getJSONObject(0);
            id = jsonObject.getInt("mal_id");
            score = (byte) jsonObject.getInt("score");
            status = (byte) jsonObject.getInt("watching_status");
            totalEpisodes = jsonObject.getInt("total_episodes");

            if (fetchFullUserData)
                fetchUserData();
        } catch (JSONException | FileNotFoundException e) {
            //e.printStackTrace();
            try {
                jsonObject = new JSONObject(new AnikumiiConnection().getStringResponse("GET", "https://api.jikan.moe/v3/search/anime/?q=" + name, null)).getJSONArray("results").getJSONObject(0);
                id = jsonObject.getInt("mal_id");
                totalEpisodes = jsonObject.getInt("episodes");
            } catch (Exception c) {
                //c.printStackTrace();
            }

            isInList = false;
            status = 1;
        }
    }

    private void fetchUserData() throws JSONException {
        isInList = true;
        priority = priorityToByte(jsonObject.getString("priority"));
        storageType = storageToByte(jsonObject.getString("storage"));

        String s = jsonObject.getString("tags");
        if (s == null)
            strings = new String[0];
        else
            strings = jsonObject.getString("tags").replace("null", "").split(",");

        seenEpisodes = jsonObject.getInt("watched_episodes");

        startYear = jsonObject.getString("watch_start_date").split("T")[0];
        endYear = jsonObject.getString("watch_end_date").split("T")[0];
    }

    public String getType() throws JSONException {
        return jsonObject.getString("type");
    }

    public String getImage() throws JSONException {
        return jsonObject.getString("image_url");
    }

    public String[] getStrings() {
        return strings;
    }

    private byte storageToByte(String storage) {
        byte result;
        if (storage.contains("HD")) {
            result = 0;
        } else if (storage.contains("NAS")) {
            result = 2;
        } else if (storage.contains("VHS")) {
            result = 5;
        } else if (storage.contains("DVD")) {
            result = 4;
        } else if (storage.contains("Blu-ray")) {
            result = 3;
        } else if (storage.contains("EHD")) {
            result = 1;
        } else {
            result = 6;
        }

        if (this.storageType != result)
            return result;
        else
            return this.priority;
    }

    private byte priorityToByte(String priority) {
        byte result;
        switch (priority) {
            case "Low":
                result = 0;
                break;
            case "Medium":
                result = 1;
                break;
            default:
                result = 2;
                break;
        }

        if (this.priority != result)
            return result;
        else
            return this.priority;
    }

    public void setRewatched(byte rewatched) {
        this.rewatched = rewatched;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRewatchValue(byte rewatchValue) {
        this.rewatchValue = rewatchValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeenEpisodes() {
        return seenEpisodes;
    }

    public void setSeenEpisodes(int seenEpisodes) {
        this.seenEpisodes = seenEpisodes;
        if (totalEpisodes == seenEpisodes)
            status = 2;
    }

    public byte getScore() {
        return score;
    }

    public void setScore(byte score) {
        if (this.score != score)
            this.score = score;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public byte getStorageType() {
        return storageType;
    }

    public void setStorageType(byte storageType) {
        if (this.storageType != storageType)
            this.storageType = storageType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tagsList;
    }

    public void setTags(List<String> tags) {
        for (short a = 0; a < tags.size(); a++) {
            if (!tagsList.contains(tags.get(a))) {
                tagsList.add(tags.get(a));
            }
        }
    }

    public String getEndYear() {
        return endYear;
    }

    public void setEndYear(String endYear) {
        if (this.endYear != endYear)
            this.endYear = endYear;
        params
                .append("&add_anime%5Bfinish_date%5D%5Bmonth%5D=").append(endYear.split("-")[1])
                .append("&add_anime%5Bfinish_date%5D%5Bday%5D=").append(endYear.split("-")[2])
                .append("&add_anime%5Bfinish_date%5D%5Byear%5D=").append(endYear.split("-")[0]);
    }

    public String getStartYear() {
        return startYear;
    }

    public void setStartYear(String startYear) {
        if (this.startYear != startYear)
            this.startYear = startYear;
        params
                .append("&add_anime%5Bstart_date%5D%5Bmonth%5D=").append(startYear.split("-")[1])
                .append("&add_anime%5Bstart_date%5D%5Bday%5D=").append(startYear.split("-")[2])
                .append("&add_anime%5Bstart_date%5D%5Byear%5D=").append(startYear.split("-")[0]);
    }

    private String getAdvancedParams() {
        try {
            if (score == 0)
                params.append("&add_anime%5Bscore%5D=");
            else
                params.append("&add_anime%5Bscore%5D=").append(score);

            return params
                    .append("&anime_id=").append(id)
                    .append("&aeps=").append(seenEpisodes)
                    .append("&astatus=").append(status)
                    .append("&add_anime%5Bstatus%5D=").append(status)
                    .append("&add_anime%5Bnum_watched_episodes%5D=").append(seenEpisodes)
                    // .append("&add_anime%5Bscore%5D=").append(score)
                    .append("&add_anime%5Bpriority%5D=").append(priority)
                    .append("&add_anime%5Bstorage_type%5D=").append(storageType)
                    .append("&add_anime%5Bstorage_value%5D=0")
                    .append("&add_anime%5Bnum_watched_times%5D=").append(rewatched)
                    .append("&add_anime%5Brewatch_value%5D=").append(rewatchValue)
                    .append("&add_anime%5Bcomments%5D=").append(comment)
                    .append("&add_anime%5Bis_asked_to_discuss%5D=1")
                    .append("&add_anime%5Bsns_post_type%5D=1")
                    .append("&submitIt=0")
                    .append("&add_anime%5Btags%5D=").append(tagsList.toString().replace("[", "").replace("]", ""))
                    .toString();
        } finally {
            params.delete(0, params.length());
            tagsList.clear();
        }
    }

    private String getBasicParams() {
        try {
            return params
                    .append("{\"anime_id\":").append(id)
                    .append(",\"status\":").append(status)
                    .append(",\"score\":").append(score)
                    .append(",\"num_watched_episodes\":").append(seenEpisodes)
                    .toString();
        } finally {
            params.delete(0, params.length());
        }
    }

    public void apiHandler(Context context, byte action) {
        String params;
        switch (action) {
            case 1:
                params = getBasicParams();
                break;
            case 2:
                params = getAdvancedParams();
                break;
            default:
                params = null;
                break;
        }
        context.startService(new Intent(context, MALApiService.class).putExtra("params", params).putExtra("malID", id).putExtra("action", action).putExtra("isInList", isInList));
        isInList = true;
    }
}
