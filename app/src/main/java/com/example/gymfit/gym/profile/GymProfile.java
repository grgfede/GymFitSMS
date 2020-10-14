package com.example.gymfit.gym.profile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymfit.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

public class GymProfile extends AppCompatActivity implements OnMapReadyCallback {
    private static final String FIRE_LOG = "fire_log";
    private static final String SYSTEM_LOG = "sys_log";

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Animation rotateOpen = null;
    private Animation rotateClose = null;
    private Animation fromButton = null;
    private Animation toButton = null;

    // Circle menu attr
    private FloatingActionButton fab = null;
    private FloatingActionButton fabSubscription = null;
    private FloatingActionButton fabRound = null;

    // Edit buttons
    //private ImageView editKeyBtn = null;
    private ImageView editPhoneBtn = null;
    private ImageView editAddressBtn = null;

    // Save buttons
    private ShapeableImageView saveMailBtn = null;
    private ShapeableImageView saveKeyBtn = null;

    // Delete buttons
    private ShapeableImageView deleteMailBtn = null;
    private ShapeableImageView deleteKeyBtn = null;
    private ImageView deletePhoneBtn = null;
    private ImageView deleteAddressBtn = null;

    // Edit text field
    private TextInputLayout mailTextBox = null;
    private TextInputEditText mailTextField = null;
    private TextInputLayout keyTextBox = null;
    private TextInputEditText keyTextField = null;
    private TextView phoneTextField = null;
    private TextView addressTextField = null;

