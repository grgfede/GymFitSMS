package com.example.gymfit.user.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;


import com.example.gymfit.R;
import com.example.gymfit.gym.main.FragmentGymProfile;
import com.example.gymfit.gym.main.FragmentGymSettings;
import com.example.gymfit.gym.main.FragmentGymSubs;
import com.example.gymfit.user.conf.User;
import com.example.gymfit.user.main.signin.Login;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser userdb = FirebaseAuth.getInstance().getCurrentUser();
    private String userUid;
    private User user;

    private DrawerLayout drawer;
    private NavigationView nav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        MaterialToolbar materialToolbar =findViewById(R.id.tollbar_profile);
        setSupportActionBar(materialToolbar);

        this.drawer = findViewById(R.id.drawer_profile);
        this.nav = findViewById(R.id.navigation_user);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, this.drawer, materialToolbar, R.string.title_open_drawer, R.string.title_close_open_drawer);
        this.drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        this.nav.setNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_user_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.system_toolbar_logout){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (this.drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_menu_home && !item.isChecked()) {

        }
        else if (item.getItemId() == R.id.nav_menu_setting && !item.isChecked()) {

        }
        else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {

        }
        else if (item.getItemId() == R.id.nav_menu_logout) {
            finish();
        }

        this.drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

