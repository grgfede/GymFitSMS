package com.example.gymfit.gym.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.GymConfDBCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ActivityGymProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String ERROR_LOG = "error";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userUid;
    private Gym gym;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);

        MaterialToolbar toolbar = findViewById(R.id.menu_gym_toolbar);
        setSupportActionBar(toolbar);

        this.drawer = findViewById(R.id.drawer_gym);
        this.navigationView = findViewById(R.id.navigation_gym);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, this.drawer, toolbar, R.string.title_open_drawer, R.string.title_close_open_drawer);
        this.drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        this.navigationView.setNavigationItemSelectedListener(this);

        setUserUid();
        setInterface(gymTmp -> {
            this.gym = gymTmp;

            if (savedInstanceState == null) {
                openFragment(FragmentGymProfile.newInstance(this.gym), false);
                navigationView.setCheckedItem(R.id.nav_menu_home);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_gym_toolbar, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.system_toolbar_logout) {
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
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_menu_home && !item.isChecked()) {
            openFragment(FragmentGymProfile.newInstance(gym), false);
        }
        else if (item.getItemId() == R.id.nav_menu_setting && !item.isChecked()) {
            openFragment(FragmentGymSettings.newInstance(gym), true);
        }
        else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {
            openFragment(FragmentGymSubs.newInstance(gym), true);
        }
        else if (item.getItemId() == R.id.nav_menu_help && !item.isChecked()) {
            // TODO: help fragment
        } else if (item.getItemId() == R.id.nav_menu_logout) {
            finish();
        }

        this.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Take from Database all fields that will be add into layout XML file fields
     *
     * gymConfDBCallback Callback method that used for set Gym object in the current class
     */
    private void setInterface(GymConfDBCallback gymConfDBCallback) {

        this.db.collection("gyms").document(userUid).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;

                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");
                String address = documentSnapshot.getString("address");
                LatLng gymPosition = new LatLng(
                        Objects.requireNonNull(documentSnapshot.getGeoPoint("position")).getLatitude(),
                        Objects.requireNonNull(documentSnapshot.getGeoPoint("position")).getLongitude());
                String imageRef = documentSnapshot.getString("img");

                Gym gym = new Gym(userUid, email, phone, name, address, gymPosition, imageRef);
                gymConfDBCallback.onCallback(gym);

            } else {
                Log.d(ERROR_LOG, Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    /**
     * Set the current user auth for next uses with Database and Storage
     */
    private void setUserUid() {
        this.userUid = this.user.getUid();
    }

    private void openFragment(Fragment fragment, boolean isAddedToStack) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        if (isAddedToStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.fragment_container_view_tag, fragment).commit();
    }

}