package com.zic.installfaker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TESTING_PACKAGE_NAME = "com.zic.test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvInfo = (TextView) findViewById(R.id.infoTV);
        Button btnTest = (Button) findViewById(R.id.testBTN);
        Button btnHistory = (Button) findViewById(R.id.historyBTN);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(100);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tvInfo.startAnimation(anim);

        btnTest.setOnClickListener(this);
        btnHistory.setOnClickListener(this);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.testBTN:
                Toast.makeText(getApplicationContext(), getString(R.string.toast_choose_fake), Toast.LENGTH_SHORT).show();
                Intent intentTest = new Intent(Intent.ACTION_VIEW);
                intentTest.setData(Uri.parse("market://details?id=" + TESTING_PACKAGE_NAME));
                startActivity(intentTest);
                break;
            case R.id.historyBTN:
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.fab:
                AddDialogFragment f = new AddDialogFragment();
                f.show(getFragmentManager(), "Create New App");
                break;
        }
    }
}