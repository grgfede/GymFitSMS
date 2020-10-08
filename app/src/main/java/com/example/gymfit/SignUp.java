package com.example.gymfit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        toolbarSettings(); //MI INIZIALIZZO LE IMPOSTAZIONI DELL'ACTIONBAR


        ViewPager viewPager = findViewById(R.id.viewPager);

        AuthenticationPagerAdapter pagerAdapter = new AuthenticationPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragmet(new SignUpFragment());
        pagerAdapter.addFragmet(new SignUp2Fragment());
        viewPager.setAdapter(pagerAdapter);
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

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