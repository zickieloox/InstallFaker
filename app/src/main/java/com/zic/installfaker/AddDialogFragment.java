package com.zic.installfaker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_add, null);
        final EditText edtPkgName = (EditText) view.findViewById(R.id.pkgNameEDT);
        final EditText edtAppName = (EditText) view.findViewById(R.id.appNameEDT);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title_create_new))
                .setMessage(getString(R.string.dialog_msg_create_new))
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName = edtPkgName.getText().toString();
                        String appName = edtAppName.getText().toString();
                        if (pkgName.length() == 0) {
                            Toast.makeText(getActivity(), getString(R.string.toast_err_empty_pkg_name), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Intent intent = new Intent(getActivity(), CreateApkActivity.class);
                        intent.putExtra(Globals.KEY_PACKAGE_NAME, pkgName);
                        intent.putExtra(Globals.KEY_APP_NAME, appName);
                        startActivity(intent);
                    }
                })

                .create();
    }
}
