package com.zeerooo.anikumii.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.AdapterAnimes;
import com.zeerooo.anikumii.adapters.AdapterMain;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;

public class AnikumiiMainFragment extends Fragment {

    public AnikumiiRecyclerView anikumiiRecyclerView;
    protected AnikumiiSharedPreferences mPreferences;
    protected boolean isHistory;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getView() != null) {
            mPreferences = new AnikumiiSharedPreferences(getActivity());

            anikumiiRecyclerView = getView().findViewById(R.id.recyclerView);
            anikumiiRecyclerView.setAdapter(new AdapterAnimes(isHistory));
            anikumiiRecyclerView.setRootView(getActivity().findViewById(R.id.activity_main_root_view));
        }
    }

    public void reactiveRecyclerView(String title, String toLoad, String elementClass, byte maxDisplayedItems) {
        ((AdapterMain) anikumiiRecyclerView.getAdapter()).removeItemsFromArray();
        anikumiiRecyclerView.setToLoad(toLoad);
        anikumiiRecyclerView.setElementClass(elementClass);
        anikumiiRecyclerView.setMaxDisplayedItems(maxDisplayedItems);

        ((MaterialToolbar) getActivity().findViewById(R.id.toolbar)).setTitle(title);
    }
}
