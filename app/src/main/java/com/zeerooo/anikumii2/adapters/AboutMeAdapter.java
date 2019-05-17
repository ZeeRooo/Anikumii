package com.zeerooo.anikumii2.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.activities.AboutUserActivity;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii2.anikumiiparts.AnikumiiUiHelper;
import com.zeerooo.anikumii2.fragments.AnimesRecyclerViewFragment;
import com.zeerooo.anikumii2.misc.ItemsModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ZeeRooo on 13/01/18
 */

public class AboutMeAdapter extends RecyclerView.Adapter<AboutMeAdapter.MyViewHolder> {
    private ArrayList<ItemsModel> animeList;
    private AboutUserActivity activity;
    private String section_name, profileUrl;

    public AboutMeAdapter(AboutUserActivity context, ArrayList<ItemsModel> animeList, String section_name, String profileUrl) {
        this.animeList = animeList;
        this.activity = context;
        this.section_name = section_name;
        this.profileUrl = profileUrl;
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_about_user_container, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        AdapterAnimes animeListFav = new AdapterAnimes(activity, true);
        animeListFav.addAll(animeList);
        holder.anikumiiRecyclerView.setHasFixedSize(true);
        holder.anikumiiRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        holder.anikumiiRecyclerView.setAdapter(animeListFav);
        AnikumiiUiHelper.transparentBackground(holder.more);
        holder.more.setText(activity.getString(R.string.section_more, section_name));
        holder.more.setOnClickListener((View view) -> {
            switch (section_name) {
                case "Animes favoritos":
                    startFragment(profileUrl + "/favoritos");
                    break;
                case "Animes que veo":
                    startFragment(profileUrl + "/siguiendo");
                    break;
                default:
                    startFragment(profileUrl + "/lista_espera");
                    break;
            }
        });
    }

    private void startFragment(String load) {
        Bundle bundle = new Bundle();
        bundle.putString("toLoad", load);
        bundle.putString("genre", section_name);
        bundle.putString("element", "div.Image");
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AnimesRecyclerViewFragment fragment = new AnimesRecyclerViewFragment();
        fragment.setArguments(bundle);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class MyViewHolder extends AnikumiiRecyclerView.ViewHolder {
        private AnikumiiRecyclerView anikumiiRecyclerView;
        private Button more;

        MyViewHolder(View view) {
            super(view);
            anikumiiRecyclerView = view.findViewById(R.id.recycler_view_list);
            more = view.findViewById(R.id.section_more);
        }
    }
}