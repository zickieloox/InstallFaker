package com.zic.installfaker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class AppUtils {

    private static final String TAG = "AppUtils";

    static List<String> getInstalledApps(Context context) {

        List<String> installedApps = new ArrayList<>();
        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo app : apps) {
            installedApps.add(app.packageName);
        }

        return installedApps;
    }

    static boolean launch(Context context, String pkgName) {
        // Launch selected Package app
        PackageManager pm = context.getPackageManager();
        Intent intentLaunch = pm.getLaunchIntentForPackage(pkgName);
        if (intentLaunch != null) {
            context.startActivity(intentLaunch);
        } else {
            return false;
        }

        return true;
    }

    static boolean uninstall(String pkgName) {
        if (!Utils.exe("pm uninstall " + pkgName, true) && !Utils.getExeError().contains("WARNING")) {
            Log.e(TAG + "_Uninstall", Utils.getExeError());
            return false;
        }

        return true;
    }
}
