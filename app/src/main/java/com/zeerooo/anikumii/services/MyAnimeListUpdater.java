package com.zeerooo.anikumii.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zeerooo.anikumii.R;
import com.zeerooo.anikumii.anikumiiparts.AnikumiiSharedPreferences;

public class MyAnimeListUpdater extends Worker {
    public MyAnimeListUpdater(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        if (!new AnikumiiSharedPreferences(context).getString("malUserName", context.getString(R.string.app_name)).equals(context.getString(R.string.app_name)))
            context.startService(new Intent(context, MALApiService.class).putExtra("action", (byte) 3));
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }
}
