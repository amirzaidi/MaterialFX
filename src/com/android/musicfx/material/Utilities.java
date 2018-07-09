package com.android.musicfx.material;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class Utilities {
    public static boolean V26 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static String floatToString(float in) {
        String out = String.valueOf(in);
        if (out.endsWith(".0")) {
            out = out.substring(0, out.length() - 2);
        }
        return out;
    }
}
