package com.example.gymfit.user.signup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.gymfit.R;
import com.example.gymfit.user.signin.Login;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ViewPager viewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        toolbarSettings();
        SignUpFragment fragmentOne = new SignUpFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.viewPager, fragmentOne).commit();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /*
     *  METODO CHE MI PERMETTE DI TORNARE INDIETRO QUANDO CLICCO SULLA FRECCIA
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent myIntent = new Intent(SignUp.this, Login.class);
                SignUp.this.startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /*
        METODO CHE GESTISCE IL CAMBIAMENTO DEL COLORE DELLA TOOLBAR
        In questo caso voglio che il colore della toolbar non sia
        quello del primary color
     */
    private void toolbarSettings() {
        // Definisco l'oggetto toolbar
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // Definisco l'oggetto per il colore
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00000000"));

        // Setto il colore della toolbar con l'oggetto colore creato prima
        actionBar.setBackgroundDrawable(colorDrawable);

        //Rimuovo il titolo dalla toolbar
        actionBar.setDisplayShowTitleEnabled(false);

        //Setto l'icona della freccia nell'action bar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    class AuthenticationPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        public AuthenticationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        void addFragmet(Fragment fragment) {
            fragmentList.add(fragment);
        }
    }
}