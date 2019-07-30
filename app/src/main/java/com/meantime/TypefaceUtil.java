package com.meantime;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.BOLD_ITALIC;
import static android.graphics.Typeface.ITALIC;
import static android.graphics.Typeface.NORMAL;
import static android.graphics.Typeface.SANS_SERIF;

public class TypefaceUtil {
    /**
     * Using reflection to override default typefaces
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE
     * OVERRIDDEN
     *
     * @param typefaces map of fonts to replace
     */
    public static void overrideFonts(Map<String, Typeface> typefaces) {
        try {
            final Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);
            Map<String, Typeface> oldFonts = (Map<String, Typeface>) field.get(null);
            if (oldFonts != null) {
                oldFonts.putAll(typefaces);
            } else {
                oldFonts = typefaces;
            }
            field.set(null, oldFonts);
            field.setAccessible(false);
        } catch (Exception e) {
            Log.e("TypefaceUtil", "Can not set custom fonts");
        }
    }

    public static Typeface getTypeface(Context context, int fontType) {
        // here you can load the Typeface from asset or use default ones
        switch (fontType) {
            case BOLD:
                return Typeface.createFromAsset(context.getAssets(), "fonts/geometria_medium.ttf");
            case ITALIC:
                return Typeface.createFromAsset(context.getAssets(), "fonts/geometria_italic.ttf");
            case BOLD_ITALIC:
                return Typeface.createFromAsset(context.getAssets(), "fonts/geometria_mediumitalic.ttf");
            case NORMAL:
                return Typeface.createFromAsset(context.getAssets(), "fonts/geometria.ttf");
            default:
                return Typeface.createFromAsset(context.getAssets(), "fonts/geometria.ttf");
        }
    }
}
