package com.zeerooo.anikumii.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.AdapterEpisodes;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiRecyclerView;

/**
 * Created by ZeeRooo on 24/02/18
 */

public class TioEpisodesFragment extends Fragment {
    private AnikumiiRecyclerView anikumiiRecyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recyclerview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null && getActivity() != null) {
            anikumiiRecyclerView = getView().findViewById(R.id.recyclerView);
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            anikumiiRecyclerView.setLayoutManager(mLayoutManager);

            if (getArguments() != null) {
                anikumiiRecyclerView.setAdapter(new AdapterEpisodes(getArguments().getString("animeId"), getArguments().getString("malUrl"), getArguments().getString("ratingStr"), getArguments().getString("nextEpisodeDate"), getArguments().getString("animeAbout"), getArguments().getString("animeType"), getArguments().getStringArrayList("genreList"), getArguments().getStringArrayList("listRel")));
                anikumiiRecyclerView.setRootView(getView());
                anikumiiRecyclerView.setElementClass("ul.episodes-list list-unstyled > li > a");
                anikumiiRecyclerView.setToLoad(getArguments().getString("animeUrl"));
                anikumiiRecyclerView.setMaxDisplayedItems((byte) 12);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        anikumiiRecyclerView.exit();
    }
}
