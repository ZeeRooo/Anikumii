package com.zeerooo.anikumii2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.adapters.AdapterAnimes;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiRecyclerView;

public class AnimesRecyclerViewFragment extends Fragment {
    private String toLoad;
    private AnikumiiRecyclerView recyclerView;

    public AnimesRecyclerViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null && getArguments() != null) {
            toLoad = getArguments().getString("toLoad");
            // if (toLoad.contains(".com/directorio")) {
            getActivity().findViewById(R.id.collapsing_appbarlayout).setVisibility(View.GONE);
            getActivity().findViewById(R.id.episodesActRecyclerView).setVisibility(View.GONE);
            //  }
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main_app_bar, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getView() != null) {
            Toolbar mToolbar = getView().findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            recyclerView = getView().findViewById(R.id.new_animes_recycler_view);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);

            if (getArguments() != null) {
                mToolbar.setTitle(getArguments().getString("genre"));

                recyclerView.setAdapter(new AdapterAnimes(getActivity()));
                recyclerView.setElementClass(getArguments().getString("element"));
                recyclerView.setToLoad(toLoad);
                recyclerView.setDynamicListener((byte) 19);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() != null && getArguments() != null) {
            //  if (toLoad.contains(".com/directorio")) {
            recyclerView.exit();
            getActivity().findViewById(R.id.collapsing_appbarlayout).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.episodesActRecyclerView).setVisibility(View.VISIBLE);
        }
    }
}
