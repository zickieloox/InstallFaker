package com.zic.installfaker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageRecyclerFragment extends Fragment implements OnUninstallDialogClickListener {
    private static final String MARKET_URL = "market://details?id=";
    private static final String DIALOG_UNINSTALL = "Uninstall Dialog";
    private Set<String> pkgInfoSet = new HashSet<>();
    private PackageAdapter pkgAdapter;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView pkgRecycler;
    private FloatingActionButton fab;
    private List<Package> packages = new ArrayList<>();
    private Package curPackage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_package_list, container, false);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshLayout);
        pkgRecycler = (RecyclerView) rootView.findViewById(R.id.packageRecycler);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        loadPackages();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPackages();
            }
        });

        pkgRecycler.setLayoutManager(new LinearLayoutManager(container.getContext()));
        pkgRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return rootView;
    }

    private void loadPackages() {
        List<String> installedApps = AppUtils.getInstalledApps(getActivity());

        pkgInfoSet = PrefUtils.getStringSet(getActivity(), Globals.PREF_KEY_PACKAGE_INFO_SET, pkgInfoSet);

        // Clear the $packages before update
        packages.clear();

        for (String pkgInfo : pkgInfoSet) {
            String pkgName = pkgInfo.split("[|]")[0];
            Long creationTime = Long.valueOf(pkgInfo.split("[|]")[1]);
            String appName = AppUtils.getAppName(getActivity(), pkgName);

            // Calculate day number from creation date
            int day = (Utils.getDaysSinceEpoch(Utils.getCurMilliSec()) - (Utils.getDaysSinceEpoch(creationTime)));
            String dayCounter;
            if (day < 1) {
                dayCounter = getString(R.string.text_today);
            } else if (day < 2) {
                dayCounter = 1 + getString(R.string.text_day_ago);
            } else {
                DecimalFormat format = new DecimalFormat("#");
                dayCounter = format.format(day) + getString(R.string.text_days_ago);
            }

            packages.add(new Package(pkgName, appName, creationTime, dayCounter));
        }

        // Set installed for installed package
        for (Package pkg : packages)
            if (installedApps.contains(pkg.getPkgName()))
                pkg.setInstalled(true);

        if (packages.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.toast_empty_history), Toast.LENGTH_SHORT).show();
            return;
        }

        setupAdapter();
    }

    private void setupAdapter() {
        pkgAdapter = new PackageAdapter(packages);
        pkgRecycler.setAdapter(pkgAdapter);
        refreshLayout.setRefreshing(false);
    }


    public void onUninstallClick() {
        curPackage = pkgAdapter.getCurItem();

        // Get root access first
        if (!Utils.exe("", true)) {
            Toast.makeText(getActivity(), getString(R.string.toast_err_root_access), Toast.LENGTH_SHORT).show();
            return;
        }

        // Uninstall selected package
        if (!AppUtils.uninstall(curPackage.getPkgName())) {
            Toast.makeText(getActivity(), getString(R.string.toast_err_uninstall), Toast.LENGTH_SHORT).show();
            return;
        }
        curPackage.setInstalled(false);
        pkgAdapter.notifyDataSetChanged();
    }

    class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> implements View.OnClickListener {
        private List<Package> items;
        private Package curItem;

        PackageAdapter(List<Package> items) {
            this.items = items;
            sortItems();
        }

        private void sortItems() {
            // Sort the $packages by creation time
            Collections.sort(items, new Comparator<Package>() {
                @Override
                public int compare(Package o1, Package o2) {
                    return Long.toString(o2.getCreationTime()).compareTo(Long.toString(o1.getCreationTime()));
                }
            });
        }

        private Package getCurItem() {
            return curItem;
        }

        @Override
        public PackageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_package, parent, false);

            return new PackageAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            curItem = items.get(position);

            holder.itemView.setTag(curItem);
            holder.itemView.setOnClickListener(this);

            holder.tvPkgName.setText(curItem.getPkgName());
            holder.tvAppName.setText(curItem.getAppName());
            holder.tvDayCounter.setText(curItem.getDayCounter());

            holder.btnMoreIcon.setTag(curItem);
            holder.btnMoreIcon.setOnClickListener(this);

            // If this current package is not installed, hide the uninstall icon,
            // $tvPkgNameItem with strike-through
            // and make current item is not clickable.
            if (!curItem.isInstalled()) {
                holder.tvPkgName.setPaintFlags(holder.tvPkgName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.btnUninstallIcon.setVisibility(View.INVISIBLE);
                holder.itemView.setOnClickListener(null);
            } else {
                // Reset to normal to prevent item is messed up after scrolling
                holder.tvPkgName.setPaintFlags(holder.tvPkgName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                holder.btnUninstallIcon.setVisibility(View.VISIBLE);
                // End reset.
                holder.btnUninstallIcon.setTag(curItem);
                holder.btnUninstallIcon.setOnClickListener(this);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
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
                    String pkgName = curItem.getPkgName();
                    if (!AppUtils.launch(getActivity(), pkgName))
                        Toast.makeText(getActivity(), getString(R.string.toast_err_launch) + " " + pkgName + ".", Toast.LENGTH_SHORT).show();
            }
        }

        private void showUninstallDialog(View v) {
            curItem = (Package) v.getTag();

            UninstallDialogFragment f = new UninstallDialogFragment();
            f.setTargetFragment(PackageRecyclerFragment.this, 0);
            f.show(getActivity().getSupportFragmentManager(), DIALOG_UNINSTALL);
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
                            Toast.makeText(getActivity(), curItem.getPkgName() + getString(R.string.toast_copied), Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.menu_playstore:
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(MARKET_URL + curItem.getPkgName()));
                            startActivity(intent);
                            return true;
                        case R.id.menu_remove:
                            // Remove $pkgName from the old $pkgInfoSet and save to SharedPreferences
                            Set<String> pkgInfoSet = new HashSet<>();
                            pkgInfoSet = PrefUtils.getStringSet(getActivity(), Globals.PREF_KEY_PACKAGE_INFO_SET, pkgInfoSet);
                            Set<String> newPkgInfoSet = new HashSet<>();
                            newPkgInfoSet.addAll(pkgInfoSet);
                            newPkgInfoSet.remove(curItem.toString());
                            PrefUtils.putStringSet(getActivity(), Globals.PREF_KEY_PACKAGE_INFO_SET, newPkgInfoSet);

                            // Update the list
                            int pos = items.indexOf(curItem);
                            items.remove(curItem);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, items.size());

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

}
