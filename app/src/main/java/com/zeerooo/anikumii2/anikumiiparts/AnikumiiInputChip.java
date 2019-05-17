package com.zeerooo.anikumii2.anikumiiparts;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.zeerooo.anikumii2.R;

import java.util.ArrayList;

public class AnikumiiInputChip extends ArrayList<String> {

    public AnikumiiInputChip(View rootView, String[] preloadedTags) {
        AutoCompleteTextView autoCompleteTextView = rootView.findViewById(R.id.preloadedTags);

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
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#29b6f6")));
        chip.setText(text);
        chip.setTag(text);
        chip.setCloseIconVisible(true);
        ((ChipGroup) rootView.findViewById(R.id.tagsChipGroup)).addView(chip);
        chip.setOnCloseIconClickListener((View v) -> {
            ((ChipGroup) rootView.findViewById(R.id.tagsChipGroup)).removeView(v);
            remove(v.getTag());
        });
    }

}
