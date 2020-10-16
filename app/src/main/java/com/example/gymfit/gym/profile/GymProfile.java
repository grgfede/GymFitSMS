package com.example.gymfit.gym.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
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

import androidx.annotation.NonNull;
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
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GymProfile extends AppCompatActivity implements OnMapReadyCallback {
    private static final String FIRE_LOG = "fire_log";
    private static final int MY_ADDRESS_REQUEST_CODE = 100, MY_CAMERA_REQUEST_CODE = 10, MY_GALLERY_REQUEST_CODE = 11;
    private static final int MY_CAMERA_PERMISSION_CODE = 9;


    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Animation rotateOpen = null;
    private Animation rotateClose = null;
    private Animation fromButton = null;
    private Animation toButton = null;

    // Image
    private ImageView gymImg = null;
    private CircleImageView gymImgName = null;

    // Circle menu attr
    private FloatingActionButton fab = null;
    private FloatingActionButton fabSubscription = null;
    private FloatingActionButton fabRound = null;
    private FloatingActionButton editImage = null;

    // Save buttons
    private MaterialButton saveMailBtn = null;
    private MaterialButton saveKeyBtn = null;
    private MaterialButton savePhoneBtn = null;
    private MaterialButton saveAddressBtn = null;
    private MaterialButton saveNameBtn = null;

    // Delete buttons
    private MaterialButton deleteMailBtn = null;
    private MaterialButton deleteKeyBtn = null;
    private MaterialButton deletePhoneBtn = null;
    private MaterialButton deleteAddressBtn = null;
    private MaterialButton deleteNameBtn = null;

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
    private String localAddress = null;
    private LatLng localPosition = null;
    private TextInputLayout nameTextBox = null;
    private TextInputEditText nameTextField = null;
    private String localName = null;

    private String userUid = null;
    private View activityView = null;
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

        this.activityView = findViewById(R.id.constraintLayout);

        // set image
        this.gymImg = findViewById(R.id.gymImgField);
        this.gymImgName = findViewById(R.id.gymImgName);

        // set circle menu
        this.fab = findViewById(R.id.fab_add);
        this.fabSubscription = findViewById(R.id.fab_subscription);
        this.fabRound = findViewById(R.id.fab_round);
        this.editImage = findViewById(R.id.gymEditImg);

        // set save btn attr
        this.saveMailBtn = findViewById(R.id.gymSaveMail);
        this.saveKeyBtn = findViewById(R.id.gymSaveKey);
        this.savePhoneBtn = findViewById(R.id.gymSavePhone);
        this.saveAddressBtn = findViewById(R.id.gymSaveAddress);
        this.saveNameBtn = findViewById(R.id.gymSaveName);

        // set delete btn attr
        this.deleteMailBtn = findViewById(R.id.gymAbortMail);
        this.deleteKeyBtn = findViewById(R.id.gymAbortKey);
        this.deletePhoneBtn = findViewById(R.id.gymAbortPhone);
        this.deleteAddressBtn = findViewById(R.id.gymAbortAddress);
        this.deleteNameBtn = findViewById(R.id.gymAbortName);

        // set text box
        this.mailTextBox = findViewById(R.id.gymBoxEmail);
        this.mailTextField = findViewById(R.id.gymTxtEmail);
        this.keyTextBox = findViewById(R.id.gymBoxKey);
        this.keyTextField = findViewById(R.id.gymTextKey);
        this.phoneTextBox = findViewById(R.id.gymBoxPhone);
        this.phoneTextField = findViewById(R.id.gymTextPhone);
        this.addressTextBox = findViewById(R.id.gymBoxAddress);
        this.addressTextField = findViewById(R.id.gymTextAddress);
        this.nameTextBox = findViewById(R.id.gymBoxName);
        this.nameTextField = findViewById(R.id.gymTxtName);

        // set animation
        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromButton = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toButton = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        /* Circle comp event */

        this.fab.setOnClickListener(view -> onAddButtons());

        this.fabSubscription.setOnClickListener(view -> {
            //TODO: Open GymSubscription activity
            Snackbar.make(activityView, "Subscription Opt", Snackbar.LENGTH_SHORT).show();
        });

        this.fabRound.setOnClickListener(view -> {
            //TODO: Open GymRound activity
            Snackbar.make(activityView, "Turn Opt", Snackbar.LENGTH_SHORT).show();;
        });

        /* Email comp event */

        this.deleteMailBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.mailTextBox, this.mailTextField, this.gym.getEmail(), false, findViewById(R.id.gymEmailButtonRight));
        });

        this.saveMailBtn.setOnClickListener(v -> this.user.updateEmail(Objects.requireNonNull(this.localEmail))
            .addOnSuccessListener(aVoid -> {
                this.db.collection("gyms").document(userUid).update(
                        "email", this.localEmail
                );
                this.gym.setEmail(this.localEmail);
                inputFieldDispatch(this.mailTextBox, this.mailTextField, this.localEmail, false, findViewById(R.id.gymEmailButtonRight));
                Snackbar.make(activityView, getResources().getString(R.string.update_email_success), Snackbar.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                inputFieldDispatch(this.mailTextBox, this.mailTextField, this.gym.getEmail(), false, findViewById(R.id.gymEmailButtonRight));
                Snackbar.make(activityView, getResources().getString(R.string.update_email_error), Snackbar.LENGTH_SHORT).show();
            }));

        this.mailTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.mailTextBox, this.mailTextField, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
            this.mailTextField.setText("");
        });

        this.mailTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.mailTextBox, this.mailTextField, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
            this.mailTextField.setText("");
        });

        this.mailTextField.setOnClickListener(v -> {
            inputFieldFocused(this.mailTextBox, this.mailTextField, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
        });

        this.mailTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.mailTextBox, this.mailTextField, getResources().getString(R.string.helper_email_hover), findViewById(R.id.gymEmailButtonRight));
                this.mailTextField.setText("");
            } else {
                inputFieldDispatch(this.mailTextBox, this.mailTextField, this.gym.getEmail(), false, findViewById(R.id.gymEmailButtonRight));
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
            inputFieldDispatch(this.keyTextBox, this.keyTextField, getResources().getString(R.string.password_hide), false, findViewById(R.id.gymKeyButtonRight));
        });

        this.saveKeyBtn.setOnClickListener(v -> {
            this.user.updatePassword(this.localKey)
                .addOnSuccessListener(aVoid -> {
                    inputFieldDispatch(this.keyTextBox, this.keyTextField, this.localKey, false, findViewById(R.id.gymKeyButtonRight));
                    Snackbar.make(activityView, getResources().getString(R.string.update_password_success), Snackbar.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    inputFieldDispatch(this.keyTextBox, this.keyTextField, getResources().getString(R.string.password_hide), false, findViewById(R.id.gymKeyButtonRight));
                    Snackbar.make(activityView, getResources().getString(R.string.update_password_error), Snackbar.LENGTH_SHORT).show();
                });
        });

        this.keyTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, this.keyTextField, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.keyTextField.setText("");
        });

        this.keyTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, this.keyTextField, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
            this.keyTextField.setText("");
        });

        this.keyTextField.setOnClickListener(v -> {
            inputFieldFocused(this.keyTextBox, this.keyTextField, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
        });

        this.keyTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.keyTextBox, this.keyTextField, getResources().getString(R.string.helper_psw_hover), findViewById(R.id.gymKeyButtonRight));
                this.keyTextField.setText("");
            } else {
                inputFieldDispatch(this.keyTextBox, this.keyTextField, getResources().getString(R.string.password_hide), false, findViewById(R.id.gymKeyButtonRight));
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
            inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.gym.getPhone(), false, findViewById(R.id.gymPhoneButtonRight));
        });

        this.savePhoneBtn.setOnClickListener(v -> {
            if(isValidPhoneNumber(this.localPhone)) {
                this.db.collection("gyms").document(userUid).update(
                        "phone", this.localPhone
                );
                this.gym.setPhone(this.localPhone);
                inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.localPhone, false, findViewById(R.id.gymPhoneButtonRight));
                Snackbar.make(activityView, getResources().getString(R.string.update_phone_success), Snackbar.LENGTH_SHORT).show();
            } else {
                inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.gym.getPhone(), false, findViewById(R.id.gymPhoneButtonRight));
                Snackbar.make(activityView, getResources().getString(R.string.update_phone_error), Snackbar.LENGTH_SHORT).show();
            }
        });

        this.phoneTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.phoneTextBox, this.phoneTextField, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
            this.phoneTextField.setText("");
        });

        this.phoneTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.phoneTextBox, this.phoneTextField, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
            this.phoneTextField.setText("");
        });

        this.phoneTextField.setOnClickListener(v -> {
            inputFieldFocused(this.phoneTextBox, this.phoneTextField, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
        });

        this.phoneTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.phoneTextBox, this.phoneTextField, getResources().getString(R.string.helper_phone_hover), findViewById(R.id.gymPhoneButtonRight));
                this.phoneTextField.setText("");
            } else {
                inputFieldDispatch(this.phoneTextBox, this.phoneTextField, this.gym.getPhone(), false, findViewById(R.id.gymPhoneButtonRight));
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
            inputFieldDispatch(this.addressTextBox, this.addressTextField, this.gym.getAddress(), false, findViewById(R.id.gymAddressButtonRight));
        });

        this.saveAddressBtn.setOnClickListener(v -> {
            this.db.collection("gyms").document(userUid).update(
                    "address", this.localAddress
            );
            this.gym.setAddress(this.localAddress);
            inputFieldDispatch(this.addressTextBox, this.addressTextField, this.localAddress, false, findViewById(R.id.gymAddressButtonRight));
            Snackbar.make(activityView, getResources().getString(R.string.update_address_success), Snackbar.LENGTH_SHORT).show();

            this.map.addMarker(new MarkerOptions().position(this.localPosition)).setTitle(gym.getName());
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(this.localPosition, 15));
        });

        this.addressTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.addressTextBox, this.addressTextField, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
            this.addressTextField.setText("");
        });

        this.addressTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.addressTextBox, this.addressTextField, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
            this.addressTextField.setText("");
        });

        this.addressTextField.setOnClickListener(v -> {
            inputFieldFocused(this.addressTextBox, this.addressTextField, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
        });

        this.addressTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.addressTextBox, this.addressTextField, getResources().getString(R.string.helper_address_hover), findViewById(R.id.gymAddressButtonRight));
                this.addressTextField.setText("");
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(GymProfile.this);
                startActivityForResult(intent, MY_ADDRESS_REQUEST_CODE);
            } else {
                inputFieldDispatch(this.addressTextBox, this.addressTextField, this.gym.getAddress(), false, findViewById(R.id.gymAddressButtonRight));
                this.addressTextBox.clearFocus();
                this.addressTextField.clearFocus();
            }
        });

        /* Name comp event */

        this.deleteNameBtn.setOnClickListener(v -> {
            inputFieldDispatch(this.nameTextBox, this.nameTextField, this.gym.getName(), true, findViewById(R.id.gymNameButtonRight));
        });

        this.saveNameBtn.setOnClickListener(v -> {
            this.db.collection("gyms").document(userUid).update(
                    "name", this.localName
            );
            this.gym.setAddress(this.localName);
            inputFieldDispatch(this.nameTextBox, this.nameTextField, this.localName, true, findViewById(R.id.gymNameButtonRight));
            Snackbar.make(activityView, getResources().getString(R.string.update_name_success), Snackbar.LENGTH_SHORT).show();
        });

        this.nameTextBox.setOnClickListener(v -> {
            inputFieldFocused(this.nameTextBox, this.nameTextField, findViewById(R.id.gymNameButtonRight));
        });

        this.nameTextBox.setEndIconOnClickListener(v -> {
            inputFieldFocused(this.nameTextBox, this.nameTextField, findViewById(R.id.gymNameButtonRight));
        });

        this.nameTextField.setOnClickListener(v -> {
            inputFieldFocused(this.nameTextBox, this.nameTextField, findViewById(R.id.gymNameButtonRight));
        });

        this.nameTextField.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                inputFieldFocused(this.nameTextBox, this.nameTextField, findViewById(R.id.gymNameButtonRight));
            } else {
                inputFieldDispatch(this.nameTextBox, this.nameTextField, this.gym.getName(), true, findViewById(R.id.gymNameButtonRight));
                this.nameTextBox.clearFocus();
                this.nameTextField.clearFocus();
            }
        });

        this.nameTextField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty() && !(s.toString().equals(gym.getName()))) {
                    localName = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /* Image comp event */

        this.editImage.setOnClickListener(v -> {
            setPickImageDialog();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(activityView, getResources().getString(R.string.permission_camera_success), Snackbar.LENGTH_SHORT).show();

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, MY_CAMERA_REQUEST_CODE);
            } else {
                Snackbar.make(activityView, getResources().getString(R.string.permission_camera_error), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_ADDRESS_REQUEST_CODE && !(data == null)) {
            //TODO: eliminare i precedenti markup dell'indirizzo se già esistenti o evitare di ricrearli
            if(resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                this.addressTextField.setText(place.getAddress());
                this.localAddress = place.getAddress();
                this.localPosition = place.getLatLng();
            } else {
                Status status = Autocomplete.getStatusFromIntent(data);
                assert status.getStatusMessage() != null;
                Snackbar.make(activityView, getResources().getString(R.string.intent_result_code_denied) + status.getStatusMessage(), Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_GALLERY_REQUEST_CODE && !(data == null)) {
            if(resultCode == RESULT_OK){
                setAndUploadNewImage(data.getData());
            } else {
                Status status = Autocomplete.getStatusFromIntent(data);
                assert status.getStatusMessage() != null;
                Snackbar.make(activityView, getResources().getString(R.string.intent_result_code_denied) + status.getStatusMessage(), Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_CAMERA_REQUEST_CODE && !(data == null)) {
            if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                setAndUploadNewImage(imageBitmap);
            } else {
                Status status = Autocomplete.getStatusFromIntent(data);
                assert status.getStatusMessage() != null;
                Snackbar.make(activityView, getResources().getString(R.string.intent_result_code_denied) + status.getStatusMessage(), Snackbar.LENGTH_SHORT).show();
            }
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

                TextInputEditText gymNameField = findViewById(R.id.gymTxtName);
                String name = documentSnapshot.getString("name");

                TextInputEditText gymEmailField = findViewById(R.id.gymTxtEmail);
                String email = documentSnapshot.getString("email");

                TextInputEditText gymPhoneField = findViewById(R.id.gymTextPhone);
                String phone = documentSnapshot.getString("phone");

                TextView gymAddressField = findViewById(R.id.gymTextAddress);
                String address = documentSnapshot.getString("address");
                LatLng gymPosition = new LatLng(
                        Objects.requireNonNull(documentSnapshot.getGeoPoint("position")).getLatitude(),
                        Objects.requireNonNull(documentSnapshot.getGeoPoint("position")).getLongitude());

                final ImageView gymImgField = findViewById(R.id.gymImgField);
                final CircleImageView gymImgNameField = findViewById(R.id.gymImgName);
                String imageRef = documentSnapshot.getString("img");
                Picasso.get().load(imageRef).into(gymImgField);
                Picasso.get().load(imageRef).into(gymImgNameField);

                Gym gymTmp = new Gym(userUid, email, phone, name, address, gymPosition, imageRef);
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

    private void setPickImageDialog() {
        final String [] items = new String[] {
                getResources().getString(R.string.dialog_item_pickiamge_gallery),
                getResources().getString(R.string.dialog_item_pickiamge_camera)
        };

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(GymProfile.this, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
        dialogBuilder.setTitle(getResources().getString(R.string.dialog_title_pickimage));
        dialogBuilder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent , MY_GALLERY_REQUEST_CODE);
            } else {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, MY_CAMERA_REQUEST_CODE);
                }
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.prompt_cancel), (dialog, which) -> {});
        dialogBuilder.show();
    }

    private void setAndUploadNewImage(Uri data) {
        Picasso.get().load(data).into(this.gymImgName);
        Picasso.get().load(data).into(this.gymImg);

        StorageReference storageReference = this.storage.getReference().child("img/gyms/" + this.userUid + "/profilePic");
        storageReference.putFile(data)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String uriString = uri.toString();
                    this.gym.setImage(data.toString());
                    this.db.collection("gyms").document(this.userUid).update("img", uriString)
                            .addOnSuccessListener(aVoid -> Snackbar.make(activityView, getResources().getString(R.string.update_image_success), Snackbar.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Snackbar.make(activityView, getResources().getString(R.string.update_image_error), Snackbar.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Snackbar.make(activityView, getResources().getString(R.string.intent_result_code_denied) + e.getMessage(), Snackbar.LENGTH_SHORT).show());

    }

    private void setAndUploadNewImage(Bitmap data) {
        this.gymImgName.setImageBitmap(data);
        this.gymImg.setImageBitmap(data);

        gymImgName.setDrawingCacheEnabled(true);
        gymImgName.buildDrawingCache();
        Bitmap bitmap = gymImgName.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        StorageReference storageReference = this.storage.getReference().child("img/gyms/" + this.userUid + "/profilePic");
        storageReference.putBytes(bytes)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    String uriString = uri.toString();
                    this.gym.setImage(uriString);
                    this.db.collection("gyms").document(this.userUid).update("img", uriString)
                            .addOnSuccessListener(aVoid -> Snackbar.make(activityView, getResources().getString(R.string.update_image_success), Snackbar.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Snackbar.make(activityView, getResources().getString(R.string.update_image_error), Snackbar.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Snackbar.make(activityView, getResources().getString(R.string.intent_result_code_denied) + e.getMessage(), Snackbar.LENGTH_SHORT).show());

        //Snackbar.make(activityView, bytes.length, Snackbar.LENGTH_SHORT).show();
    }

    private void inputFieldFocused(TextInputLayout box, TextInputEditText text, String helperText, LinearLayout container) {
        text.requestFocus();
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

        box.setHelperTextEnabled(true);
        box.setHelperText(helperText);
        box.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, getTheme())));

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void inputFieldFocused(TextInputLayout box, TextInputEditText text, LinearLayout container) {
        text.requestFocus();
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void inputFieldDispatch(TextInputLayout box, TextInputEditText textField, String originText, boolean helperEnable, LinearLayout container) {
        textField.setText(originText);
        textField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_high_type, getTheme())));

        box.setEndIconDrawable(R.drawable.ic_edit);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, getTheme())));
        box.setHelperTextEnabled(helperEnable);

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0));
    }

    private boolean isValidPhoneNumber(String number) {
        /*
            1. ^ start of expression
            2. (\\+\\d{1,3}( )?)? is optional match of country code between 1 to 3 digits prefixed with '+' symbol, followed by space or no space.
            3. ((\\(\\d{1,3}\\))|\\d{1,3} is mandatory group of 1 to 3 digits with or without parenthesis followed by hyphen, space or no space.
            4. \\d{3,4}[- .]? is mandatory group of 3 or 4 digits followed by hyphen, space or no space
            5. \\d{4} is mandatory group of last 4 digits
            6. $ end of expression
        */
        String allCountryRegex = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
        if(number.matches(allCountryRegex)) {
            return number.length() <= this.phoneTextBox.getCounterMaxLength();
        } else {
            return false;
        }
    }

}