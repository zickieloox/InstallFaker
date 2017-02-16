package com.zic.installfaker;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageListFragment extends ListFragment implements View.OnClickListener, DialogClickListener {

    private Set<String> pkgInfoSet = new HashSet<>();
    private String pkgName;
    private String appName;
    private long creationDate;
    private PackageAdapter pkgAdapter;
    private Package curItem;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<String> installedApps = AppUtils.getInstalledApps(getActivity());

        pkgInfoSet = PrefUtils.getStringSet(getActivity(), Globals.KEY_PACKAGE_INFO_SET, pkgInfoSet);

        List<Package> packages = new ArrayList<>();
        for (String pkgInfo : pkgInfoSet) {
            pkgName = pkgInfo.split("[|]")[0];
            appName = AppUtils.getAppName(getActivity(), pkgName);
            creationDate = Long.valueOf(pkgInfo.split("[|]")[1]);
            packages.add(new Package(pkgName, appName, creationDate));
        }

        // Set installed for installed package
        for (Package pkg : packages)
            if (installedApps.contains(pkg.getPkgName()))
                pkg.setInstalled(true);

        if (packages.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.toast_empty_history), Toast.LENGTH_SHORT).show();
        } else {
            // Sort the $package by creation date
            Collections.sort(packages, new Comparator<Package>() {
                @Override
                public int compare(Package o1, Package o2) {
                    return Long.toString(o2.getCreationDate()).compareTo(Long.toString(o1.getCreationDate()));
                }
            });
        }

        pkgAdapter = new PackageAdapter(packages);
        //getListView().setDividerHeight(0);
        setListAdapter(pkgAdapter);
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        Package currentItem = (Package) listView.getItemAtPosition(position);
        String pkgName = currentItem.getPkgName();

        if (!AppUtils.launch(getActivity(), pkgName))
            Toast.makeText(getActivity(), getString(R.string.toast_err_launch) + " " + pkgName + ".", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onYesClick() {
        // Get root access first
        if (!Utils.exe("", true)) {
            Toast.makeText(getActivity(), getString(R.string.toast_err_root_access), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!AppUtils.uninstall(curItem.getPkgName())) {
            Toast.makeText(getActivity(), getString(R.string.toast_err_uninstall), Toast.LENGTH_SHORT).show();
            return;
        }
        curItem.setInstalled(false);
        pkgAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(final View v) {
        v.post(new Runnable() {
            @Override
            public void run() {
                showPopupMenu(v);
            }
        });
    }

    private void showPopupMenu(View v) {
        curItem = (Package) v.getTag();

        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_copy:
                        ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("pkgName", curItem.getPkgName()));
                        Toast.makeText(getActivity(), curItem.getPkgName() + " " + getString(R.string.toast_copied), Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_playstore:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + curItem.getPkgName()));
                        startActivity(intent);
                        return true;
                    case R.id.menu_remove:
                        // add $pkgName to the old $pkgInfoSet and save to SharedPreferences
                        Set<String> newPkgInfoSet = new HashSet<>();
                        pkgInfoSet = PrefUtils.getStringSet(getActivity(), Globals.KEY_PACKAGE_INFO_SET, pkgInfoSet);
                        newPkgInfoSet.addAll(pkgInfoSet);
                        newPkgInfoSet.remove(curItem.toString());
                        PrefUtils.putStringSet(getActivity(), Globals.KEY_PACKAGE_INFO_SET, newPkgInfoSet);
                        pkgAdapter.remove(curItem);
                        return true;
                }
                return false;
            }
        });

        popup.show();
    }

    class PackageAdapter extends ArrayAdapter<Package> {

        PackageAdapter(List<Package> items) {
            super(getActivity(), R.layout.fragment_package_item, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.fragment_package_item, parent, false);

            Package currentItem = getItem(position);
            TextView tvPkgNameItem = (TextView) view.findViewById(R.id.pkgNameItemTV);
            TextView tvAppNameItem = (TextView) view.findViewById(R.id.appNameItemTV);
            TextView tvDateItem = (TextView) view.findViewById(R.id.dateItemTV);
            ImageButton btnUninstallIcon = (ImageButton) view.findViewById(R.id.uninstallIconBTN);
            ImageButton btnMoreIcon = (ImageButton) view.findViewById(R.id.moreIconBTN);

            assert currentItem != null;
            pkgName = currentItem.getPkgName();
            appName = currentItem.getAppName();
            creationDate = currentItem.getCreationDate();

            // Calculate day number from creation date
            int day = (Utils.getDaysSinceEpoch(Utils.getCurMilliSec()) - (Utils.getDaysSinceEpoch(creationDate)));
            String dayCounter;
            if (day < 1) {
                dayCounter = "Today";
            } else if (day < 2) {
                dayCounter = 1 + " day ago";
            } else {
                DecimalFormat format = new DecimalFormat("#");
                dayCounter = format.format(day) + " days ago";
            }

            tvPkgNameItem.setText(pkgName);
            tvAppNameItem.setText(appName);
            tvDateItem.setText(dayCounter);

            btnMoreIcon.setTag(currentItem);
            btnMoreIcon.setOnClickListener(PackageListFragment.this);

            // If this current package is not installed, hide the uninstall icon, $tvPkgNameItem with strike-through and make current item is not clickable
            if (!currentItem.isInstalled()) {
                tvPkgNameItem.setPaintFlags(tvPkgNameItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                btnUninstallIcon.setVisibility(View.INVISIBLE);
                view.setOnClickListener(null);
                return view;
            }
            btnUninstallIcon.setTag(currentItem);
            btnUninstallIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    curItem = (Package) v.getTag();
                    DialogFragment f = new AlertDialogFragment();
                    Bundle bundle = new Bundle();
                    String dialogMsg = getString(R.string.alert_msg_uninstall);
                    bundle.putString(Globals.KEY_DIALOG_MESSAGE, dialogMsg);
                    f.setArguments(bundle);
                    f.setTargetFragment(PackageListFragment.this, 0);
                    f.show(getActivity().getFragmentManager(), "Uninstall Dialog");
                }
            });

            return view;
        }
    }
}
