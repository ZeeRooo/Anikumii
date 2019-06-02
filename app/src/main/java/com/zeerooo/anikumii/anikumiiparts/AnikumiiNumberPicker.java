package com.zeerooo.anikumii.anikumiiparts;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import static android.text.InputType.TYPE_CLASS_NUMBER;

public class AnikumiiNumberPicker extends LinearLayout {
    private short maxEpisodes;
    private Paint paint = new Paint();

    private AppCompatEditText appCompatEditText;

    public AnikumiiNumberPicker(Context context) {
        super(context);
        init();
    }

    public AnikumiiNumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnikumiiNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        setOrientation(HORIZONTAL);
        Button decreaseBtn = new Button(getContext());
        AnikumiiUiHelper.transparentBackground(decreaseBtn);
        decreaseBtn.setText("-");
        decreaseBtn.setOnClickListener(v -> appCompatEditText.setText(String.valueOf(Short.parseShort(appCompatEditText.getText().toString()) - 1)));
        addView(decreaseBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        appCompatEditText = new AppCompatEditText(getContext());
        appCompatEditText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        appCompatEditText.setInputType(TYPE_CLASS_NUMBER);

        appCompatEditText.setFilters(new InputFilter[]{new InputFilterMinMax()});
        addView(appCompatEditText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        Button increaseBtn = new Button(getContext());
        AnikumiiUiHelper.transparentBackground(increaseBtn);
        increaseBtn.setText("+");
        increaseBtn.setOnClickListener(v -> appCompatEditText.setText(String.valueOf(Short.parseShort(appCompatEditText.getText().toString()) + 1)));
        addView(increaseBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    public void setMaxValue(short maxEpisodes) {
        this.maxEpisodes = maxEpisodes;
    }

    public void setValue(String actualValue) {
        appCompatEditText.setText(actualValue);
    }

    public short getValue() {
        return Short.parseShort(appCompatEditText.getText().toString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    class InputFilterMinMax implements InputFilter {
        String newVal;

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // Remove the string out of destination that is to be replaced
                newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.length());
                // Add the new string in
                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());

                if (isInRange(maxEpisodes, Integer.parseInt(newVal)))
                    return null;
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return "0";
        }

        private boolean isInRange(int b, int c) {
            return b > 0 ? c >= 0 && c <= b : c >= b && c <= 0;
        }
    }
}
