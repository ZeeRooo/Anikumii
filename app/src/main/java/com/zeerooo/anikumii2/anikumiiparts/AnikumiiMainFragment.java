package com.zeerooo.anikumii2.anikumiiparts;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterAnimes;

public class AnikumiiMainFragment extends Fragment {

    private AnikumiiRecyclerView anikumiiRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private AnikumiiSharedPreferences mPreferences;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getView() != null) {
            mPreferences = new AnikumiiSharedPreferences(getActivity());

            anikumiiRecyclerView = getView().findViewById(R.id.new_animes_recycler_view);
            anikumiiRecyclerView.setAdapter(new AdapterAnimes(getActivity()));
            anikumiiRecyclerView.setItemAnimator(new DefaultItemAnimator());
            //  anikumiiRecyclerView.setHasFixedSize(true);

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


    @Override
    public void onPause() {
        super.onPause();
        anikumiiRecyclerView.exit();
    }

    public void reactiveRecyclerView(String title, String toLoad, String elementClass, byte maxDisplayedItems) {
        anikumiiRecyclerView.setAdapter(new AdapterAnimes(getActivity()));
        anikumiiRecyclerView.setToLoad(toLoad);
        anikumiiRecyclerView.setElementClass(elementClass);
        anikumiiRecyclerView.setDynamicListener(maxDisplayedItems);

        ((Toolbar) getActivity().findViewById(R.id.toolbar)).setTitle(title);
    }
}
