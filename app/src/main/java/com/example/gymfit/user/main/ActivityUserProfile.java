package com.example.gymfit.user.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.InitUserCallback;
import com.example.gymfit.user.conf.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityUserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private String userUID;
    private User user;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        try {
            // Get Toolbar from layout XML and init it
            MaterialToolbar materialToolbar = findViewById(R.id.menu_user_toolbar);
            setSupportActionBar(materialToolbar);

            // Get Drawer from layout XML and init it
            this.drawer = findViewById(R.id.drawer_user);
            this.navigationView = findViewById(R.id.navigation_user);
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, this.drawer, materialToolbar, R.string.title_open_drawer, R.string.title_close_open_drawer);
            this.drawer.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
            this.navigationView.setNavigationItemSelectedListener(this);

            // Initialize User
            initUserID();
            initUserFromDatabase(user -> {

            });

        } catch (Exception e) {
            AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
            AppUtils.restartActivity(this);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_user_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.system_toolbar_logout){
            FirebaseAuth.getInstance().signOut();
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof TextInputEditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_menu_home && !item.isChecked()) {
            // TODO: Start FragmentUserProfile
        } else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {
            // TODO: Start FragmentUserSubscriptions
        } else if (item.getItemId() == R.id.nav_menu_gyms && !item.isChecked()) {
            // TODO: Start FragmentUserListGyms
        } else if (item.getItemId() == R.id.nav_menu_help && !item.isChecked()) {
            // TODO: help fragment
        } else if (item.getItemId() == R.id.nav_menu_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        this.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            try {
                @SuppressLint("InflateParams")
                View headerView = LayoutInflater.from(this).inflate(R.layout.layout_system_drawer_user, null);
                this.navigationView.removeHeaderView(this.navigationView.getHeaderView(0));
                this.navigationView.addHeaderView(headerView);

                // Get view object
                MaterialTextView menuNameField = this.navigationView.getHeaderView(0).findViewById(R.id.header_user_name);
                CircleImageView imageMenu = this.navigationView.getHeaderView(0).findViewById(R.id.header_user_image);

                // Set view object
                menuNameField.setText(this.user.getUsername());
                Picasso.get().load(this.user.getUrlImage()).into(imageMenu);

            } catch (Exception e) {
                AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                AppUtils.restartActivity(this);
            }
        }
    }

    // Set methods

    /**
     * Set the current user auth for next uses with Database and Storage
     */
    private void initUserID() {
        this.userUID = this.currentUser.getUid();
    }

    /**
     * Take from Database all fields that will be add into layout XML file fields
     *
     * initUserCallback Callback method that used for set User object in the current class
     */
    private void initUserFromDatabase(InitUserCallback initUserCallback) {


    }
}

