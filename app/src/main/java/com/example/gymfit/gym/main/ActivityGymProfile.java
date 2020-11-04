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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.InitGymCallback;
import com.example.gymfit.system.conf.exception.NullDataException;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityGymProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private String gymUID;
    private Gym gym;

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private boolean isEmptyData = false;
    private final List<String> emptyData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);

        try {
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

            // Initialize Google Place API
            Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

            // Initialize Gym
            initGymID();
            initGymFromDatabase(gymTmp -> {
                this.gym = gymTmp;
                initDatabaseFromEmpty();

                AppUtils.startFragment(this, FragmentGymProfile.newInstance(this.gym, this.isEmptyData, (ArrayList<String>) this.emptyData), false);
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
        getMenuInflater().inflate(R.menu.menu_gym_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
            AppUtils.startFragment(this, FragmentGymProfile.newInstance(this.gym, this.isEmptyData, (ArrayList<String>) this.emptyData), false);
        } else if (item.getItemId() == R.id.nav_menu_setting && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentGymSettings.newInstance(this.gym), true);
        } else if (item.getItemId() == R.id.nav_menu_subs && !item.isChecked()) {
            AppUtils.startFragment(this, FragmentGymSubs.newInstance(this.gym), true);
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
     * Take from Database all fields that will be add into layout XML file fields
     *
     * gymConfDBCallback Callback method that used for set Gym object in the current class
     */
    private void initGymFromDatabase(InitGymCallback initGymCallback) {

        // Take data from Gym node
        this.db.collection("gyms").document(this.gymUID).get()
                // the gym user is correct and there is the data
                .addOnCompleteListener(task -> {
                    Gym gym;

                    if(task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        try {
                            isEmptyDataFromDatabase(documentSnapshot.getData());
                            gym = initGym(documentSnapshot);
                        } catch(NullDataException e) {
                            AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                            gym = initGym(documentSnapshot, e.getEmptyData());
                        }
                    } else {
                        AppUtils.log(Thread.currentThread().getStackTrace(), task.getException().getMessage());
                        gym = initGym();
                    }
                    initGymCallback.onCallback(gym);
                })
                // the gym user is correct but there isn't any data
                .addOnFailureListener(task -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), task.getMessage());

                    // init the Gym object with default values
                    this.gym = initGym();
                    initGymCallback.onCallback(this.gym);

                    // init with true to send a message at User after layout XML inflate
                    this.isEmptyData = true;
                });
    }

    /**
     * Init Database critical node with default values
     */
    private void initDatabaseFromEmpty() {
        String[] keys = getResources().getStringArray(R.array.gym_field);
        String[] keysMorning = getResources().getStringArray(R.array.morning_session_name);
        String[] keysAfternoon = getResources().getStringArray(R.array.afternoon_session_name);
        String[] keysEvening = getResources().getStringArray(R.array.evening_session_name);

        this.emptyData.forEach(key -> {
            // If empty value is Image, init Storage and Database with another default
            if (key.equals(keys[3])) {
                StorageReference storageReference = this.storage.getReference().child("img/gyms/" + this.gymUID + "/profilePic");
                storageReference.putFile(Uri.parse(ResourceUtils.getURIForResource(R.drawable.default_user)))
                        .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String uriString = uri.toString();
                            this.db.collection("gyms").document(this.gymUID).update(key, uriString)
                                    .addOnSuccessListener(aVoid -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is added into Database"))
                                    .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is not added into Database"));

                            AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is added into Storage");
                        }))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Default image is deleted"));
            }
            // If empty value is Position or Address, init Database with another default
            else if (key.equals(keys[5]) || key.equals(keys[6])) {
                this.db.collection("gyms").document(this.gymUID).update(keys[6], new GeoPoint(0, 0))
                        .addOnSuccessListener(aVoid -> AppUtils.log(Thread.currentThread().getStackTrace(), "Position is added into Database"))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Position is not added into Database"));
            }
            // If empty value is Subscription, init Database with another default
            else if (key.equals(keys[7])) {
                Map<String, Boolean> subscriptions = new HashMap<String, Boolean>() {
                    {
                        put(keys[10], true);
                        put(keys[11], true);
                        put(keys[12], true);
                        put(keys[13], true);
                    }
                };

                this.db.collection("gyms").document(this.gymUID).update(key, subscriptions)
                        .addOnSuccessListener(aVoid -> AppUtils.log(Thread.currentThread().getStackTrace(), "Subscription is added into Database"))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Subscription is not added into Database"));
            }
            // If empty value is Turns, init Database with another default
            else if (key.equals(keys[9])) {
                Map<String, Map<String, Boolean>> turns = new HashMap<String, Map<String, Boolean>>(){
                    {
                        put(keys[14], new HashMap<String, Boolean>() {
                            {
                                put(keysMorning[0], true);
                                put(keysMorning[1], true);
                                put(keysMorning[2], true);
                            }
                        });
                        put(keys[15], new HashMap<String, Boolean>() {
                            {
                                put(keysAfternoon[0], true);
                                put(keysAfternoon[1], true);
                                put(keysAfternoon[2], true);
                            }
                        });
                        put(keys[16], new HashMap<String, Boolean>() {
                            {
                                put(keysEvening[0], true);
                                put(keysEvening[1], true);
                                put(keysEvening[2], true);
                            }
                        });
                    }
                };

                this.db.collection("gyms").document(this.gymUID).update(key, turns)
                        .addOnSuccessListener(aVoid -> AppUtils.log(Thread.currentThread().getStackTrace(), "Turn is added into Database"))
                        .addOnFailureListener(e -> AppUtils.log(Thread.currentThread().getStackTrace(), "Turn is not added into Database"));
            }
        });

        // this.emptyData.remove(keys[6]); position need reset if address or/and position are empty
        this.emptyData.remove(keys[7]);
        this.emptyData.remove(keys[8]);
        this.emptyData.remove(keys[9]);
    }

    /**
     * Set the current user auth for next uses with Database and Storage
     */
    private void initGymID() {
        this.gymUID = this.currentUser.getUid();
    }

    /**
     * Return a Gym object with a default values
     *
     * @return Gym object
     */
    @SuppressWarnings("unchecked")
    private Gym initGym() {
        Map<String, Object> data = new HashMap<>();

        data.put("userID", this.gymUID);
        data.put("name", "null");
        data.put("email", "null");
        data.put("phone", "null");
        data.put("address", "null");
        data.put("img", ResourceUtils.getURIForResource(R.drawable.default_user));
        data.put("position", new LatLng(0, 0));
        data.put("subscribers", new ArrayList<>());

        return new Gym((String) data.get("userID"), (String) data.get("email"), (String) data.get("phone"),
                (String) data.get("name"), (String) data.get("address"), (List<String>) data.get("subscribers"),
                (LatLng) data.get("position"), (String) data.get("img"));
    }

    /**
     * Get field from a DocumentSnapshot object and initialize a Gym object with them.
     *
     * @param ds documentSnapshot used to get all field for Gym initialization
     * @return Gym object
     */
    @SuppressWarnings("unchecked")
    private Gym initGym(@NotNull DocumentSnapshot ds) {
        Map<String, Object> data = new HashMap<>();

        data.put("userID", this.gymUID);
        data.put("name", ds.getString("name"));
        data.put("email", ds.getString("email"));
        data.put("phone", ds.getString("phone"));
        data.put("address", ds.getString("address"));
        data.put("img", ds.getString("img"));
        data.put("position", new LatLng(
                ds.getGeoPoint("position").getLatitude(),
                ds.getGeoPoint("position").getLongitude()));
        data.put("subscribers", new LinkedList<>(Arrays.asList(stringToArray(ds.get("subscribers").toString()))));

        return new Gym((String) data.get("userID"), (String) data.get("email"), (String) data.get("phone"),
                (String) data.get("name"), (String) data.get("address"), (List<String>) data.get("subscribers"),
                (LatLng) data.get("position"), (String) data.get("img"));
    }

    /**
     * Get field from a DocumentSnapshot object and initialize a Gym object with them. When a field is null (contained into emptyData) set field with default value
     *
     * @param ds documentSnapshot used to get all field for Gym initialization
     * @param emptyData list of String that contains field key of Gym with empty into Database
     * @return Gym object
     */
    @SuppressWarnings("unchecked")
    private Gym initGym(@NotNull DocumentSnapshot ds, List<String> emptyData) {
        Map<String, Object> data = new HashMap<>();

        data.put("userID", this.gymUID);
        ds.getData().forEach((key, value) -> {
            if (emptyData.contains(key)) {
                AppUtils.log(Thread.currentThread().getStackTrace(), key + " is missing on Database");

                switch (key) {
                    case "position":
                        data.put(key, new LatLng(0, 0));
                        break;
                    case "subscribers":
                        data.put(key, new ArrayList<>());
                        break;
                    case "img":
                        data.put(key, ResourceUtils.getURIForResource(R.drawable.default_user));
                        break;
                    default:
                        data.put(key, "null");
                        break;
                }
            } else {
                switch (key) {
                    case "position":
                        data.put("position", new LatLng(
                                ds.getGeoPoint("position").getLatitude(),
                                ds.getGeoPoint("position").getLongitude()));
                        break;
                    case "subscribers":
                        data.put("subscribers", new LinkedList<>(Arrays.asList(stringToArray(ds.get("subscribers").toString()))));
                        break;
                    default:
                        data.put(key, value);
                        break;
                }
            }
        });

        return new Gym((String) data.get("userID"), (String) data.get("email"), (String) data.get("phone"),
                (String) data.get("name"), (String) data.get("address"), (List<String>) data.get("subscribers"),
                (LatLng) data.get("position"), (String) data.get("img"));
    }

    // Other methods

    /**
     * Check if the documentSnapshot has any empty field
     *
     * @param data DocumentSnapshot data gained from Database
     * @throws NullDataException Exception thrown to alert of Null object
     */
    private void isEmptyDataFromDatabase(Map<String, Object> data) throws NullDataException {
        this.emptyData.clear();
        AtomicBoolean flag = new AtomicBoolean(false);
        String[] keys = getResources().getStringArray(R.array.gym_field);

        data.forEach((key, value) -> {
            if (value == null) {
                this.emptyData.add(key);
                flag.set(true);

                // (7) subscription, (8) subscribers, (9) turn
                if (!key.equals(keys[7]) && !key.equals(keys[8]) && !key.equals(keys[9])) {
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

    /**
     * This methods transform a String into array of String.
     * To obtain it the origin string is cut into substring without first and last character because they are represented with "[,]".
     * Finally, from substring is deleted all whitespaces using Apache method.
     *
     * @param str origin string that will be used for get and return its respective array
     * @return array of strings obtained from origin string
     */
    private String[] stringToArray(String str) {
        str = str.substring(1, str.length()-1);
        str = StringUtils.deleteWhitespace(str);
        return str.split(",");
    }

}