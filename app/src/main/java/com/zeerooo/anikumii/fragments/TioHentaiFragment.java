package com.zeerooo.anikumii.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;

public class TioHentaiFragment extends AnikumiiMainFragment {

    private boolean isFirstTime = true;

    public TioHentaiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recyclerview, container, false);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        Anikumii.dominium = "https://tiohentai.com";

        if (menuVisible && isFirstTime) {
            reactiveRecyclerView(getString(R.string.latest_episodes), "https://tiohentai.com/", "article.episode", (byte) 20);
            anikumiiRecyclerView.setDynamicListener();

            isFirstTime = false;
        }
    }
}