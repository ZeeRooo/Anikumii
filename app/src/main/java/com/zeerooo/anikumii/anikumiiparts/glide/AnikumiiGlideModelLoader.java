package com.zeerooo.anikumii.anikumiiparts.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import java.io.InputStream;

class AnikumiiGlideModelLoader extends BaseGlideUrlLoader<String> {
    //public Context context;

    AnikumiiGlideModelLoader(ModelLoader<GlideUrl, InputStream> urlLoader/*, Context context*/) {
        super(urlLoader);
        // this.context = context;
    }

    @Override
    protected String getUrl(String s, int width, int height, Options options) {
        return s;
    }

    @Nullable
    @Override
    protected Headers getHeaders(String s, int width, int height, Options options) {
        return new LazyHeaders.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:66.0) Gecko/20100101 Firefox/66.0")
                //.addHeader("Cookie", "cf_clearance=" + ((Anikumii) context.getApplicationContext()).getCloudFlare())
                .build();
    }

    @Override
    public boolean handles(@NonNull String s) {
        return true;
    }
}
