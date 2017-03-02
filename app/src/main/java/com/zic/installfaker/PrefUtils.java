package com.zic.installfaker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

class PrefUtils {

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    static Set<String> getStringSet(final Context context, final String key, final Set<String> defaultValue) {
        return PrefUtils.getSharedPreferences(context).getStringSet(key, defaultValue);
    }

    static boolean putStringSet(final Context context, final String key, final Set<String> value) {
        final SharedPreferences.Editor editor = PrefUtils.getSharedPreferences(context).edit();

        editor.putStringSet(key, value);

        return editor.commit();
    }

    static boolean isFirstRun(Context context) {
        SharedPreferences prefs = PrefUtils.getSharedPreferences(context);

        if (prefs.getBoolean("first_run", true)) {
            prefs.edit().putBoolean("first_run", false).apply();
            return true;
        } else {
            return false;
        }
    }
}
