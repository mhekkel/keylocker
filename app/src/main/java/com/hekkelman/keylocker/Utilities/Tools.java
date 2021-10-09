package com.hekkelman.keylocker.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

public class Tools {

    /* Get a color based on the current theme */
    public static int getThemeColor(Context context, int colorAttr) {
        Resources.Theme theme = context.getTheme();
        TypedArray arr = theme.obtainStyledAttributes(new int[]{colorAttr});

        int colorValue = arr.getColor(0, -1);
        arr.recycle();

        return colorValue;
    }

    /* Create a ColorFilter based on the current theme */
    public static ColorFilter getThemeColorFilter(Context context, int colorAttr) {
        return new PorterDuffColorFilter(getThemeColor(context, colorAttr), PorterDuff.Mode.SRC_IN);
    }
}
