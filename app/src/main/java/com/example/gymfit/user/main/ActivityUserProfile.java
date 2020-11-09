package com.example.gymfit.user.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

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
import android.widget.FrameLayout;


import com.example.gymfit.R;
import com.example.gymfit.system.conf.exception.NullDataException;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.example.gymfit.user.conf.InitUserCallback;
import com.example.gymfit.user.conf.User;
import com.example.gymfit.user.conf.UserViewPagerAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityUserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private String userUID;
    private User user;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private boolean isEmptyData = false;
    private final List<String> emptyData = new ArrayList<>();

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

            // Initialize Google Place API
            Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

            // Initialize User
            initUserID();
            initUserFromDatabase(userTmp -> {
                this.user = userTmp;
                initDatabaseFromEmpty();

                AppUtils.startFragment(this, FragmentUserProfile.newInstance(this.user, this.isEmptyData, (ArrayList<String>) this.emptyData), false);
                navigationView.setCheckedItem(R.id.nav_menu_home);
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
            AppUtils.startFragment(this, FragmentUserProfile.newInstance(this.user, this.isEmptyData, (ArrayList<String>) this.emptyData), false);
        } else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentUserMainTurn.newInstance(this.user), true);
        } else if (item.getItemId() == R.id.nav_menu_gyms && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentUserListGyms.newInstance(this.user), true);
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

        // Take data from User node
        this.db.collection("users").document(this.userUID).get()
                // the user is correct and there is the data
                .addOnCompleteListener(task -> {
                    User userTmp;

                    if(task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        try {
                            isEmptyDataFromDatabase(Objects.requireNonNull(documentSnapshot.getData()));
                            userTmp = initUser(documentSnapshot);
                        } catch(NullDataException e) {
                            AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                            userTmp = initUser(documentSnapshot);
                        }
                    } else {
                        AppUtils.log(Thread.currentThread().getStackTrace(), Objects.requireNonNull(task.getException()).getMessage());
                        userTmp = initUser();
                    }
                    initUserCallback.onCallback(userTmp);
                })
                // the user is correct but there isn't any data
                .addOnFailureListener(task -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), task.getMessage());

                    // init the User object with default values
                    this.user = initUser();
                    initUserCallback.onCallback(this.user);

                    // init with true to send a message at User after layout XML inflate
                    this.isEmptyData = true;
                });
    }

    /**
     * Init Database critical node with default values
     */
    private void initDatabaseFromEmpty() {
        String[] keys = getResources().getStringArray(R.array.user_field);

        this.emptyData.forEach(key -> {
            // If empty value is Image, init Storage and Database with another default
            if (key.equals(keys[8])) {
                StorageReference storageReference = this.storage.getReference().child("img/users/" + this.userUID + "/profilePic");
                storageReference.putFile(Uri.parse(ResourceUtils.getURIForResource(R.drawable.default_user)))
                        .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String uriString = uri.toString();
                            this.db.collection("users").document(this.userUID).update(key, uriString)
                                    .addOnSuccessListener(aVoid -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is added into Database"))
                                    .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is not added into Database"));

                            AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is added into Storage");
                        }))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is deleted"));
            }
            // If empty value is Date, init Database with another default
            else if (key.equals(keys[4])) {
                this.db.collection("users").document(this.userUID).update(key, new Timestamp(new Date()))
                        .addOnSuccessListener(aVoid -> AppUtils.log(Thread.currentThread().getStackTrace(), "Date is added into Database"))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Date is not added into Database"));
            }
        });

        this.emptyData.remove(keys[10]);
        this.emptyData.remove(keys[11]);
    }

    /**
     * Return a User object with a default values
     *
     * @return User object
     */
    @SuppressWarnings("unchecked")
    private User initUser() {
        Map<String, Object> data = new HashMap<>();
        final String[] userKeys = getResources().getStringArray(R.array.user_field);

        data.put(userKeys[0], this.userUID); //uid
        data.put(userKeys[1], "null"); // name
        data.put(userKeys[2], "null"); // surname
        data.put(userKeys[4], new Date()); // dateOfBirthday
        data.put(userKeys[5], "null"); // email
        data.put(userKeys[6], "null"); // gender
        data.put(userKeys[7], "null"); // phone
        data.put(userKeys[8], ResourceUtils.getURIForResource(R.drawable.default_user)); // img
        data.put(userKeys[9], "null"); // address
        data.put(userKeys[10], new String[] {"null", "null"}); // subscription
        data.put(userKeys[11], new ArrayList<Map<String, Object>>()); // turns

        return new User((String) data.get(userKeys[0]), (String) data.get(userKeys[1]), (String) data.get(userKeys[2]),
                (String) data.get(userKeys[5]), (Date) data.get(userKeys[4]), (String) data.get(userKeys[9]),
                (String) data.get(userKeys[6]), (String) data.get(userKeys[8]), (String) data.get(userKeys[7]),
                (String[]) data.get(userKeys[10]), (ArrayList<Map<String, Object>>) data.get(userKeys[11]));
    }

    /**
     * Get field from a DocumentSnapshot object and initialize a User object with them. When a field is null (contained into emptyData) set field with default value
     *
     * @param ds documentSnapshot used to get all field for User initialization
     * @return User object
     */
    @SuppressWarnings("unchecked")
    private User initUser(@NotNull DocumentSnapshot ds) {
        Map<String, Object> data = new HashMap<>();
        final String[] userKeys = getResources().getStringArray(R.array.user_field);

        data.put(userKeys[0], this.userUID);
        Objects.requireNonNull(ds.getData()).forEach((key, value) -> {
            if (this.emptyData.contains(key)) {
                AppUtils.log(Thread.currentThread().getStackTrace(), key + " is missing on Database");
                switch (key) {
                    case "dateOfBirthday":
                        data.put(key, new Date());
                        break;
                    case "img":
                        data.put(key, ResourceUtils.getURIForResource(R.drawable.default_user));
                        break;
                    case "subscription":
                        data.put(key, new String[] {"null", "null"});
                        break;
                    case "turns":
                        data.put(key, new ArrayList<Map<String, Object>>());
                        break;
                    default:
                        data.put(key, "null");
                        break;
                }
            } else {
                switch (key) {
                    case "dateOfBirthday":
                        data.put(userKeys[4], (ds.getTimestamp(userKeys[4])).toDate());
                        break;
                    case "img":
                        data.put(userKeys[8], ds.getString(userKeys[8]));
                        break;
                    case "subscription":
                        data.put(key, ((ArrayList<String>) ds.get(userKeys[10])).toArray(new String[0]));
                        break;
                    case "turns":
                        data.put(key, ds.get(userKeys[11]));
                        break;
                    default:
                        data.put(key, value);
                        break;
                }
            }
        });

        return new User((String) data.get(userKeys[0]), (String) data.get(userKeys[1]), (String) data.get(userKeys[2]),
                (String) data.get(userKeys[5]), (Date) data.get(userKeys[4]), (String) data.get(userKeys[9]),
                (String) data.get(userKeys[6]), (String) data.get(userKeys[8]), (String) data.get(userKeys[7]),
                (String[]) data.get(userKeys[10]), (ArrayList<Map<String, Object>>) data.get(userKeys[11]));
    }

    // Other methods

    /**
     * Check if the documentSnapshot has any empty field
     *
     * @param data DocumentSnapshot data gained from Database
     * @throws NullDataException Exception thrown to alert of Null object
     */
    private void isEmptyDataFromDatabase(@NonNull Map<String, Object> data) {
        this.emptyData.clear();
        AtomicBoolean flag = new AtomicBoolean(false);
        String[] userKeys = getResources().getStringArray(R.array.user_field);

        data.forEach((key, value) -> {
            if (value == null) {
                this.emptyData.add(key);
                flag.set(true);

                // (10) subscription, (11) turns
                if (!key.equals(userKeys[10]) && !key.equals(userKeys[11])) {
                    this.isEmptyData = true;
                }
            }
        });

        // if get boolean return true means that there are empty value into Gym node of Database.
        // So throw Exception and init a message to show at User after layout XML inflate
        if (flag.get()) {
            throw new NullDataException(this.emptyData);
        }
    }

}

