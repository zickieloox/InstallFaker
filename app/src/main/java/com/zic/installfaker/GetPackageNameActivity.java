package com.zic.installfaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class GetPackageNameActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intentGet = this.getIntent();
        String dataString = intentGet.getDataString();
        if (dataString == null) {
            dataString = intentGet.getStringExtra("android.intent.extra.TEXT");
        }

        String url = (dataString != null) ? dataString : "";

		/* get the package name from these urls:
            https://play.gooogle.com/details?id=<$pkgName>&<something>
			market://details?id=<$pkgName>&<something>

			https://play.gooogle.com/details?id=<$pkgName>
            market://details?id=<$pkgName>
            ...
		*/

        String pkgName;
        if (url.contains("&") && url.contains("details?id=")) {
            pkgName = url.substring(url.indexOf("=") + 1, url.indexOf("&"));
        } else if (url.contains("details?id=")) {
            pkgName = url.substring(url.indexOf("=") + 1);
        } else {
            pkgName = null;
        }
        if (pkgName != null) {

            // Copy $pkgName to clipboard and toast it
            //((ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("pkgName", pkgName));
            //Toast.makeText(this, pkgName + getString(R.string.toast_copied), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), CreateApkActivity.class);
            intent.putExtra(Globals.KEY_PACKAGE_NAME, pkgName);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.toast_err_link), Toast.LENGTH_LONG).show();
        }
        this.finish();
    }
}