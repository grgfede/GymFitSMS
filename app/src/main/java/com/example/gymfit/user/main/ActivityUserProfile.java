package com.example.gymfit.user.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.example.gymfit.system.main.FragmentSystemMainHelp;
import com.example.gymfit.user.conf.User;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityUserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private String userUID;
    private User user;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private final List<String> emptyData = new ArrayList<>();
    private boolean isEmptyData = false;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        this.preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Initialization interface
            initSystemInterface(Objects.requireNonNull(bundle.getString("uid")));

            // Initialize User
            DatabaseUtils.getUser(this.userUID, ((data, result) -> {
                if (result == DatabaseUtils.RESULT_OK) {
                    this.user = data;
                    this.emptyData.clear();
                    this.emptyData.addAll(initDatabaseFromEmpty());
                    this.isEmptyData = this.emptyData.isEmpty();
                    AppUtils.startFragment(this, FragmentUserProfile.newInstance(this.user, !this.isEmptyData, (ArrayList<String>) this.emptyData), false);
                    navigationView.setCheckedItem(R.id.nav_menu_home);
                }
            }));

            AppUtils.log(Thread.currentThread().getStackTrace(), "ActivityUserProfile created.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_user_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.system_toolbar_logout){
            FirebaseAuth.getInstance().signOut();
            this.preferences.edit().putString("uid", null).apply();
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
            AppUtils.startFragment(this, FragmentUserProfile.newInstance(this.user, !this.isEmptyData, (ArrayList<String>) this.emptyData), false);
        } else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentUserMainTurn.newInstance(this.user), true);
        } else if (item.getItemId() == R.id.nav_menu_gyms && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentUserListGyms.newInstance(this.user), true);
        } else if (item.getItemId() == R.id.nav_menu_help && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentSystemMainHelp.newInstance(this.user), true);
        } else if (item.getItemId() == R.id.nav_menu_logout) {
            FirebaseAuth.getInstance().signOut();
            this.preferences.edit().putString("uid", null).apply();
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
                View headerView = LayoutInflater.from(this).inflate(R.layout.layout_system_drawer_user, null);
                this.navigationView.removeHeaderView(this.navigationView.getHeaderView(0));
                this.navigationView.addHeaderView(headerView);

                // Get view object
                MaterialTextView menuNameField = this.navigationView.getHeaderView(0).findViewById(R.id.header_user_name);
                CircleImageView imageMenu = this.navigationView.getHeaderView(0).findViewById(R.id.header_user_image);

                // Set view object
                menuNameField.setText(this.user.getFullname());
                Picasso.get().load(this.user.getImg()).into(imageMenu);

            } catch (Exception e) {
                AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                AppUtils.restartActivity(this);
            }
        }
    }

    // Set methods

    /**
     * Initialize toolbar, drawer, tab and current User logged uid
     *
     * @param uid current User uid used to get from database values and to init other fragments
     */
    private void initSystemInterface(@NonNull final String uid) {
        this.userUID = uid;

        // Initialize Google Place API
        Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

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

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of ActivityUserProfile initialized");
    }

    /**
     * Init Database critical node with default values
     */
    @NonNull
    private List<String> initDatabaseFromEmpty() {
        final String[] keys = getResources().getStringArray(R.array.user_field);
        final List<String> emptyKeys = this.user.getEmptyValues();

        emptyKeys.forEach(key -> {
            // If empty value is Image, init Storage and Database with another default
            if (key.equals(keys[8])) {
                StorageReference storageReference = this.storage.getReference().child("img/users/" + this.userUID + "/profilePic");
                storageReference.putFile(Uri.parse(ResourceUtils.getURIForResource(R.drawable.default_user)))
                        .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String uriString = uri.toString();
                            DatabaseUtils.updateUserImg(this.userUID, uriString, ((data, result) -> {}));
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is added into Storage");
                        }))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is deleted"));
            }
            // If empty value is Date, init Database with another default
            else if (key.equals(keys[4])) {
                DatabaseUtils.updateUserDateOfBirthday(this.userUID, new Date(), ((data, result) -> {}));
            }
        });

        emptyKeys.remove(keys[4]);
        emptyKeys.remove(keys[6]);
        emptyKeys.remove(keys[10]);
        emptyKeys.remove(keys[11]);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Empty keys: " + emptyKeys.toString());
        return emptyKeys;
    }

}

