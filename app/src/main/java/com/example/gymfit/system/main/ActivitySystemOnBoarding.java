package com.example.gymfit.system.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.example.gymfit.R;
import com.example.gymfit.system.main.signin.Login;
import com.google.android.material.button.MaterialButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

public class ActivitySystemOnBoarding extends AppCompatActivity {

    private ViewPager pager;
    private SmartTabLayout indicator;
    private MaterialButton skip;
    private MaterialButton next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system_onboarding);

        pager = (ViewPager)findViewById(R.id.pager);
        indicator = (SmartTabLayout)findViewById(R.id.indicator);
        skip = (MaterialButton) findViewById(R.id.skip);
        next = (MaterialButton) findViewById(R.id.next);

        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0 : return new FragmentSystemOnBoarding1();
                    case 1 : return new FragmentSystemOnBoarding2();
                    case 2 : return new FragmentSystemOnBoarding3();
                    default: return null;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        pager.setAdapter(adapter);

        indicator.setViewPager(pager);

        indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if(position == 2){
                    skip.setVisibility(View.GONE);
                    next.setText(R.string.onboarding_done);
                } else {
                    skip.setVisibility(View.VISIBLE);
                    next.setText(getResources().getString(R.string.onboarding_next));
                }
            }

        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnboarding();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pager.getCurrentItem() == 2){
                    finishOnboarding();
                } else {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }
            }
        });
    }

    private void finishOnboarding() {
        SharedPreferences preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);

        preferences.edit().putBoolean("onboarding_complete",true).apply();

        Intent main = new Intent(this, Login.class);
        startActivity(main);

        finish();
    }
}