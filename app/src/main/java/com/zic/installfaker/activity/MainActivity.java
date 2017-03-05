package com.zic.installfaker.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zic.installfaker.R;
import com.zic.installfaker.adapter.MainPagerAdapter;
import com.zic.installfaker.dialog.AddDialogFragment;
import com.zic.installfaker.fragment.PackageRecyclerFragment;
import com.zic.installfaker.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        tabLayout.setupWithViewPager(viewPager);
        fab.setOnClickListener(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        MainPagerAdapter mainAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mainAdapter.addFragment(new PackageRecyclerFragment(), getString(R.string.tab_history));
        mainAdapter.addFragment(new SettingsFragment(), getString(R.string.tab_settings));
        viewPager.setAdapter(mainAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                AddDialogFragment f = new AddDialogFragment();
                f.show(getSupportFragmentManager(), "Create New App");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_about:
                Toast.makeText(this, getString(R.string.menu_about), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_exit:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}