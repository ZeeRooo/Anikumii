package com.zeerooo.anikumii2.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterAnimes;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiSharedPreferences;

public class AnimesRecyclerViewFragment extends Fragment {
    private String toLoad;
    private AnikumiiRecyclerView anikumiiRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private AnikumiiSharedPreferences mPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null && getArguments() != null) {
            toLoad = getArguments().getString("toLoad");
            // if (toLoad.contains(".com/directorio")) {
            getActivity().findViewById(R.id.collapsing_appbarlayout).setVisibility(View.GONE);
            getActivity().findViewById(R.id.recyclerView).setVisibility(View.GONE);
            //  }
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_animes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getView() != null) {
            Toolbar mToolbar = getView().findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mPreferences = new AnikumiiSharedPreferences(getActivity());
            gridLayoutManager = new GridLayoutManager(getActivity(), mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));

            if (getArguments() != null) {
                mToolbar.setTitle(getArguments().getString("genre"));

                anikumiiRecyclerView = getView().findViewById(R.id.recyclerView);
                anikumiiRecyclerView.setAdapter(new AdapterAnimes());
                anikumiiRecyclerView.setLayoutManager(gridLayoutManager);
                anikumiiRecyclerView.setElementClass(getArguments().getString("element"));
                anikumiiRecyclerView.setToLoad(toLoad);
                anikumiiRecyclerView.setDynamicListener();
                anikumiiRecyclerView.setMaxDisplayedItems((byte) 12);
            }
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
    public void onDetach() {
        super.onDetach();
        if (getActivity() != null && getArguments() != null) {
            //  if (toLoad.contains(".com/directorio")) {
            anikumiiRecyclerView.exit();
            getActivity().findViewById(R.id.collapsing_appbarlayout).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
        }
    }
}
