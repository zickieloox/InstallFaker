package com.zic.installfaker.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zic.installfaker.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private static final String TESTING_PACKAGE_NAME = "com.zic.test";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_settings, container, false);
        TextView tvInfo = (TextView) rootView.findViewById(R.id.infoTV);
        Button btnTest = (Button) rootView.findViewById(R.id.testBTN);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(100);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tvInfo.startAnimation(anim);

        btnTest.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.testBTN:
                Toast.makeText(getContext(), getString(R.string.toast_choose_fake), Toast.LENGTH_SHORT).show();
                Intent intentTest = new Intent(Intent.ACTION_VIEW);
                intentTest.setData(Uri.parse("market://details?id=" + TESTING_PACKAGE_NAME));
                startActivity(intentTest);
                break;
        }
    }
}
