package com.zeerooo.anikumii2.anikumiiparts.glide;

import android.content.Context;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

import androidx.annotation.NonNull;

public class AnikumiiModelLoaderFactory implements ModelLoaderFactory<String, InputStream> {
    private Context context;

    AnikumiiModelLoaderFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new AnikumiiGlideModelLoader(multiFactory.build(GlideUrl.class, InputStream.class), context);
    }

    @Override
    public void teardown() {
    }
}
