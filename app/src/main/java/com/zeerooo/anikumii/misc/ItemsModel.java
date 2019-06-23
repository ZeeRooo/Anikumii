package com.zeerooo.anikumii.misc;

/**
 * Created by ZeeRooo on 05/01/18
 */

public class ItemsModel {

    private String title, number, imgUrl, chapterUrl, date;
    private int textColor;

    public ItemsModel(String title, String number, String imgUrl, String chapterUrl) {
        this.title = title;
        this.number = number;
        this.imgUrl = imgUrl;
        this.chapterUrl = chapterUrl;
    }

    public ItemsModel(String title, String number, String imgUrl, String chapterUrl, int textColor) {
        this.title = title;
        this.number = number;
        this.imgUrl = imgUrl;
        this.chapterUrl = chapterUrl;
        this.textColor = textColor;
    }

    public ItemsModel(String title, String number, String imgUrl, String chapterUrl, String date) {
        this.title = title;
        this.number = number;
        this.imgUrl = imgUrl;
        this.chapterUrl = chapterUrl;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getNumber() {
        return number;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public String getDate() {
        return date;
    }

    public int getTextColor() {
        return textColor;
    }
}
