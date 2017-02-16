package com.zic.installfaker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

public class AddDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_add, null);

        final TextInputLayout tilPkgName = (TextInputLayout) view.findViewById(R.id.pkgNameTIL);
        final TextInputLayout tilAppName = (TextInputLayout) view.findViewById(R.id.appNameTIL);

        assert tilPkgName.getEditText() != null && tilAppName.getEditText() != null;

        builder
                .setTitle(getString(R.string.dialog_title_create_new))
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName = tilPkgName.getEditText().getText().toString().trim();
                        String appName = tilAppName.getEditText().getText().toString().trim();

                        Intent intent = new Intent(getActivity(), CreateApkActivity.class);
                        intent.putExtra(Globals.KEY_PACKAGE_NAME, pkgName);
                        intent.putExtra(Globals.KEY_APP_NAME, appName);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show(); // This is very important, show dialog before doing something
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        assert tilPkgName.getEditText() != null && tilAppName.getEditText() != null;

        tilPkgName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().contains(".")) {
                    tilPkgName.setErrorEnabled(true);
                    tilPkgName.setError(getString(R.string.til_err_invalid_pkg_name));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    tilPkgName.setErrorEnabled(false);
                    tilPkgName.setError(null);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

                tilAppName.getEditText().setText("_Z_" + s.toString().trim());

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    tilPkgName.setErrorEnabled(true);
                    tilPkgName.setError(getString(R.string.til_err_empty_pkg_name));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });

        return dialog;
    }
}
