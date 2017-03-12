package com.zic.installfaker.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jrummyapps.android.shell.Shell;
import com.zic.installfaker.R;
import com.zic.installfaker.adapter.PackageAdapter;
import com.zic.installfaker.data.Globals;
import com.zic.installfaker.data.Package;
import com.zic.installfaker.listener.OnUninstallDialogClickListener;
import com.zic.installfaker.utils.AppUtils;
import com.zic.installfaker.utils.PrefUtils;
import com.zic.installfaker.utils.TimeUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageRecyclerFragment extends Fragment implements OnUninstallDialogClickListener {

    private static final String TAG = "PackageRecyclerFragment";
    private Set<String> pkgInfoSet = new HashSet<>();
    private PackageAdapter pkgAdapter;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView pkgRecycler;
    private FloatingActionButton fab;
    private List<Package> packages = new ArrayList<>();

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
            int day = (TimeUtils.getDaysSinceEpoch(TimeUtils.getCurMilliSec()) - (TimeUtils.getDaysSinceEpoch(creationTime)));
            String dayCounter;
            if (day < 1) {
                dayCounter = getString(R.string.text_today);
            } else if (day < 2) {
                dayCounter = 1 + getString(R.string.text_day_ago);
            } else {
                DecimalFormat format = new DecimalFormat("#");
                dayCounter = format.format(day) + getString(R.string.text_days_ago);
            }

            packages.add(new com.zic.installfaker.data.Package(pkgName, appName, creationTime, dayCounter));
        }

        // Set installed for installed package
        for (com.zic.installfaker.data.Package pkg : packages)
            if (installedApps.contains(pkg.getPkgName()))
                pkg.setInstalled(true);

        if (packages.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.toast_empty_history), Toast.LENGTH_SHORT).show();
            return;
        }

        setupAdapter();
    }

    private void setupAdapter() {
        pkgAdapter = new PackageAdapter(getContext(), getActivity().getSupportFragmentManager(), PackageRecyclerFragment.this, packages);
        pkgRecycler.setAdapter(pkgAdapter);
        refreshLayout.setRefreshing(false);
    }


    public void onUninstallClick() {
        Package curPackage = pkgAdapter.getCurPackage();
        String pkgName = curPackage.getPkgName();

        new UninstallTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pkgName);
    }

    private class UninstallTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {

            if (!Shell.SU.available()) {
                return 1;
            } else {
                if (!AppUtils.uninstall(params[0])) {
                    return 2;
                }
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                Package curPackage = pkgAdapter.getCurPackage();
                curPackage.setInstalled(false);
                pkgAdapter.notifyDataSetChanged();
            } else if (result == 1) {
                Toast.makeText(getActivity(), getString(R.string.toast_err_root_access), Toast.LENGTH_SHORT).show();
            } else if (result == 2) {
                Toast.makeText(getActivity(), getString(R.string.toast_err_uninstall), Toast.LENGTH_SHORT).show();
            }

        }
    }

}
