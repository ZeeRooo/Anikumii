package com.zeerooo.anikumii2.anikumiiparts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by ZeeRooo on 28/01/18
 */

public class AnimeRatingView extends ProgressBar {
    private String number = "0.0";
    private short aShort;
    private Paint paint;
    private Rect rect;
    private byte xPos, yPos;

    public AnimeRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        rect = new Rect();
    }

    public void setAnimatesProgress() {
        ObjectAnimator
                .ofInt(this, "Progress", 0, aShort)
                .setDuration(1000)
                .start();
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        paint.setTextSize(getWidth() / 3);
        xPos = (byte) (getWidth() / 2 - rect.centerX());
        yPos = (byte) ((getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        super.onLayout(changed, left, top, right, bottom);
    }

    public void init(String number) {
        this.number = number;

        aShort = Short.valueOf(number.replace(".", ""));

        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        int color;
        if (aShort < 11)
            color = Color.parseColor("#e32228");
        else if (aShort < 21)
            color = Color.parseColor("#f27127");
        else if (aShort < 31)
            color = Color.parseColor("#f7cb19");
        else if (aShort < 41)
            color = Color.parseColor("#73b045");
        else
            color = Color.parseColor("#03804e");

        GradientDrawable gradientDrawable = (GradientDrawable) getProgressDrawable();
        gradientDrawable.setColor(color);
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(number, xPos, yPos, paint);
    }
}