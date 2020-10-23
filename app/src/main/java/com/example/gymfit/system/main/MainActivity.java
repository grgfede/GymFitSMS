package com.example.gymfit.system.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.gymfit.user.main.signin.Login;
import com.example.gymfit.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void loginIntent(View v){
        startActivity(new Intent(MainActivity.this, Login.class));
    }
}