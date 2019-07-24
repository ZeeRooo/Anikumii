package com.zeerooo.anikumii.fragments;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.AdapterAnimes;
import com.zeerooo.anikumii.adapters.AdapterMain;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;

public class AnikumiiMainFragment extends Fragment {

    public AnikumiiRecyclerView anikumiiRecyclerView;
    GridLayoutManager gridLayoutManager;
    private final AnikumiiSharedPreferences anikumiiSharedPreferences;

    AnikumiiMainFragment(AnikumiiSharedPreferences anikumiiSharedPreferences) {
        this.anikumiiSharedPreferences = anikumiiSharedPreferences;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getView() != null) {
            gridLayoutManager = new GridLayoutManager(getActivity(), anikumiiSharedPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));

            anikumiiRecyclerView = getView().findViewById(R.id.recyclerView);
            anikumiiRecyclerView.setAdapter(new AdapterAnimes(false));
            anikumiiRecyclerView.setRootView(getActivity().findViewById(R.id.activity_main_root_view));
            anikumiiRecyclerView.setHasFixedSize(true);
        }
    }

    public void reactiveRecyclerView(String title, String toLoad, String elementClass, byte maxDisplayedItems) {
        ((AdapterMain) anikumiiRecyclerView.getAdapter()).removeItemsFromArray();
        anikumiiRecyclerView.setToLoad(toLoad);
        anikumiiRecyclerView.setElementClass(elementClass);
        anikumiiRecyclerView.setMaxDisplayedItems(maxDisplayedItems);

        ((MaterialToolbar) getActivity().findViewById(R.id.toolbar)).setTitle(title);
    }

    public void refreshGridLayoutManager(int spanCount) {
        gridLayoutManager.setSpanCount(spanCount);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (gridLayoutManager != null)
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                gridLayoutManager.setSpanCount(anikumiiSharedPreferences.getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300)));
            else
                gridLayoutManager.setSpanCount(anikumiiSharedPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
    }
}
