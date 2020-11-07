package com.example.gymfit.system.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.example.gymfit.system.main.signin.Login;
import com.example.gymfit.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // CONTROLLO SE L'ONBOARDING DEVE ESSERE VISUALIZZATO SFRUTTANDO LE PREFERENZE DI SISTEMA DELL'APP
        SharedPreferences preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);

        if(!preferences.getBoolean("onboarding_complete",false)){

            Intent onboarding = new Intent(this, ActivitySystemOnBoarding.class);
            startActivity(onboarding);
            finish();
            return;
        }

    }

    public void loginIntent(View v){
        startActivity(new Intent(MainActivity.this, Login.class));
    }
}