package com.zeerooo.anikumii.anikumiiparts;

import android.content.res.ColorStateList;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii.R;

import java.util.ArrayList;

public class AnikumiiInputChip extends ArrayList<String> {

    public AnikumiiInputChip(View rootView, String[] preloadedTags) {
        AppCompatAutoCompleteTextView autoCompleteTextView = rootView.findViewById(R.id.preloadedTags);

        if (preloadedTags != null)
            autoCompleteTextView.setAdapter(new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_dropdown_item_1line, preloadedTags));

        autoCompleteTextView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            addRemovableChip((String) parent.getItemAtPosition(position), rootView, true);
            autoCompleteTextView.setText("");
        });

        autoCompleteTextView.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE)) {
                addRemovableChip(v.getText().toString(), rootView, true);
                autoCompleteTextView.setText("");
                return true;
            } else
                return false;
        });
    }

    public void addRemovableChip(String text, View rootView, boolean add) {
        if (add)
            add(text);

        Chip chip = new Chip(rootView.getContext());
        chip.setChipBackgroundColor(ColorStateList.valueOf(rootView.getResources().getColor(R.color.mtrl_textinput_default_box_stroke_color)));
        chip.setText(text);
        chip.setTag(text);
        chip.setCloseIconVisible(true);
        ((ChipGroup) rootView.findViewById(R.id.tagsChipGroup)).addView(chip);
        chip.setOnCloseIconClickListener(v -> {
            ((ChipGroup) rootView.findViewById(R.id.tagsChipGroup)).removeView(v);
            remove(v.getTag());
        });
    }

}
