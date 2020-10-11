package com.example.gymfit.gym.profile;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymfit.R;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Objects;

public class GymProfile extends AppCompatActivity {
    private static final String FIRE_LOG = "fire_log";

    // Get and set Firebase DB/Storage
    private static final StorageReference _FIREBASE_STORAGE_REF = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromButton;
    private Animation toButton;

    private FloatingActionButton fab;
    private FloatingActionButton fabSubscription;
    private FloatingActionButton fabRound;

    private String userUid = null;
    private Gym gym;
    private boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);
        setUserUid();
        setGymInterface(new GymDBCallback() {
            @Override
            public void onCallback(Gym gymTmp) {
                gym = gymTmp;
            }
        });

        _FIREBASE_STORAGE_REF.child("img/gyms/dota2.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(FIRE_LOG, "INFO: " + uri.getPath());
                //ImageView imageView = findViewById(R.id.gymImageField);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    //imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Log.d(FIRE_LOG, "ERROR: " + e.getMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(FIRE_LOG, "ERROR: " + e.getMessage());
            }
        });

        this.fab = findViewById(R.id.fab_add);
        this.fabSubscription = findViewById(R.id.fab_subscription);
        this.fabRound = findViewById(R.id.fab_round);

        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromButton = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toButton = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        this.fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onAddButtons();
            }
        });

        this.fabSubscription.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //TODO: Open GymSubscription activity
                Toast.makeText(GymProfile.this, "Subscription Opt", Toast.LENGTH_SHORT).show();
            }
        });

        this.fabRound.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //TODO: Open GymRound activity
                Toast.makeText(GymProfile.this, "Round Opt", Toast.LENGTH_SHORT).show();
            }
        });

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

        this.db.collection("gyms").document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

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

                    Gym gymTmp = new Gym(userUid, email, phone, name, addressFields);
                    gymDBCallback.onCallback(gymTmp);

                } else {
                    Log.d(FIRE_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });
    }

}