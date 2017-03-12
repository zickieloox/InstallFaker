package com.zic.installfaker.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.softsec.editor.MainAxmlEditor;
import com.zic.installfaker.data.Globals;
import com.zic.installfaker.R;
import com.zic.installfaker.utils.FileUtils;
import com.zic.installfaker.utils.PrefUtils;
import com.zic.installfaker.utils.TimeUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreateApkActivity extends Activity {

    private static final String TAG = "CreateApkActivity";
    private static final String TESTING_PACKAGE_NAME = "com.zic.test";
    private String manifestPath;
    private String apkPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean firstRun = PrefUtils.isFirstRun(this);
        String filesDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String assetsDirName = "Zickie";
        String workingDirPath = filesDirPath + "/" + assetsDirName;
        String sampleManifestPath = workingDirPath + "/sample.xml";
        manifestPath = workingDirPath + "/AndroidManifest.xml";
        apkPath = workingDirPath + "/sample.apk";

        // Check read & write
        if (!FileUtils.isExternalStorageWritable()) {
            Toast.makeText(this, getString(R.string.toast_err_writable), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check first run
        if (firstRun) {
            // Copy files in assets to $filesDirPath
            if (!copyAssetsFile(assetsDirName, filesDirPath)) {
                Toast.makeText(this, getString(R.string.toast_err_copy_assets), Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
        }

        // Create the working directory first
        File file = new File(workingDirPath);
        if (!file.exists()) {
            Log.d(TAG, assetsDirName + " doesn't exist.");
            file.mkdir();
            if (!copyAssetsFile(assetsDirName, filesDirPath)) {
                Toast.makeText(this, getString(R.string.toast_err_copy_assets), Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
        }

        // Check existing
        if (!(new File(sampleManifestPath)).exists() || !(new File(apkPath)).exists()) {
            Log.e(TAG, "Files not found");
            if (!copyAssetsFile(assetsDirName, filesDirPath)) {
                Toast.makeText(this, getString(R.string.toast_err_copy_assets), Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
        }

        Bundle bundleGet = getIntent().getExtras();
        String pkgName = (String) bundleGet.get(Globals.KEY_PACKAGE_NAME);
        String appName = (String) bundleGet.get(Globals.KEY_APP_NAME);

        if (appName == null) {
            appName = "";
        }

        // Testing from MainActivity
        assert pkgName != null;
        if (pkgName.equals(TESTING_PACKAGE_NAME)) {
            if (MainAxmlEditor.change(sampleManifestPath, manifestPath, pkgName, appName)) {
                Toast.makeText(this, getString(R.string.toast_working), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_not_working), Toast.LENGTH_LONG).show();
            }

            this.finish();
            return;
        }

        // Change the package name and app name in $sampleManifestPath
        if (MainAxmlEditor.change(sampleManifestPath, manifestPath, pkgName, appName)) {

            saveToPref(pkgName);

            if (copyManifestToApk()) {
                installApk();
            } else {
                Toast.makeText(this, getString(R.string.toast_err_create_app), Toast.LENGTH_LONG).show();
                this.finish();
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_err_change_pkgname), Toast.LENGTH_LONG).show();
            this.finish();
        }

    }

    private void saveToPref(String pkgName) {
        // Add $pkgName to the old $pkgInfoSet and save to SharedPreferences
        Set<String> pkgInfoSet = new HashSet<>();
        Set<String> newPkgInfoSet = new HashSet<>();
        pkgInfoSet = PrefUtils.getStringSet(this, Globals.PREF_KEY_PACKAGE_INFO_SET, pkgInfoSet);
        newPkgInfoSet.addAll(pkgInfoSet);

        // Remove the old $pkgInfo contains present pkgName - prevent duplicate
        for (String pkgInfo : pkgInfoSet) {
            if (pkgInfo.contains(pkgName))
                newPkgInfoSet.remove(pkgInfo);
        }

        newPkgInfoSet.add(pkgName + "|" + TimeUtils.getCurMilliSec());
        PrefUtils.putStringSet(this, Globals.PREF_KEY_PACKAGE_INFO_SET, newPkgInfoSet);

        this.finish();
    }

    private boolean copyManifestToApk() {
        try {
            ZipFile zipFile = new ZipFile(apkPath);
            ArrayList<File> filesToAdd = new ArrayList<>();
            filesToAdd.add(new File(manifestPath));
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zipFile.addFiles(filesToAdd, parameters);

            return true;
        } catch (ZipException e) {
            Log.e(TAG, "copyManifestToApk: " + e.toString());
            return false;
        }
    }

    private void installApk() {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(installIntent);
    }

    private boolean copyAssetsFile(String fileName, String desDirPath) {
        AssetManager assetManager = this.getAssets();
        String assets[];
        // $desPath is file or dir
        String desPath = desDirPath + "/" + fileName;
        InputStream in;
        OutputStream out;

        // Copy from assets to $desDirPath
        try {
            assets = assetManager.list(fileName);
            // If $des is not a directory
            if (assets.length == 0) {
                in = assetManager.open(fileName);
                out = new FileOutputStream(desPath);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();

            } else {
                File des = new File(desPath);
                // $des is now a directory
                if (!des.exists())
                    des.mkdir();
                for (String asset : assets) {
                    copyAssetsFile(fileName + "/" + asset, desDirPath);
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "copyAssetsFile" + e.toString());
            return false;
        }
    }

}