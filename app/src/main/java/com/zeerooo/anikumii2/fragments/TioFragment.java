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

public class TioFragment extends Fragment {

    public TioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tio_episodes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null && getActivity() != null) {
            final AnikumiiRecyclerView anikumiiRecyclerView = getView().findViewById(R.id.episodesActRecyclerView);
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            anikumiiRecyclerView.setNestedScrollingEnabled(true);
            anikumiiRecyclerView.setLayoutManager(mLayoutManager);
            anikumiiRecyclerView.setHasFixedSize(true);

            if (getArguments() != null) {
                ArrayList<String> genreList = getArguments().getStringArrayList("genreList");
                ArrayList<String> listRel = getArguments().getStringArrayList("listRel");

                final AdapterEpisodes adapterEpisodes = new AdapterEpisodes(getActivity(), getArguments().getString("ratingStr"), getArguments().getString("nextEpisode_date"), getArguments().getString("anime_about"), getArguments().getString("animeType"), genreList, listRel, getArguments().getBoolean("isFav"), getArguments().getBoolean("isFollowing"), getArguments().getBoolean("isPending"));

                anikumiiRecyclerView.setAdapter(adapterEpisodes);
                anikumiiRecyclerView.setElementClass("ul.episodes-list list-unstyled > li > a" + "---" + getArguments().getString("animeId"));
                anikumiiRecyclerView.setToLoad(getArguments().getString("animeUrl"));
                anikumiiRecyclerView.setDynamicListener((byte) 12);
            }
        }
    }
}
