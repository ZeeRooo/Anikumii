package com.zeerooo.anikumii2.fragments;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterAnimes;
import com.zeerooo.anikumii2.adapters.AdapterMain;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;

public class AnikumiiMainFragment extends Fragment {

    public AnikumiiRecyclerView anikumiiRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private AnikumiiSharedPreferences mPreferences;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getView() != null) {
            mPreferences = new AnikumiiSharedPreferences(getActivity());

            anikumiiRecyclerView = getView().findViewById(R.id.recyclerView);
            anikumiiRecyclerView.setAdapter(new AdapterAnimes());

            gridLayoutManager = new GridLayoutManager(getActivity(), mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
            anikumiiRecyclerView.setLayoutManager(gridLayoutManager);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300)));
        else
            gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
    }

    public void reactiveRecyclerView(String title, String toLoad, String elementClass, byte maxDisplayedItems) {
        ((AdapterMain) anikumiiRecyclerView.getAdapter()).removeItemsFromArray();
        anikumiiRecyclerView.setToLoad(toLoad);
        anikumiiRecyclerView.setElementClass(elementClass);
        anikumiiRecyclerView.setMaxDisplayedItems(maxDisplayedItems);

        ((Toolbar) getActivity().findViewById(R.id.toolbar)).setTitle(title);
    }
}
