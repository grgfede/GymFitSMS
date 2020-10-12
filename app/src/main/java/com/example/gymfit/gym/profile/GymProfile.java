package com.example.gymfit.gym.profile;

import android.app.AppComponentFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gymfit.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

public class GymProfile extends AppCompatActivity implements OnMapReadyCallback {
    private static final String FIRE_LOG = "fire_log";

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromButton;
    private Animation toButton;

    private FloatingActionButton fab;
    private FloatingActionButton fabSubscription;
    private FloatingActionButton fabRound;

    private String userUid = null;
    private Gym gym = null;
    private GoogleMap map = null;
    private boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gymMapField);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        setUserUid();
        setGymInterface(gymTmp -> gym = gymTmp);

        this.fab = findViewById(R.id.fab_add);
        this.fabSubscription = findViewById(R.id.fab_subscription);
        this.fabRound = findViewById(R.id.fab_round);

        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromButton = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toButton = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        this.fab.setOnClickListener(view -> onAddButtons());

        this.fabSubscription.setOnClickListener(view -> {
            //TODO: Open GymSubscription activity
            Toast.makeText(GymProfile.this, "Subscription Opt", Toast.LENGTH_SHORT).show();
        });

        this.fabRound.setOnClickListener(view -> {
            //TODO: Open GymRound activity
            Toast.makeText(GymProfile.this, "Round Opt", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        LatLng latLngUser = new LatLng(41.051477, 16.698150);
        this.map.addMarker(new MarkerOptions().position(latLngUser));
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngUser, 15));
    }

    private void onAddButtons() {
        setVisibility(clicked);
        setAnimation(clicked);
        setClickable(clicked);
        clicked = !clicked;
    }

    private void setAnimation(boolean clicked) {
        if(!clicked) {
            this.fabSubscription.setVisibility(View.VISIBLE);
            this.fabRound.setVisibility(View.VISIBLE);
        } else {
            this.fabSubscription.setVisibility(View.INVISIBLE);
            this.fabRound.setVisibility(View.INVISIBLE);
        }
    }

    private void setVisibility(boolean clicked) {
        if(!clicked) {
            this.fabSubscription.startAnimation(this.fromButton);
            this.fabRound.startAnimation(this.fromButton);
            this.fab.startAnimation(this.rotateOpen);
        } else {
            this.fabSubscription.startAnimation(this.toButton);
            this.fabRound.startAnimation(this.toButton);
            this.fab.startAnimation(this.rotateClose);
        }
    }

    private void setClickable(boolean clicked) {
        if(!clicked) {
            this.fabSubscription.setClickable(true);
            this.fabRound.setClickable(true);
        } else {
            this.fabSubscription.setClickable(false);
            this.fabRound.setClickable(false);
        }
    }

    private void setUserUid() {
        this.userUid = getIntent().getStringExtra("userUid");
    }

    private void setGymInterface(final GymDBCallback gymDBCallback) {

        this.db.collection("gyms").document(userUid).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;

                TextView gymNameField = findViewById(R.id.gymNameField);
                String name = documentSnapshot.getString("name");
                gymNameField.setText(name);

                TextView gymEmailField = findViewById(R.id.gymEmailField);
                String email = documentSnapshot.getString("email");
                gymEmailField.setText(email);

                TextView gymPhoneField = findViewById(R.id.gymPhoneField);
                String phone = Objects.requireNonNull(documentSnapshot.get("phone")).toString();
                gymPhoneField.setText(phone);

                TextView gymAddressField = findViewById(R.id.gymAddressField);
                HashMap<String, Object> addressFields = new HashMap<>();
                addressFields.put("city", documentSnapshot.getString("address.city"));
                addressFields.put("country", documentSnapshot.getString("address.country"));
                addressFields.put("numberStreet", Objects.requireNonNull(documentSnapshot.get("address.numberStreet")).toString());
                addressFields.put("street", documentSnapshot.getString("address.street"));
                addressFields.put("zipCode", Objects.requireNonNull(documentSnapshot.get("address.zipCode")).toString());
                String address = addressFields.get("street") + " " + addressFields.get("numberStreet") + ", " +
                                addressFields.get("city");
                gymAddressField.setText(address);

                //TODO: get it from db
                final ImageView gymImgField = findViewById(R.id.gymImgField);
                StorageReference imageRef = storage.getReference().child("img/gyms/dota2.jpg");
                long MAXBYTES = 1024 * 1024;

                imageRef.getBytes(MAXBYTES).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    gymImgField.setImageBitmap(bitmap);
                }).addOnFailureListener(e -> Log.d(FIRE_LOG, "ERROR"));

                Gym gymTmp = new Gym(userUid, email, phone, name, addressFields);
                gymDBCallback.onCallback(gymTmp);

            } else {
                Log.d(FIRE_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

}