package com.example.gymfit.system;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.gymfit.user.signin.Login;
import com.example.gymfit.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //NASCONDO LA TITLE BAR
        Objects.requireNonNull(getSupportActionBar()).hide();

    }

    public void loginIntent(View v){
        startActivity(new Intent(MainActivity.this, Login.class));
    }
}