package com.zic.installfaker.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zic.installfaker.data.Globals;
import com.zic.installfaker.R;
import com.zic.installfaker.data.Package;
import com.zic.installfaker.dialog.UninstallDialogFragment;
import com.zic.installfaker.utils.AppUtils;
import com.zic.installfaker.utils.PrefUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> implements View.OnClickListener {
    private static final String DIALOG_UNINSTALL = "Uninstall Dialog";
    private static final String MARKET_URL = "market://details?id=";
    private List<Package> packages;
    private Package curPackage;
    private Context context;
    private FragmentManager fragmentManager;
    private Fragment fragment;

    public PackageAdapter(Context context, FragmentManager fragmentManager, Fragment fragment, List<Package> packages) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.fragment = fragment;
        this.packages = packages;
        sortPackages();
    }

    private void sortPackages() {
        // Sort the $packages by creation time
        Collections.sort(packages, new Comparator<Package>() {
            @Override
            public int compare(Package o1, Package o2) {
                return Long.toString(o2.getCreationTime()).compareTo(Long.toString(o1.getCreationTime()));
            }
        });
    }

    public Package getCurPackage() {
        return this.curPackage;
    }

    @Override
    public PackageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_package, parent, false);

        return new PackageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        curPackage = packages.get(position);

        holder.itemView.setTag(curPackage);
        holder.itemView.setOnClickListener(this);

        holder.tvPkgName.setText(curPackage.getPkgName());
        holder.tvAppName.setText(curPackage.getAppName());
        holder.tvDayCounter.setText(curPackage.getDayCounter());

        holder.btnMoreIcon.setTag(curPackage);
        holder.btnMoreIcon.setOnClickListener(this);

        // If this current package is not installed, hide the uninstall icon,
        // $tvPkgNameItem with strike-through
        // and make current item is not clickable.
        if (!curPackage.isInstalled()) {
            holder.tvPkgName.setPaintFlags(holder.tvPkgName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.btnUninstallIcon.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
        } else {
            // Reset to normal to prevent item is messed up after scrolling
            holder.tvPkgName.setPaintFlags(holder.tvPkgName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.btnUninstallIcon.setVisibility(View.VISIBLE);
            // End reset.
            holder.btnUninstallIcon.setTag(curPackage);
            holder.btnUninstallIcon.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        switch (id) {
            case R.id.uninstallIconBTN:
                showUninstallDialog(v);
                return;
            case R.id.moreIconBTN:
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        showPopupMenu(v);
                    }
                });
                return;
            default:
                curPackage = (Package) v.getTag();
                String pkgName = curPackage.getPkgName();
                if (!AppUtils.launch(context, pkgName))
                    Toast.makeText(context, context.getString(R.string.toast_err_launch) + " " + pkgName + ".", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUninstallDialog(View v) {
        curPackage = (Package) v.getTag();

        UninstallDialogFragment f = new UninstallDialogFragment();
        f.setTargetFragment(fragment, 0);
        f.show(fragmentManager, DIALOG_UNINSTALL);
    }

    private void showPopupMenu(View v) {
        curPackage = (Package) v.getTag();

        PopupMenu popup = new PopupMenu(context, v);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_copy:
                        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("pkgName", curPackage.getPkgName()));
                        Toast.makeText(context, curPackage.getPkgName() + context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_playstore:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(MARKET_URL + curPackage.getPkgName()));
                        context.startActivity(intent);
                        return true;
                    case R.id.menu_remove:
                        // Remove $pkgName from the old $pkgInfoSet and save to SharedPreferences
                        Set<String> pkgInfoSet = new HashSet<>();
                        pkgInfoSet = PrefUtils.getStringSet(context, Globals.PREF_KEY_PACKAGE_INFO_SET, pkgInfoSet);
                        Set<String> newPkgInfoSet = new HashSet<>();
                        newPkgInfoSet.addAll(pkgInfoSet);
                        newPkgInfoSet.remove(curPackage.toString());
                        PrefUtils.putStringSet(context, Globals.PREF_KEY_PACKAGE_INFO_SET, newPkgInfoSet);

                        // Update the list
                        int pos = packages.indexOf(curPackage);
                        packages.remove(curPackage);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, packages.size());

                        return true;
                }
                return false;
            }
        });

        popup.show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView tvPkgName;
        private TextView tvAppName;
        private TextView tvDayCounter;
        private ImageButton btnUninstallIcon;
        private ImageButton btnMoreIcon;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.tvPkgName = (TextView) itemView.findViewById(R.id.pkgNameTV);
            this.tvAppName = (TextView) itemView.findViewById(R.id.appNameTV);
            this.tvDayCounter = (TextView) itemView.findViewById(R.id.dayCounterTV);
            this.btnUninstallIcon = (ImageButton) itemView.findViewById(R.id.uninstallIconBTN);
            this.btnMoreIcon = (ImageButton) itemView.findViewById(R.id.moreIconBTN);
        }
    }

}

