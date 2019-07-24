package com.zeerooo.anikumii.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;

public class TioAnimeFragment extends AnikumiiMainFragment {

    public TioAnimeFragment(AnikumiiSharedPreferences anikumiiSharedPreferences) {
        super(anikumiiSharedPreferences);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recyclerview, container, false);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        Anikumii.dominium = "https://tioanime.com";
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        anikumiiRecyclerView.setLayoutManager(gridLayoutManager);

        reactiveRecyclerView(getString(R.string.latest_episodes), "https://tioanime.com/", "article.episode", (byte) 20);
        anikumiiRecyclerView.setDynamicListener();
    }
}
