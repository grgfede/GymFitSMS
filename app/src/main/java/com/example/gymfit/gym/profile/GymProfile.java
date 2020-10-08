package com.example.gymfit.gym.profile;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gymfit.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GymProfile extends AppCompatActivity {
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromButton;
    private Animation toButton;

    private FloatingActionButton fab;
    private FloatingActionButton fabSetting;
    private FloatingActionButton fabSubscription;
    private FloatingActionButton fabRound;
    private boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);

        this.fab = findViewById(R.id.fab_add);
        this.fabSetting = findViewById(R.id.fab_setting);
        this.fabSubscription = findViewById(R.id.fab_subscription);
        this.fabRound = findViewById(R.id.fab_round);

        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromButton = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toButton = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        this.fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onAddButtons();
            }
        });

        this.fabSetting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: Open GymSetting activity
                Toast.makeText(GymProfile.this, "Setting Opt", Toast.LENGTH_SHORT).show();
            }
        });

        this.fabSubscription.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //TODO: Open GymSubscription activity
                Toast.makeText(GymProfile.this, "Subscription Opt", Toast.LENGTH_SHORT).show();
            }
        });

        this.fabRound.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //TODO: Open GymRound activity
                Toast.makeText(GymProfile.this, "Round Opt", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void onAddButtons() {
        setVisibility(clicked);
        setAnimation(clicked);
        setClickable(clicked);
        clicked = !clicked;
    }

    private void setAnimation(boolean clicked) {
        if(!clicked) {
            this.fabSetting.setVisibility(View.VISIBLE);
            this.fabSubscription.setVisibility(View.VISIBLE);
            this.fabRound.setVisibility(View.VISIBLE);
        } else {
            this.fabSetting.setVisibility(View.INVISIBLE);
            this.fabSubscription.setVisibility(View.INVISIBLE);
            this.fabRound.setVisibility(View.INVISIBLE);
        }
    }

    private void setVisibility(boolean clicked) {
        if(!clicked) {
            this.fabSetting.startAnimation(this.fromButton);
            this.fabSubscription.startAnimation(this.fromButton);
            this.fabRound.startAnimation(this.fromButton);
            this.fab.startAnimation(this.rotateOpen);
        } else {
            this.fabSetting.startAnimation(this.toButton);
            this.fabSubscription.startAnimation(this.toButton);
            this.fabRound.startAnimation(this.toButton);
            this.fab.startAnimation(this.rotateClose);
        }
    }

    private void setClickable(boolean clicked) {
        if(!clicked) {
            this.fabSetting.setClickable(true);
            this.fabSubscription.setClickable(true);
            this.fabRound.setClickable(true);
        } else {
            this.fabSetting.setClickable(false);
            this.fabSubscription.setClickable(false);
            this.fabRound.setClickable(false);
        }
    }

}