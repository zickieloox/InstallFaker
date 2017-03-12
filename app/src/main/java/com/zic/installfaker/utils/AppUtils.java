package com.zic.installfaker.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;

import java.util.ArrayList;
import java.util.List;

public class AppUtils {

    private static final String TAG = "AppUtils";

    public static List<String> getInstalledApps(Context context) {

        List<String> installedApps = new ArrayList<>();
        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo app : apps) {
            installedApps.add(app.packageName);
        }

        return installedApps;
    }

    public static String getAppName(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(pkgName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "unknown");

    }

    public static boolean launch(Context context, String pkgName) {

        PackageManager pm = context.getPackageManager();
        Intent intentLaunch = pm.getLaunchIntentForPackage(pkgName);
        if (intentLaunch != null) {
            context.startActivity(intentLaunch);
        } else {
            return false;
        }

        return true;
    }

    public static boolean uninstall(String pkgName) {
        CommandResult result = Shell.SU.run("pm uninstall " + pkgName);
        if (result.exitCode != 0) {
            Log.e(TAG, "uninstall: " + result.getStderr());
            return false;
        }

        return true;
    }
}
