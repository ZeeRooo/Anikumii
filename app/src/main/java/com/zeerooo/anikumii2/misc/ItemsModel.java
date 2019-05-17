package com.zeerooo.anikumii2.misc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class ItemsModel implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ItemsModel createFromParcel(Parcel in) {
            return new ItemsModel(in);
        }

        public ItemsModel[] newArray(int size) {
            return new ItemsModel[size];
        }
    };
    private final String title, number, img_url, chapterUrl;
    // private boolean seen;
    private int textColor;

    public ItemsModel(String title, String number, String img_url, String chapterUrl, int textColor) {
        this.title = title;
        this.number = number;
        this.img_url = img_url;
        this.chapterUrl = chapterUrl;
        this.textColor = textColor;
    }

    public ItemsModel(String title, String number, String img_url, String chapterUrl) {
        this.title = title;
        this.number = number;
        this.img_url = img_url;
        this.chapterUrl = chapterUrl;
    }

    private ItemsModel(Parcel in) {
        String[] stringData = new String[4];
        //boolean[] booleanData = new boolean[1];
        int[] intData = new int[1];

        in.readStringArray(stringData);
        //in.readBooleanArray(color);
        in.readIntArray(intData);
        // the order needs to be the same as in writeToParcel() method
        title = stringData[0];
        number = stringData[1];
        img_url = stringData[2];
        chapterUrl = stringData[3];
        // this.seen = booleanData[0];
        textColor = intData[0];
    }

    public String getTitle() {
        return title;
    }

    public String getNumber() {
        return number;
    }

    public String getImg_url() {
        return img_url;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public int getTextColor() {
        return textColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.title, this.number, this.img_url, this.chapterUrl});
        // dest.writeBooleanArray(new boolean[]{this.seen});
        dest.writeIntArray(new int[]{this.textColor});
    }
}
