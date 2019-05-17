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
    private String number;
    private Paint paint;
    private Rect rect;
    private byte xPos, yPos;

    public AnimeRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAnimatesProgress() {
        ObjectAnimator
                .ofInt(this, "Progress", 0, Integer.valueOf(number.replace(".", "")))
                .setDuration(1000)
                .start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        paint.setTextSize(getWidth() / 3);
        xPos = (byte) (getWidth() / 2 - rect.centerX());
        yPos = (byte) ((getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    public void init(String number) {
        this.number = number;

        paint = new Paint();
        rect = new Rect();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        int color;
        switch ((byte) Double.parseDouble(number)) {
            case 1:
                color = Color.parseColor("#e32228");
                break;
            case 2:
                color = Color.parseColor("#f27127");
                break;
            case 3:
                color = Color.parseColor("#f7cb19");
                break;
            case 4:
                color = Color.parseColor("#73b045");
                break;
            default:
                color = Color.parseColor("#03804e");
                break;
        }

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
