package com.zeerooo.anikumii2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterEpisodes;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiRecyclerView;

import java.util.ArrayList;

/**
 * Created by ZeeRooo on 24/02/18
 */

public class TioEpisodesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recyclerview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null && getActivity() != null) {
            final AnikumiiRecyclerView anikumiiRecyclerView = getView().findViewById(R.id.recyclerView);
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            anikumiiRecyclerView.setLayoutManager(mLayoutManager);

            if (getArguments() != null) {
                ArrayList<String> genreList = getArguments().getStringArrayList("genreList");
                ArrayList<String> listRel = getArguments().getStringArrayList("listRel");

                final AdapterEpisodes adapterEpisodes = new AdapterEpisodes(getArguments().getString("ratingStr"), getArguments().getString("nextEpisodeDate"), getArguments().getString("animeAbout"), getArguments().getString("animeType"), genreList, listRel, getArguments().getBoolean("isFav"));

                anikumiiRecyclerView.setAdapter(adapterEpisodes);
                anikumiiRecyclerView.setElementClass("ul.episodes-list list-unstyled > li > a" + "---" + getArguments().getString("animeId"));
                anikumiiRecyclerView.setToLoad(getArguments().getString("animeUrl"));
                anikumiiRecyclerView.setDynamicListener();
                anikumiiRecyclerView.setMaxDisplayedItems((byte) 12);
            }
        }
    }
}
