package com.zeerooo.anikumii.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.adapters.AdapterAnimes;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiRecyclerView;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;

public class AnimeActivity extends AppCompatActivity {
    private AnikumiiRecyclerView anikumiiRecyclerView;
    private boolean isHistory;
    private GridLayoutManager gridLayoutManager;
    private AnikumiiSharedPreferences mPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_animes);

        isHistory = getIntent().getStringExtra("title").equals("Historial");

        MaterialToolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getIntent().getStringExtra("title"));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        anikumiiRecyclerView = findViewById(R.id.recyclerView);
        anikumiiRecyclerView.setRootView(findViewById(R.id.recyclerView));

        if (isHistory) {
            anikumiiRecyclerView.initDB();
            anikumiiRecyclerView.setAdapter(new AdapterAnimes(true));
            anikumiiRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            anikumiiRecyclerView.setToLoad("history");
            anikumiiRecyclerView.setMaxDisplayedItems((byte) 23);
        } else {
            mPreferences = new AnikumiiSharedPreferences(this);

            gridLayoutManager = new GridLayoutManager(this, mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
            anikumiiRecyclerView.setAdapter(new AdapterAnimes(false));
            anikumiiRecyclerView.setLayoutManager(gridLayoutManager);
            anikumiiRecyclerView.setElementClass(getIntent().getStringExtra("element"));
            anikumiiRecyclerView.setToLoad(getIntent().getStringExtra("toLoad"));
            anikumiiRecyclerView.setMaxDisplayedItems((byte) 12);
        }

        anikumiiRecyclerView.setDynamicListener();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isHistory && gridLayoutManager != null)
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsLandscape", Math.round((float) getResources().getDisplayMetrics().heightPixels / 300)));
            else
                gridLayoutManager.setSpanCount(mPreferences.getInt("gridColumnsPortrait", Math.round((float) getResources().getDisplayMetrics().widthPixels / 300)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        anikumiiRecyclerView.exit();
        if (isHistory)
            anikumiiRecyclerView.closeDB();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}