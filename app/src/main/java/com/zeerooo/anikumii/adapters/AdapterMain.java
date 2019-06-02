package com.zeerooo.anikumii.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zeerooo.anikumii.misc.ItemsModel;

import java.util.ArrayList;

public class AdapterMain extends RecyclerView.Adapter {

    ArrayList<ItemsModel> animeList = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    public void appendArray(ArrayList<ItemsModel> arrayList) {
        int size = animeList.size();
        animeList.addAll(arrayList);
        notifyItemRangeInserted(size, arrayList.size());
    }

    public void removeItemsFromArray() {
        int size = animeList.size();
        animeList.clear();
        notifyItemRangeRemoved(0, size);
    }
}
