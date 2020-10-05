package com.example.gymfit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //CODICE CHE PERMETTE DI CHIUDERE LA TASTIERA IN AUTOMATICO QUANDO SI CLICCA SULLO SCHERMO
        findViewById(R.id.loginParentLayout).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
    }


    public void hideKeyboard(View view) {
        InputMethodManager imm =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}