    private String userUid = null;
    private Gym gym = null;
    private GoogleMap map = null;
    private boolean circleBtnClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);

        setUserUid();
        setGymInterface(gymTmp -> {
            gym = gymTmp;

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gymMapField);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);

        });

        // set circle menu
        this.fab = findViewById(R.id.fab_add);
        this.fabSubscription = findViewById(R.id.fab_subscription);
        this.fabRound = findViewById(R.id.fab_round);

        // set edit btn attr
        this.editPhoneBtn = findViewById(R.id.editPhoneGymButton);
        this.editAddressBtn = findViewById(R.id.editAddressGymButton);

        // set save btn attr
        this.saveMailBtn = findViewById(R.id.gymSaveMail);
        this.saveKeyBtn = findViewById(R.id.gymSaveKey);

        // set delete btn attr
        this.deleteMailBtn = findViewById(R.id.gymAbortMail);
        this.deleteKeyBtn = findViewById(R.id.gymAbortKey);
        this.deletePhoneBtn = findViewById(R.id.abortPhoneGymButton);
        this.deleteAddressBtn = findViewById(R.id.abortAddressGymButton);

        // set text box
        this.mailTextBox = findViewById(R.id.gymBoxEmail);
        this.mailTextField = findViewById(R.id.gymTxtEmail);
        this.keyTextBox = findViewById(R.id.gymBoxKey);
        this.keyTextField = findViewById(R.id.gymTextKey);
        this.phoneTextField = findViewById(R.id.gymPhoneField);
        this.addressTextField = findViewById(R.id.gymAddressField);

        // set animation
        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromButton = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toButton = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        /* Circle comp event */

        this.fab.setOnClickListener(view -> onAddButtons());

        this.fabSubscription.setOnClickListener(view -> {
            //TODO: Open GymSubscription activity
            Toast.makeText(GymProfile.this, "Subscription Opt", Toast.LENGTH_SHORT).show();
        });

        this.fabRound.setOnClickListener(view -> {
            //TODO: Open GymRound activity
            Toast.makeText(GymProfile.this, "Round Opt", Toast.LENGTH_SHORT).show();
        });

        /* Email comp event */

        this.deleteMailBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.mailTextBox, this.mailTextField, this.gym.getEmail(), findViewById(R.id.gymEmailButtonRight));
        });

        this.saveMailBtn.setOnClickListener(v -> {
            this.user.updateEmail(Objects.requireNonNull(this.mailTextField.getText()).toString())
                .addOnSuccessListener(aVoid -> {
                    this.db.collection("gyms").document(userUid).update(
                            "email", this.mailTextField.getText().toString()
                    );
                    Toast.makeText(GymProfile.this, getResources().getString(R.string.update_email_success), Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    this.mailTextField.setText(this.gym.getEmail());
                    Toast.makeText(GymProfile.this, getResources().getString(R.string.update_email_error), Toast.LENGTH_SHORT).show();
                });

            this.mailTextField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

            this.mailTextBox.setEndIconDrawable(R.drawable.ic_edit);
            this.mailTextBox.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, getTheme())));
            this.mailTextBox.setHelperTextEnabled(false);
            this.mailTextBox.setHintEnabled(false);

            this.saveMailBtn.setVisibility(View.INVISIBLE);
            this.deleteMailBtn.setVisibility(View.INVISIBLE);
        });

        this.mailTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.mailTextBox, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
            this.mailTextField.setText("");
        });

        this.mailTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.mailTextBox, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
            this.mailTextField.setText("");
        });

        this.mailTextField.setOnClickListener(v -> {
            inputFieldFocused(this.mailTextBox, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
        });

        this.mailTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.mailTextBox, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
                this.mailTextField.setText("");
            } else {
                inputFieldDispatch(this.mailTextBox, this.mailTextField, this.gym.getEmail(), findViewById(R.id.gymEmailButtonRight));
                this.mailTextBox.clearFocus();
                this.mailTextField.clearFocus();
            }
        });

        /* Password comp event */

        this.deleteKeyBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.mailTextBox, this.mailTextField, getResources().getString(R.string.password_hide), findViewById(R.id.gymKeyButtonRight));

        });

        this.saveKeyBtn.setOnClickListener(v -> {
            this.user.updatePassword(Objects.requireNonNull(this.keyTextField.getText()).toString())
                .addOnSuccessListener(aVoid -> {
                    this.keyTextField.setText(getResources().getString(R.string.password_hide));
                    Toast.makeText(GymProfile.this, getResources().getString(R.string.update_password_success), Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    this.keyTextField.setText(getResources().getString(R.string.password_hide));
                    Toast.makeText(GymProfile.this, getResources().getString(R.string.update_password_error), Toast.LENGTH_LONG).show();
                });

            this.keyTextField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

            this.keyTextBox.setEndIconDrawable(R.drawable.ic_edit);
            this.keyTextBox.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, getTheme())));
            this.keyTextBox.setHelperTextEnabled(false);
            this.keyTextBox.setHintEnabled(false);

            this.saveKeyBtn.setVisibility(View.INVISIBLE);
            this.deleteKeyBtn.setVisibility(View.INVISIBLE);
        });

        this.keyTextBox.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
                this.keyTextField.setText("");
            } else {
                this.keyTextBox.clearFocus();
                this.keyTextField.clearFocus();
            }
        });

        this.keyTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.saveKeyBtn.setVisibility(View.VISIBLE);
            this.deleteKeyBtn.setVisibility(View.VISIBLE);
            this.keyTextField.setText("");
        });

        this.keyTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.saveKeyBtn.setVisibility(View.VISIBLE);
            this.deleteKeyBtn.setVisibility(View.VISIBLE);
            this.keyTextField.setText("");
        });

        this.keyTextField.setOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.saveKeyBtn.setVisibility(View.VISIBLE);
            this.deleteKeyBtn.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        LatLng latLngUser = new LatLng(Double.parseDouble(Objects.requireNonNull(gym.getAddress().get("latitude"))), Double.parseDouble(Objects.requireNonNull(gym.getAddress().get("longitude"))));
        this.map.addMarker(new MarkerOptions().position(latLngUser)).setTitle(gym.getName());
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngUser, 15));
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

    private void onAddButtons() {
        setVisibility(circleBtnClicked);
        setAnimation(circleBtnClicked);
        setCircleBtnClickable(circleBtnClicked);
        circleBtnClicked = !circleBtnClicked;
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

    private void setCircleBtnClickable(boolean circleBtnClicked) {
        if(!circleBtnClicked) {
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

                TextInputEditText gymEmailField = findViewById(R.id.gymTxtEmail);
                String email = documentSnapshot.getString("email");
                gymEmailField.setText(email);

                TextView gymPhoneField = findViewById(R.id.gymPhoneField);
                String phone = Objects.requireNonNull(documentSnapshot.get("phone")).toString();
                gymPhoneField.setText(phone);

                TextView gymAddressField = findViewById(R.id.gymAddressField);
                HashMap<String, String> addressFields = new HashMap<>();
                addressFields.put("city", documentSnapshot.getString("address.city"));
                addressFields.put("country", documentSnapshot.getString("address.country"));
                addressFields.put("numberStreet", Objects.requireNonNull(documentSnapshot.get("address.numberStreet")).toString());
                addressFields.put("street", documentSnapshot.getString("address.street"));
                addressFields.put("zipCode", Objects.requireNonNull(documentSnapshot.get("address.zipCode")).toString());
                addressFields.put("latitude", Objects.requireNonNull(documentSnapshot.get("address.latitude")).toString());
                addressFields.put("longitude", Objects.requireNonNull(documentSnapshot.get("address.longitude")).toString());
                String address = addressFields.get("street") + " " + addressFields.get("numberStreet") + ", " +
                                addressFields.get("city");
                gymAddressField.setText(address);

                final ImageView gymImgField = findViewById(R.id.gymImgField);
                //String imageRef = documentSnapshot.getString("img");
                //assert imageRef != null;
                StorageReference imageRef = storage.getReference().child("img/gyms/dota2.jpg");
                long MAXBYTES = 1024 * 1024;

                imageRef.getBytes(MAXBYTES).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    gymImgField.setImageBitmap(bitmap);
                }).addOnFailureListener(e -> Log.d(FIRE_LOG, "ERROR: " + e.getMessage()));

                Gym gymTmp = new Gym(userUid, email, phone, name, addressFields);
                gymDBCallback.onCallback(gymTmp);

            } else {
                Log.d(FIRE_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    private void inputFieldFocused(TextInputLayout box, String helperText, LinearLayout container) {
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

        box.setHelperTextEnabled(true);
        box.setHelperText(helperText);
        box.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, getTheme())));

        container.setVisibility(View.VISIBLE);
    }

    private void inputFieldDispatch(TextInputLayout box, TextInputEditText textField, String originText, LinearLayout container) {
        textField.setText(originText);
        textField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

        box.setEndIconDrawable(R.drawable.ic_edit);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, getTheme())));
        box.setHelperTextEnabled(false);
;
        container.setVisibility(View.INVISIBLE);
    }

}