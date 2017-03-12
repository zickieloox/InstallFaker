package com.zic.installfaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class PrefUtils {

    private static final String TAG = "PrefUtils";
    private static final String PREFS_NAME = "prefs";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static Set<String> getStringSet(final Context context, final String key, final Set<String> defaultValue) {
        return PrefUtils.getSharedPreferences(context).getStringSet(key, defaultValue);
    }

    public static boolean putStringSet(final Context context, final String key, final Set<String> value) {
        final SharedPreferences.Editor editor = PrefUtils.getSharedPreferences(context).edit();

        editor.remove(key);
        editor.putStringSet(key, value);

        return editor.commit();
    }

    public static boolean isFirstRun(Context context) {
        SharedPreferences prefs = PrefUtils.getSharedPreferences(context);

        if (prefs.getBoolean("first_run", true)) {
            prefs.edit().putBoolean("first_run", false).apply();
            return true;
        } else {
            return false;
        }
    }
}
