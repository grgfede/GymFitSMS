package com.example.gymfit.gym.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymfit.R;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GymProfile extends AppCompatActivity implements OnMapReadyCallback {
    private static final String FIRE_LOG = "fire_log";

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

    // Save buttons
    private MaterialButton saveMailBtn = null;
    private MaterialButton saveKeyBtn = null;
    private MaterialButton savePhoneBtn = null;
    private MaterialButton saveAddressBtn = null;

    // Delete buttons
    private MaterialButton deleteMailBtn = null;
    private MaterialButton deleteKeyBtn = null;
    private MaterialButton deletePhoneBtn = null;
    private MaterialButton deleteAddressBtn = null;

    // Edit text field
    private TextInputLayout mailTextBox = null;
    private TextInputEditText mailTextField = null;
    private String localEmail = null;
    private TextInputLayout keyTextBox = null;
    private TextInputEditText keyTextField = null;
    private String localKey = null;
    private TextInputLayout phoneTextBox = null;
    private TextInputEditText phoneTextField = null;
    private String localPhone = null;
    private TextInputLayout addressTextBox = null;
    private TextInputEditText addressTextField = null;
    private LatLng localAddress = null;

    private String userUid = null;
    private Gym gym = null;
    private GoogleMap map = null;
    private boolean circleBtnClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

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

        // set save btn attr
        this.saveMailBtn = findViewById(R.id.gymSaveMail);
        this.saveKeyBtn = findViewById(R.id.gymSaveKey);
        this.savePhoneBtn = findViewById(R.id.gymSavePhone);
        this.saveAddressBtn = findViewById(R.id.gymSaveAddress);

        // set delete btn attr
        this.deleteMailBtn = findViewById(R.id.gymAbortMail);
        this.deleteKeyBtn = findViewById(R.id.gymAbortKey);
        this.deletePhoneBtn = findViewById(R.id.gymAbortPhone);
        this.deleteAddressBtn = findViewById(R.id.gymAbortAddress);

        // set text box
        this.mailTextBox = findViewById(R.id.gymBoxEmail);
        this.mailTextField = findViewById(R.id.gymTxtEmail);
        this.keyTextBox = findViewById(R.id.gymBoxKey);
        this.keyTextField = findViewById(R.id.gymTextKey);
        this.phoneTextBox = findViewById(R.id.gymBoxPhone);
        this.phoneTextField = findViewById(R.id.gymTextPhone);
        this.addressTextBox = findViewById(R.id.gymBoxAddress);
        this.addressTextField = findViewById(R.id.gymTextAddress);

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

        this.saveMailBtn.setOnClickListener(v -> this.user.updateEmail(Objects.requireNonNull(this.localEmail))
            .addOnSuccessListener(aVoid -> {
                this.db.collection("gyms").document(userUid).update(
                        "email", this.localEmail
                );
                this.gym.setEmail(this.localEmail);
                inputFieldDispatch(this.mailTextBox, this.mailTextField, this.localEmail, findViewById(R.id.gymEmailButtonRight));
                Toast.makeText(GymProfile.this, getResources().getString(R.string.update_email_success), Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                inputFieldDispatch(this.mailTextBox, this.mailTextField, this.gym.getEmail(), findViewById(R.id.gymEmailButtonRight));
                Toast.makeText(GymProfile.this, getResources().getString(R.string.update_email_error), Toast.LENGTH_SHORT).show();
            }));

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

        this.mailTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty() && !(s.toString().equals(gym.getEmail()))) {
                    localEmail = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /* Password comp event */

        this.deleteKeyBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.keyTextBox, this.keyTextField, getResources().getString(R.string.password_hide), findViewById(R.id.gymKeyButtonRight));
        });

        this.saveKeyBtn.setOnClickListener(v -> {
            this.user.updatePassword(this.localKey)
                .addOnSuccessListener(aVoid -> {
                    inputFieldDispatch(this.keyTextBox, this.keyTextField, this.localKey, findViewById(R.id.gymKeyButtonRight));
                    Toast.makeText(GymProfile.this, getResources().getString(R.string.update_password_success), Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    inputFieldDispatch(this.keyTextBox, this.keyTextField, getResources().getString(R.string.password_hide), findViewById(R.id.gymKeyButtonRight));
                    Toast.makeText(GymProfile.this, getResources().getString(R.string.update_password_error), Toast.LENGTH_LONG).show();
                });
        });

        this.keyTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.keyTextField.setText("");
        });

        this.keyTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.keyTextField.setText("");
        });

        this.keyTextField.setOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
        });

        this.keyTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.keyTextBox, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
                this.keyTextField.setText("");
            } else {
                inputFieldDispatch(this.keyTextBox, this.keyTextField, getResources().getString(R.string.password_hide), findViewById(R.id.gymKeyButtonRight));
                this.keyTextBox.clearFocus();
                this.keyTextField.clearFocus();
            }
        });

        this.keyTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty() && !s.toString().equals(getResources().getString(R.string.password_hide))) {
                    localKey = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /* Phone comp event */

        this.deletePhoneBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.gym.getPhone(), findViewById(R.id.gymPhoneButtonRight));
        });

        this.savePhoneBtn.setOnClickListener(v -> {
            if(isValidPhoneNumber(this.localPhone)) {
                this.db.collection("gyms").document(userUid).update(
                        "phone", this.localPhone
                );
                this.gym.setPhone(this.localPhone);
                inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.localPhone, findViewById(R.id.gymPhoneButtonRight));
                Toast.makeText(GymProfile.this, getResources().getString(R.string.update_phone_success), Toast.LENGTH_SHORT).show();
            } else {
                inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.gym.getPhone(), findViewById(R.id.gymPhoneButtonRight));
                Toast.makeText(GymProfile.this, getResources().getString(R.string.update_phone_error), Toast.LENGTH_LONG).show();
            }
        });

        this.phoneTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.phoneTextBox, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
            this.phoneTextField.setText("");
        });

        this.phoneTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.phoneTextBox, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
            this.phoneTextField.setText("");
        });

        this.phoneTextField.setOnClickListener(v -> {
            inputFieldFocused(this.phoneTextBox, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
        });

        this.phoneTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.phoneTextBox, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
                this.phoneTextField.setText("");
            } else {
                inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.gym.getPhone(), findViewById(R.id.gymPhoneButtonRight));
                this.phoneTextBox.clearFocus();
                this.phoneTextField.clearFocus();
            }
        });

        this.phoneTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty() && !(s.toString().equals(gym.getPhone()))) {
                    localPhone = s.toString();
                }

                if(s.toString().length() > phoneTextBox.getCounterMaxLength()) {
                    phoneTextBox.setError(getResources().getString(R.string.helper_phone_error));
                } else {
                    phoneTextBox.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /* Address comp event */

        this.deleteAddressBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.addressTextBox, this.addressTextField, this.gym.getAddressToString(), findViewById(R.id.gymAddressButtonRight));
        });

        this.saveAddressBtn.setOnClickListener(v -> {
            Toast.makeText(GymProfile.this, this.localAddress.toString(), Toast.LENGTH_SHORT).show();
        });

        this.addressTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.addressTextBox, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
            this.addressTextField.setText("");
        });

        this.addressTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.addressTextBox, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
            this.addressTextField.setText("");
        });

        this.addressTextField.setOnClickListener(v -> {
            inputFieldFocused(this.addressTextBox, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
        });

        this.addressTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.addressTextBox, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
                this.addressTextField.setText("");
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(GymProfile.this);
                startActivityForResult(intent, 100);
            } else {
                inputFieldDispatch(this.addressTextBox, this.addressTextField, this.gym.getAddressToString(), findViewById(R.id.gymAddressButtonRight));
                this.addressTextBox.clearFocus();
                this.addressTextField.clearFocus();
            }
        });

        this.addressTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            //When success
            //Initialize places
            assert data != null;
            Place place = Autocomplete.getPlaceFromIntent(data);
            this.addressTextField.setText(place.getAddress());
            this.localAddress = place.getLatLng();
        } else if(resultCode == AutocompleteActivity.RESULT_ERROR) {
            //When error
            //Initialize status
            assert data != null;
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(GymProfile.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        LatLng latLngUser = this.gym.getPosition();
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

                TextInputEditText gymEmailField = findViewById(R.id.gymTxtEmail);
                String email = documentSnapshot.getString("email");

                TextInputEditText gymPhoneField = findViewById(R.id.gymTextPhone);
                String phone = documentSnapshot.getString("phone");

                TextView gymAddressField = findViewById(R.id.gymTextAddress);
                HashMap<String, String> addressFields = new HashMap<>();
                addressFields.put("city", documentSnapshot.getString("address.city"));
                addressFields.put("country", documentSnapshot.getString("address.country"));
                addressFields.put("numberStreet", Objects.requireNonNull(documentSnapshot.get("address.numberStreet")).toString());
                addressFields.put("street", documentSnapshot.getString("address.street"));
                addressFields.put("zipCode", Objects.requireNonNull(documentSnapshot.get("address.zipCode")).toString());
                String address =
                        addressFields.get("street") + ", " +
                        addressFields.get("numberStreet") + ", " +
                        addressFields.get("zipCode") + " " + addressFields.get("city") + ", " +
                        addressFields.get("country");

                LatLng gymPosition = new LatLng(
                        Objects.requireNonNull(documentSnapshot.getGeoPoint("position")).getLatitude(),
                        Objects.requireNonNull(documentSnapshot.getGeoPoint("position")).getLongitude());

                final ImageView gymImgField = findViewById(R.id.gymImgField);
                //String imageRef = documentSnapshot.getString("img");
                //assert imageRef != null;
                StorageReference imageRef = storage.getReference().child("img/gyms/dota2.jpg");
                long MAXBYTES = 1024 * 1024;

                imageRef.getBytes(MAXBYTES).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    gymImgField.setImageBitmap(bitmap);
                }).addOnFailureListener(e -> Log.d(FIRE_LOG, "ERROR: " + e.getMessage()));

                Gym gymTmp = new Gym(userUid, email, phone, name, addressFields, gymPosition);
                gymDBCallback.onCallback(gymTmp);

                gymNameField.setText(name);
                gymEmailField.setText(email);
                gymPhoneField.setText(phone);
                gymAddressField.setText(address);

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

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void inputFieldDispatch(TextInputLayout box, TextInputEditText textField, String originText, LinearLayout container) {
        textField.setText(originText);
        textField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

        box.setEndIconDrawable(R.drawable.ic_edit);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, getTheme())));
        box.setHelperTextEnabled(false);

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0));
    }

    /*
        1. ^ start of expression
        2. (\\+\\d{1,3}( )?)? is optional match of country code between 1 to 3 digits prefixed with '+' symbol, followed by space or no space.
        3. ((\\(\\d{1,3}\\))|\\d{1,3} is mandatory group of 1 to 3 digits with or without parenthesis followed by hyphen, space or no space.
        4. \\d{3,4}[- .]? is mandatory group of 3 or 4 digits followed by hyphen, space or no space
        5. \\d{4} is mandatory group of last 4 digits
        6. $ end of expression
     */
    public boolean isValidPhoneNumber(String number) {
        String allCountryRegex = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
        if(number.matches(allCountryRegex)) {
            return number.length() <= this.phoneTextBox.getCounterMaxLength();
        } else {
            return false;
        }
    }

}