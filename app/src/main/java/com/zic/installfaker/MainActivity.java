package com.zic.installfaker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TESTING_PACKAGE_NAME = "com.zic.test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTest = (Button) findViewById(R.id.testBTN);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_fake_this), Toast.LENGTH_SHORT).show();
                Intent intentTest = new Intent(Intent.ACTION_VIEW);
                intentTest.setData(Uri.parse("market://details?id=" + TESTING_PACKAGE_NAME));
                startActivity(intentTest);
            }
        });

        Button btnHistory = (Button) findViewById(R.id.historyBTN);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDialogFragment f = new AddDialogFragment();
                f.show(getFragmentManager(), "Create New App");
            }
        });
    }
}