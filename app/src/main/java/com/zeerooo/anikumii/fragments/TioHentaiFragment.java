package com.zeerooo.anikumii.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zeerooo.anikumii.Anikumii;
import com.zeerooo.anikumii.R;

public class TioHentaiFragment extends AnikumiiMainFragment {

    private boolean isFirstTime = true;
    private GridLayoutManager gridLayoutManager;

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
            gridLayoutManager = new GridLayoutManager(getActivity(), mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
            anikumiiRecyclerView.setLayoutManager(gridLayoutManager);

            isHistory = false;

            reactiveRecyclerView(getString(R.string.latest_episodes), "https://tiohentai.com/", "article.episode", (byte) 20);
            anikumiiRecyclerView.setDynamicListener();

            isFirstTime = false;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (gridLayoutManager != null)
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300)));
            else
                gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
    }
}
