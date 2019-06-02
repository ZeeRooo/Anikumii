package com.zeerooo.anikumii.anikumiiparts.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

@GlideModule
public class AnikumiiGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(String.class, InputStream.class, new AnikumiiModelLoaderFactory(/*context*/));

        super.registerComponents(context, glide, registry);
    }
}
