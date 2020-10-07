package com.example.gymfit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //NASCONDO LA TITLE BAR
        getSupportActionBar().hide();

    }

    public void loginIntent(View v){
        startActivity(new Intent(MainActivity.this, Login.class));
    }
}