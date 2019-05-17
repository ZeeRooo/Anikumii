package com.zeerooo.anikumii2.anikumiiparts.glide;

import android.content.Context;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.zeerooo.anikumii2.Anikumii;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AnikumiiGlideModelLoader extends BaseGlideUrlLoader<String> {
    public Context context;

    AnikumiiGlideModelLoader(ModelLoader<GlideUrl, InputStream> urlLoader, Context context) {
        super(urlLoader);
        this.context = context;
    }

    @Override
    protected String getUrl(String s, int width, int height, Options options) {
        return s;
    }

    @Nullable
    @Override
    protected Headers getHeaders(String s, int width, int height, Options options) {
        return new LazyHeaders.Builder()
                .addHeader("User-Agent", Anikumii.userAgent)
                .addHeader("Cookie", "cf_clearance=" + ((Anikumii) context.getApplicationContext()).getCloudFlare())
                .build();
    }

    @Override
    public boolean handles(@NonNull String s) {
        return true;
    }
}
