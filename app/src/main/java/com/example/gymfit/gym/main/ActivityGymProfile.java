package com.example.gymfit.gym.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.example.gymfit.system.main.FragmentSystemMainHelp;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityGymProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private String gymUID;
    private Gym gym;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private final List<String> emptyData = new ArrayList<>();
    private boolean isEmptyData = false;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Initialization interface
            initSystemInterface(Objects.requireNonNull(bundle.getString("uid")));

            // Initialize Gym
            DatabaseUtils.getGym(this.gymUID, ((data, result) -> {
                if (result == DatabaseUtils.RESULT_OK) {
                    this.gym = data;
                    this.emptyData.clear();
                    this.emptyData.addAll(initDatabaseFromEmpty());
                    this.isEmptyData = this.emptyData.isEmpty();
                    AppUtils.startFragment(this, FragmentGymProfile.newInstance(this.gym, this.isEmptyData, (ArrayList<String>) this.emptyData), false);
                    navigationView.setCheckedItem(R.id.nav_menu_home);
                }
            }));

            AppUtils.log(Thread.currentThread().getStackTrace(), "ActivityUserProfile created.");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_gym_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.system_toolbar_logout) {
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
    public boolean dispatchTouchEvent(@NonNull final MotionEvent event) {
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
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.nav_menu_home && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentGymProfile.newInstance(this.gym, this.isEmptyData, (ArrayList<String>) this.emptyData), false);
        } else if (item.getItemId() == R.id.nav_menu_setting && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentGymSettings.newInstance(this.gym), true);
        } else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentGymSubs.newInstance(this.gym), true);
        } else if (item.getItemId() == R.id.nav_menu_help && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentSystemMainHelp.newInstance(this.gym), true);
        } else if (item.getItemId() == R.id.nav_menu_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        this.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            try {
                @SuppressLint("InflateParams")
                View headerView = LayoutInflater.from(this).inflate(R.layout.layout_system_drawer_gym, null);
                this.navigationView.removeHeaderView(this.navigationView.getHeaderView(0));
                this.navigationView.addHeaderView(headerView);

                // Get view object
                MaterialTextView menuNameField = this.navigationView.getHeaderView(0).findViewById(R.id.header_gym_name);
                CircleImageView imageMenu = this.navigationView.getHeaderView(0).findViewById(R.id.header_gym_image);

                // Set view object
                menuNameField.setText(this.gym.getName());
                Picasso.get().load(this.gym.getImage()).into(imageMenu);

            } catch (Exception e) {
                AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                AppUtils.restartActivity(this);
            }
        }
    }

    // Set methods

    /**
     * Initialize toolbar, drawer, tab and current Gym logged uid
     *
     * @param uid current Gym uid used to get from database values and to init other fragments
     */
    private void initSystemInterface(@NonNull final String uid) {
        this.gymUID = uid;

        // Initialize Google Place API
        Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

        // Get Toolbar from layout XML and init it
        MaterialToolbar toolbar = findViewById(R.id.menu_gym_toolbar);
        setSupportActionBar(toolbar);

        // Get Drawer from layout XML and init it
        this.drawer = findViewById(R.id.drawer_gym);
        this.navigationView = findViewById(R.id.navigation_gym);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, this.drawer, toolbar, R.string.title_open_drawer, R.string.title_close_open_drawer);
        this.drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        this.navigationView.setNavigationItemSelectedListener(this);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of ActivityUserProfile initialized");
    }

    /**
     * Init Database critical node with default values
     */
    @NonNull
    private List<String> initDatabaseFromEmpty() {
        final String[] keys = getResources().getStringArray(R.array.gym_field);
        final List<String> emptyKeys = this.gym.getEmptyValues();

        emptyKeys.forEach(key -> {
            // If empty value is Image, init Storage and Database with another default
            if (key.equals(keys[3])) {
                StorageReference storageReference = this.storage.getReference().child("img/gyms/" + this.gymUID + "/profilePic");
                storageReference.putFile(Uri.parse(ResourceUtils.getURIForResource(R.drawable.default_user)))
                        .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String uriString = uri.toString();
                            DatabaseUtils.updateGymImg(this.gymUID, uriString, ((data, result) -> {}));
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is added into Storage");
                        }))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is deleted"));
            }
            // If empty value is Position or Address, init Database with another default
            else if (key.equals(keys[5]) || key.equals(keys[6])) {
                DatabaseUtils.updateGymPosition(this.gymUID, new LatLng(0, 0), ((data, result) -> {}));
            }
            // If empty value is Subscription, init Database with another default
            else if (key.equals(keys[7])) {
                DatabaseUtils.updateGymSubscriptions(this.gymUID, Gym.getDefaultGymSubscription(), ((data, result) -> {}));
            }
            // If empty value is Turns, init Database with another default
            else if (key.equals(keys[9])) {
                DatabaseUtils.updateGymTurns(this.gymUID, Gym.getDefaultGymDatabaseTurn(), ((data, result) -> {}));
            }
        });

        emptyKeys.remove(keys[7]);
        emptyKeys.remove(keys[8]);
        emptyKeys.remove(keys[9]);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Empty keys: " + emptyKeys.toString());
        return emptyKeys;
    }

}