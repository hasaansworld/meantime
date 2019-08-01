package com.meantime;

import android.graphics.Typeface;

import androidx.multidex.MultiDexApplication;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmUtils.init(this);
        changeFonts();
    }

    private void changeFonts() {
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/geometria.ttf");
        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/geometria_light.ttf");
        Typeface medium = Typeface.createFromAsset(getAssets(), "fonts/geometria_medium.ttf");
        Map<String, Typeface> fonts = new HashMap<>();
        fonts.put("sans-serif", regular);
        fonts.put("sans-serif-light", light);
        fonts.put("sans-serif-medium", medium);
        TypefaceUtil.overrideFonts(fonts);
    }
}
