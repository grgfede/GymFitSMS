package com.example.gymfit.gym.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.main.FragmentUserProfile;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentGymProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentGymProfile extends Fragment implements OnMapReadyCallback {
    private static final String GYM_KEY = "gym_key";
    private static final String IS_EMPTY_KEY = "is_empty_key";
    private static final String EMPTY_DATA_KEY = "empty_data_key";

    private static final int MY_ADDRESS_REQUEST_CODE = 100, MY_CAMERA_REQUEST_CODE = 10, MY_GALLERY_REQUEST_CODE = 11, MY_CAMERA_PERMISSION_CODE = 9;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Animations
    private final Map<String, Animation> animationMap = new HashMap<>();

    // Floating Action Buttons
    private final Map<String, FloatingActionButton> fabMap = new HashMap<>();

    // Image
    private final Map<String, View> imageMap = new HashMap<>();

    // Buttons
    private final Map<String, MaterialButton> saveButtonMap = new HashMap<>();
    private final Map<String, MaterialButton> deleteButtonMap = new HashMap<>();

    // Texts
    private final Map<String, TextInputLayout> layoutTextMap = new HashMap<>();
    private final Map<String, TextInputEditText> editTextMap = new HashMap<>();
    private final Map<String, Object> tempTextMap = new HashMap<>();

    // Screen orientation
    private int orientation;
    private View messageAnchor = null;
    private Menu toolbar = null;

    private String gymUID = null;
    private Gym gym = null;
    private boolean isEmptyData = false;
    private List<String> emptyData = new ArrayList<>();

    private GoogleMap map = null;
    private boolean circleBtnClicked = false;

    public static FragmentGymProfile newInstance(Gym gym, boolean isEmptyData, ArrayList<String> emptyData) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentGymProfile created");

        FragmentGymProfile fragment = new FragmentGymProfile();
        Bundle bundle = new Bundle();
        bundle.putSerializable(GYM_KEY, gym);
        bundle.putBoolean(IS_EMPTY_KEY, isEmptyData);
        bundle.putStringArrayList(EMPTY_DATA_KEY, emptyData);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.gym = (Gym) getArguments().getSerializable(GYM_KEY);
            this.isEmptyData = getArguments().getBoolean(IS_EMPTY_KEY);
            this.emptyData = getArguments().getStringArrayList(EMPTY_DATA_KEY);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_profile, container, false);

        initSystemInterface(rootView);
        initInterface(rootView, orientation);

        setImageMap(rootView, orientation);
        setAnimationMap(rootView);
        setFabMap(rootView);
        setEditTextMap(rootView);
        setLayoutTextMap(rootView);
        setSaveButtonMap(rootView);
        setDeleteButtonMap(rootView);
        setTempTextMap();

        isEmptyData();

        // View listener
        try {

            /* Floating Action Buttons listener */
            this.fabMap.forEach((key, fab) -> fab.setOnClickListener(v -> {
                if (!key.equals("main") && !key.equals("editImage")) {
                    setVisibility(false);
                    setAnimation(false);
                    setCircleBtnClickable(false);
                    circleBtnClicked = false;
                }

                switch (key) {
                    case "settings":
                        AppUtils.startFragment((AppCompatActivity) requireActivity(), FragmentGymSettings.newInstance(this.gym), true);
                        break;
                    case "subscribers":
                        AppUtils.startFragment((AppCompatActivity) requireActivity(), FragmentGymSubs.newInstance(this.gym), true);
                        break;
                    case "main":
                        onAddButtons();
                        break;
                    case "editImage":
                        setPickImageDialog();
                        break;
                }

            }));

            /* LayoutText listener */
            this.layoutTextMap.forEach((key, field) -> {
                switch (key) {
                    case "email":
                        field.setOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("email"), getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.gymEmailButtonRight));
                            this.editTextMap.get("email").setText("");
                        });

                        field.setEndIconOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("email"), getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.gymEmailButtonRight));
                            this.editTextMap.get("email").setText("");
                        });
                        break;
                    case "key":
                        field.setOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("key"), getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.gymKeyButtonRight));
                            this.editTextMap.get("key").setText("");
                        });

                        field.setEndIconOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("key"), getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.gymKeyButtonRight));
                            this.editTextMap.get("key").setText("");
                        });
                        break;
                    case "phone":
                        field.setOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("phone"), getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.gymPhoneButtonRight));
                            this.editTextMap.get("phone").setText("");
                        });

                        field.setEndIconOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("phone"), getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.gymPhoneButtonRight));
                            this.editTextMap.get("phone").setText("");
                        });
                        break;
                    case "address":
                        field.setOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("address"), getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.gymAddressButtonRight));
                            this.editTextMap.get("address").setText("");
                        });

                        field.setEndIconOnClickListener(v -> {
                            inputFieldFocused(field, this.editTextMap.get("address"), getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.gymAddressButtonRight));
                            this.editTextMap.get("address").setText("");
                        });
                        break;
                    case "name":
                        field.setOnClickListener(v -> inputFieldFocused(field, this.editTextMap.get("name"), rootView.findViewById(R.id.gymNameButtonRight)));

                        field.setEndIconOnClickListener(v -> inputFieldFocused(field, this.editTextMap.get("name"), rootView.findViewById(R.id.gymNameButtonRight)));
                        break;
                }
            });

            /* EditTest listener */
            this.editTextMap.forEach((key, field) -> {
                switch (key) {
                    case "email":
                        field.setOnClickListener(v -> inputFieldFocused(this.layoutTextMap.get("email"), field, getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.gymEmailButtonRight)));

                        field.setOnFocusChangeListener((v, hasFocus) -> {

                            if(hasFocus) {
                                inputFieldFocused(this.layoutTextMap.get("email"), field, getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.gymEmailButtonRight));
                                field.setText("");
                            } else {
                                inputFieldDispatch(this.layoutTextMap.get("email"), field, this.gym.getEmail(), false, rootView.findViewById(R.id.gymEmailButtonRight));
                                this.layoutTextMap.get("email").clearFocus();
                                field.clearFocus();
                            }
                        });

                        field.addTextChangedListener(new TextWatcher() {

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!s.toString().isEmpty() && !(s.toString().equals(gym.getEmail()))) {
                                    tempTextMap.replace("email", s.toString());
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        break;
                    case "key":
                        field.setOnClickListener(v -> inputFieldFocused(this.layoutTextMap.get("key"), field, getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.gymKeyButtonRight)));

                        field.setOnFocusChangeListener((v, hasFocus) -> {

                            if(hasFocus) {
                                inputFieldFocused(this.layoutTextMap.get("key"), field, getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.gymKeyButtonRight));
                                field.setText("");
                            } else {
                                inputFieldDispatch(this.layoutTextMap.get("key"), field, getResources().getString(R.string.password_hide), false, rootView.findViewById(R.id.gymKeyButtonRight));
                                this.layoutTextMap.get("key").clearFocus();
                                field.clearFocus();
                            }
                        });

                        field.addTextChangedListener(new TextWatcher() {

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!s.toString().isEmpty() && !s.toString().equals(getResources().getString(R.string.password_hide))) {
                                    tempTextMap.replace("key", s.toString());
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        break;
                    case "phone":
                        field.setOnClickListener(v -> inputFieldFocused(this.layoutTextMap.get("phone"), field, getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.gymPhoneButtonRight)));

                        field.setOnFocusChangeListener((v, hasFocus) -> {

                            if(hasFocus) {
                                inputFieldFocused(this.layoutTextMap.get("phone"), field, getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.gymPhoneButtonRight));
                                field.setText("");
                            } else {
                                inputFieldDispatch(this.layoutTextMap.get("phone"), field, this.gym.getPhone(), false, rootView.findViewById(R.id.gymPhoneButtonRight));
                                this.layoutTextMap.get("phone").clearFocus();
                                field.clearFocus();
                            }
                        });

                        field.addTextChangedListener(new TextWatcher() {

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!s.toString().isEmpty() && !(s.toString().equals(gym.getPhone()))) {
                                    tempTextMap.replace("phone", s.toString());
                                }

                                if(s.toString().length() > layoutTextMap.get("phone").getCounterMaxLength()) {
                                    layoutTextMap.get("phone").setError(getResources().getString(R.string.helper_phone_error));
                                } else {
                                    layoutTextMap.get("phone").setError(null);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        break;
                    case "address":
                        field.setOnClickListener(v -> inputFieldFocused(this.layoutTextMap.get("address"), field, getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.gymAddressButtonRight)));

                        field.setOnFocusChangeListener((v, hasFocus) -> {

                            if(hasFocus) {
                                inputFieldFocused(this.layoutTextMap.get("address"), field, getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.gymAddressButtonRight));
                                field.setText("");
                                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(requireContext());
                                startActivityForResult(intent, MY_ADDRESS_REQUEST_CODE);
                            } else {
                                inputFieldDispatch(this.layoutTextMap.get("address"), field, this.gym.getAddress(), false, rootView.findViewById(R.id.gymAddressButtonRight));
                                this.layoutTextMap.get("address").clearFocus();
                                field.clearFocus();
                            }
                        });
                        break;
                    case "name":
                        field.setOnClickListener(v -> inputFieldFocused(this.layoutTextMap.get("name"), field, rootView.findViewById(R.id.gymNameButtonRight)));

                        field.setOnFocusChangeListener((v, hasFocus) -> {

                            if(hasFocus) {
                                inputFieldFocused(this.layoutTextMap.get("name"), field, rootView.findViewById(R.id.gymNameButtonRight));
                            } else {
                                inputFieldDispatch(this.layoutTextMap.get("name"), field, this.gym.getName(), true, rootView.findViewById(R.id.gymNameButtonRight));
                                this.layoutTextMap.get("name").clearFocus();
                                field.clearFocus();
                            }
                        });

                        field.addTextChangedListener(new TextWatcher() {

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!s.toString().isEmpty() && !(s.toString().equals(gym.getName()))) {
                                    tempTextMap.replace("name", s.toString());
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        break;
                }
            });

            /* Save Buttons listener */
            this.saveButtonMap.forEach((key, btn) -> {
                switch (key) {
                    case "email":
                        btn.setOnClickListener(v -> FirebaseAuth.getInstance().getCurrentUser().updateEmail(Objects.requireNonNull((String) this.tempTextMap.get("email")))
                                .addOnSuccessListener(aVoid -> {
                                    this.db.collection("gyms").document(gymUID).update(
                                            "email", this.tempTextMap.get("email")
                                    );
                                    this.gym.setEmail((String) this.tempTextMap.get("email"));

                                    this.emptyData.remove(key);

                                    inputFieldDispatch(this.layoutTextMap.get("email"), this.editTextMap.get("email"), (String) this.tempTextMap.get("email"), false, rootView.findViewById(R.id.gymEmailButtonRight));
                                    AppUtils.log(Thread.currentThread().getStackTrace(), "New email is updated on Database and Gym object");
                                    AppUtils.message(messageAnchor, getResources().getString(R.string.update_email_success), Snackbar.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    inputFieldDispatch(this.layoutTextMap.get("email"), this.editTextMap.get("email"), this.gym.getEmail(), false, rootView.findViewById(R.id.gymEmailButtonRight));
                                    AppUtils.log(Thread.currentThread().getStackTrace(), "New email is deleted");
                                    AppUtils.message(messageAnchor, getResources().getString(R.string.update_email_error), Snackbar.LENGTH_SHORT).show();
                                }));
                        break;
                    case "key":
                        btn.setOnClickListener(v ->FirebaseAuth.getInstance().getCurrentUser().updatePassword((String) Objects.requireNonNull(this.tempTextMap.get("key")))
                                .addOnSuccessListener(aVoid -> {
                                    inputFieldDispatch(this.layoutTextMap.get("key"), this.editTextMap.get("key"), (String) this.tempTextMap.get("key"), false, rootView.findViewById(R.id.gymKeyButtonRight));
                                    AppUtils.log(Thread.currentThread().getStackTrace(), "New key is updated on Database and Gym object");
                                    AppUtils.message(messageAnchor, getResources().getString(R.string.update_password_success), Snackbar.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    inputFieldDispatch(this.layoutTextMap.get("key"), this.editTextMap.get("key"), getResources().getString(R.string.password_hide), false, rootView.findViewById(R.id.gymKeyButtonRight));
                                    AppUtils.log(Thread.currentThread().getStackTrace(), "New key is deleted");
                                    AppUtils.message(messageAnchor, getResources().getString(R.string.update_password_error), Snackbar.LENGTH_SHORT).show();
                                }));
                        break;
                    case "phone":
                        btn.setOnClickListener(v -> {
                            if(isValidPhoneNumber((String) Objects.requireNonNull(this.tempTextMap.get("phone")))) {
                                this.db.collection("gyms").document(gymUID).update(
                                        "phone", this.tempTextMap.get("phone")
                                );
                                this.gym.setPhone((String) this.tempTextMap.get("phone"));

                                this.emptyData.remove(key);

                                inputFieldDispatch(this.layoutTextMap.get("phone"), this.editTextMap.get("phone"), (String) this.tempTextMap.get("phone"), false, rootView.findViewById(R.id.gymPhoneButtonRight));
                                AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is updated on Database and Gym object");
                                AppUtils.message(messageAnchor, getResources().getString(R.string.update_phone_success), Snackbar.LENGTH_SHORT).show();
                            } else {
                                inputFieldDispatch(this.layoutTextMap.get("phone"), this.editTextMap.get("phone"), this.gym.getPhone(), false, rootView.findViewById(R.id.gymPhoneButtonRight));
                                AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is deleted");
                                AppUtils.message(messageAnchor, getResources().getString(R.string.update_phone_error), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case "address":
                        btn.setOnClickListener(v -> {
                            LatLng positionTmp = (LatLng) this.tempTextMap.get("position");
                            this.db.collection("gyms").document(gymUID).update(
                                    "address", this.tempTextMap.get("address"),
                                    "position", new GeoPoint(positionTmp.latitude, positionTmp.longitude)
                            );
                            this.gym.setAddress((String) this.tempTextMap.get("address"));
                            this.gym.setPosition((LatLng) this.tempTextMap.get("position"));

                            this.emptyData.remove(key);
                            this.emptyData.remove("position");

                            inputFieldDispatch(this.layoutTextMap.get("address"), this.editTextMap.get("address"), (String) this.tempTextMap.get("address"), false, rootView.findViewById(R.id.gymAddressButtonRight));
                            AppUtils.log(Thread.currentThread().getStackTrace(), "New address is updated on Database and Gym object");
                            AppUtils.message(messageAnchor, getResources().getString(R.string.update_address_success), Snackbar.LENGTH_SHORT).show();

                            this.map.clear();
                            this.map.addMarker(new MarkerOptions().position((LatLng) this.tempTextMap.get("position"))).setTitle(gym.getName());
                            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) this.tempTextMap.get("position"), 15));
                        });
                        break;
                    case "name":
                        btn.setOnClickListener(v -> {
                            this.db.collection("gyms").document(gymUID).update(
                                    "name", this.tempTextMap.get("name")
                            );
                            this.gym.setName((String) this.tempTextMap.get("name"));

                            this.emptyData.remove(key);

                            inputFieldDispatch(this.layoutTextMap.get("name"), this.editTextMap.get("name"), (String) this.tempTextMap.get("name"), true, rootView.findViewById(R.id.gymNameButtonRight));
                            AppUtils.log(Thread.currentThread().getStackTrace(), "New name is updated on Database and Gym object");
                            AppUtils.message(messageAnchor, getResources().getString(R.string.update_name_success), Snackbar.LENGTH_SHORT).show();

                            NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);
                            ((MaterialTextView) navigationView.getHeaderView(0).findViewById(R.id.header_gym_name)).setText((String) this.tempTextMap.get("name"));
                        });
                        break;
                }
            });

            /* Delete Buttons listener */
            this.deleteButtonMap.forEach((key, btn) -> {
                switch (key) {
                    case "email":
                        btn.setOnClickListener(v ->
                                inputFieldDispatch(this.layoutTextMap.get("email"), this.editTextMap.get("email"), this.gym.getEmail(), false, rootView.findViewById(R.id.gymEmailButtonRight)));
                        break;
                    case "key":
                        btn.setOnClickListener(v ->
                                inputFieldDispatch(this.layoutTextMap.get("key"), this.editTextMap.get("key"), getResources().getString(R.string.password_hide), false, rootView.findViewById(R.id.gymKeyButtonRight)));
                        break;
                    case "phone":
                        btn.setOnClickListener(v ->
                                inputFieldDispatch(this.layoutTextMap.get("phone"), this.editTextMap.get("phone"), this.gym.getPhone(), false, rootView.findViewById(R.id.gymPhoneButtonRight)));
                        break;
                    case "address":
                        btn.setOnClickListener(v ->
                                inputFieldDispatch(this.layoutTextMap.get("address"), this.editTextMap.get("address"), this.gym.getAddress(), false, rootView.findViewById(R.id.gymAddressButtonRight)));
                        break;
                    case "name":
                        btn.setOnClickListener(v ->
                                inputFieldDispatch(this.layoutTextMap.get("name"), this.editTextMap.get("name"), this.gym.getName(), true, rootView.findViewById(R.id.gymNameButtonRight)));
                        break;
                }
            });

        } catch (Exception e) {
            AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
            AppUtils.restartActivity((AppCompatActivity) requireActivity());
        }

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentGymProfile layout XML created");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gym_profile_toolbar, menu);
        this.toolbar = menu;
        super.onCreateOptionsMenu(menu, inflater);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Toolbar Gym is inflated");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_edit) {
            if (item.isChecked()) {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Gym profile is not more under edit");

                // Restore icon
                item.setIcon(R.drawable.ic_edit);
                setEditIconVisibility(false);
                setLayoutTextEnable(false);
                item.setChecked(false);

                // show empty data message
                isEmptyData();
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Gym profile is under edit");

                // Activate edit icons
                item.setIcon(R.drawable.ic_clear);
                setEditIconVisibility(true);
                setLayoutTextEnable(true);
                item.setChecked(true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Camera permission granted from Gym profile");
                AppUtils.message(messageAnchor, getResources().getString(R.string.permission_camera_success), Snackbar.LENGTH_SHORT).show();

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, MY_CAMERA_REQUEST_CODE);
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Camera permission not authorized from Gym profile");
                AppUtils.message(messageAnchor, getResources().getString(R.string.permission_camera_error), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.clear();

        LatLng latLngUser = this.gym.getPosition();
        this.map.addMarker(new MarkerOptions().position(latLngUser)).setTitle(this.gym.getName());
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngUser, 15));

        AppUtils.log(Thread.currentThread().getStackTrace(), "Map is created and positioned at" + " (lat) " + latLngUser.latitude + " (lng) " + latLngUser.longitude);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppUtils.log(Thread.currentThread().getStackTrace(), "Activity result: " + requestCode + " " + resultCode);

        if (requestCode == MY_ADDRESS_REQUEST_CODE && !(data == null)) {
            if(resultCode == ActivityGymProfile.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                this.editTextMap.get("address").setText(place.getAddress());
                this.tempTextMap.replace("address", place.getAddress());
                this.tempTextMap.replace("position", place.getLatLng());
            }
        } else if (requestCode == MY_GALLERY_REQUEST_CODE && !(data == null)) {
            if(resultCode == ActivityGymProfile.RESULT_OK) {
                this.emptyData.remove("img");
                setAndUploadNewImage(data.getData(), this.orientation);
            } else {
                Status status = Autocomplete.getStatusFromIntent(data);
                AppUtils.message(messageAnchor, status.getStatusMessage(), Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_CAMERA_REQUEST_CODE && !(data == null)) {
            if(resultCode == ActivityGymProfile.RESULT_OK) {
                this.emptyData.remove("img");
                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                setAndUploadNewImage(imageBitmap, this.orientation);
            } else {
                Status status = Autocomplete.getStatusFromIntent(data);
                AppUtils.message(messageAnchor, status.getStatusMessage(), Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppUtils.log(Thread.currentThread().getStackTrace(), "Orientation changed: " + newConfig.orientation);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            try {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                if (Build.VERSION.SDK_INT >= 26) {
                    ft.setReorderingAllowed(false);
                }
                ft.detach(this).attach(this).commit();

                AppUtils.log(Thread.currentThread().getStackTrace(), "Orientation changed: replaced interface");

                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    initInterface(requireView(), newConfig.orientation);
                }

            } catch (Exception e) {
                AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                AppUtils.message(this.messageAnchor, e.toString(), Snackbar.LENGTH_SHORT).show();
                AppUtils.restartActivity((AppCompatActivity) requireActivity());
            }
        }
    }

    private void onAddButtons() {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Fab status: " + !circleBtnClicked);

        setVisibility(circleBtnClicked);
        setAnimation(circleBtnClicked);
        setCircleBtnClickable(circleBtnClicked);
        circleBtnClicked = !circleBtnClicked;
    }

    // Animation methods

    /**
     * Set map with animation resources.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setAnimationMap(View rootView) {
        this.animationMap.put("rotateOpen", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.rotate_open_anim));
        this.animationMap.put("rotateClose", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.rotate_close_anim));
        this.animationMap.put("fromButton", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.from_bottom_anim));
        this.animationMap.put("toButton", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.to_bottom_anim));
    }

    /**
     * Set map with Floating Buttons views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setFabMap(View rootView) {
        this.fabMap.put("main", rootView.findViewById(R.id.fab_add));
        this.fabMap.put("settings", rootView.findViewById(R.id.fab_setting));
        this.fabMap.put("subscribers", rootView.findViewById(R.id.fab_subs));
        this.fabMap.put("editImage", rootView.findViewById(R.id.gymEditImg));
    }

    /**
     * Set the visibility of button when the first "fab" is clicked
     *
     * @param clicked truth flag for button visibility
     */
    private void setVisibility(boolean clicked) {
        if(!clicked) {
            this.fabMap.get("settings").setVisibility(View.VISIBLE);
            this.fabMap.get("subscribers").setVisibility(View.VISIBLE);
        } else {
            this.fabMap.get("settings").setVisibility(View.INVISIBLE);
            this.fabMap.get("subscribers").setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Set the animation of button when the first "fab" is clicked
     *
     * @param clicked truth flag for button visibility
     */
    private void setAnimation(boolean clicked) {
        if(!clicked) {
            this.fabMap.get("settings").startAnimation(this.animationMap.get("fromButton"));
            this.fabMap.get("subscribers").startAnimation(this.animationMap.get("fromButton"));
            this.fabMap.get("main").startAnimation(this.animationMap.get("rotateOpen"));
        } else {
            this.fabMap.get("settings").startAnimation(this.animationMap.get("toButton"));
            this.fabMap.get("subscribers").startAnimation(this.animationMap.get("toButton"));
            this.fabMap.get("main").startAnimation(this.animationMap.get("rotateClose"));
        }
    }

    /**
     * Set the value of truth flag
     *
     * @param circleBtnClicked truth flag for button visibility
     */
    private void setCircleBtnClickable(boolean circleBtnClicked) {
        if(!circleBtnClicked) {
            this.fabMap.get("settings").setClickable(true);
            this.fabMap.get("subscribers").setClickable(true);
        } else {
            this.fabMap.get("settings").setClickable(false);
            this.fabMap.get("subscribers").setClickable(false);
        }
    }

    // Image methods

    /**
     * Set map with ImageView and CircleImageView views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     * @param orientation Orientation value to separate the setting of map
     */
    private void setImageMap(View rootView, int orientation) {
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);
        View viewHeader = navigationView.getHeaderView(0);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.imageMap.put("imageMain", rootView.findViewById(R.id.gymImgField));
        }

        this.imageMap.put("imageIcon", rootView.findViewById(R.id.gymImgName));
        this.imageMap.put("imageMenu", viewHeader.findViewById(R.id.header_gym_image));
    }

    /**
     * Set and create a Material Dialog with two items (from gallery, from camera) to pick a image, witch it will replace the old image.
     * To do this will be create two Intent (one for each choice) with their Android permission.
     * In the case of "camera" choice if its permission is denied will be called another method to activate it and instantiate Intent.
     */
    private void setPickImageDialog() {
        final String [] items = new String[] {
                getResources().getString(R.string.dialog_item_pickimage_gallery),
                getResources().getString(R.string.dialog_item_pickimage_camera)
        };

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
        dialogBuilder.setTitle(getResources().getString(R.string.dialog_title_pickimage));
        dialogBuilder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent , MY_GALLERY_REQUEST_CODE);
            } else {
                if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, MY_CAMERA_REQUEST_CODE);
                }
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.prompt_delete), (dialog, which) -> {});
        dialogBuilder.show();
    }

    /**
     * Set views with Uri image and upload it into Database and Storage
     *
     * @param data Uri of image taken from Gallery that will be used to store image into ImageViews, Database and Storage
     * @param orientation Orientation value to separate the setting of image
     */
    private void setAndUploadNewImage(Uri data, int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Picasso.get().load(data).into((ImageView) this.imageMap.get("imageMain"));
        }

        Picasso.get().load(data).into((CircleImageView) this.imageMap.get("imageIcon"));
        Picasso.get().load(data).into((CircleImageView) this.imageMap.get("imageMenu"));

        StorageReference storageReference = this.storage.getReference().child("img/gyms/" + this.gymUID + "/profilePic");
        storageReference.putFile(data)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String uriString = uri.toString();
                    this.gym.setImage(data.toString());
                    this.db.collection("gyms").document(this.gymUID).update("img", uriString)
                            .addOnSuccessListener(aVoid -> {
                                AppUtils.log(Thread.currentThread().getStackTrace(), "New image is updated into Database");
                                AppUtils.message(messageAnchor, getResources().getString(R.string.update_image_success), Snackbar.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                AppUtils.log(Thread.currentThread().getStackTrace(), "New image is not updated into Database");
                                AppUtils.message(messageAnchor, getResources().getString(R.string.update_image_error), Snackbar.LENGTH_SHORT).show();
                            });

                    AppUtils.log(Thread.currentThread().getStackTrace(), "New image is updated on Storage and Gym object");
                }))
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "New image is deleted");
                    AppUtils.message(messageAnchor, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });

    }

    /**
     * Set views with Bitmap image and upload it into Database and Storage
     *
     * @param data Bitmap of image taken from Camera that will be used to store image into ImageViews, Database and Storage
     * @param orientation Orientation value to separate the setting of image
     */
    private void setAndUploadNewImage(Bitmap data, int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((ImageView) Objects.requireNonNull(this.imageMap.get("imageMain"))).setImageBitmap(data);
        }

        ((CircleImageView) Objects.requireNonNull(this.imageMap.get("imageMenu"))).setImageBitmap(data);
        ((CircleImageView) Objects.requireNonNull(this.imageMap.get("imageIcon"))).setImageBitmap(data);

        Objects.requireNonNull(this.imageMap.get("imageIcon")).setDrawingCacheEnabled(true);
        Objects.requireNonNull(this.imageMap.get("imageIcon")).buildDrawingCache();
        Bitmap bitmap = Objects.requireNonNull(this.imageMap.get("imageIcon")).getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        StorageReference storageReference = this.storage.getReference().child("img/gyms/" + this.gymUID + "/profilePic");
        storageReference.putBytes(bytes)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    String uriString = uri.toString();
                    this.gym.setImage(uriString);
                    this.db.collection("gyms").document(this.gymUID).update("img", uriString)
                            .addOnSuccessListener(aVoid -> {
                                AppUtils.log(Thread.currentThread().getStackTrace(), "New image is updated into Database");
                                AppUtils.message(messageAnchor, getResources().getString(R.string.update_image_success), Snackbar.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                AppUtils.log(Thread.currentThread().getStackTrace(), "New image is not updated into Database");
                                AppUtils.message(messageAnchor, getResources().getString(R.string.update_image_error), Snackbar.LENGTH_SHORT).show();
                            });

                    AppUtils.log(Thread.currentThread().getStackTrace(), "New image is updated on Storage and Gym object");
                })).addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "New image is deleted");
                    AppUtils.message(messageAnchor, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
    }

    // Buttons

    /**
     * Set map with save buttons views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setSaveButtonMap(View rootView) {
        this.saveButtonMap.put("email", rootView.findViewById(R.id.gymSaveMail));
        this.saveButtonMap.put("key", rootView.findViewById(R.id.gymSaveKey));
        this.saveButtonMap.put("phone", rootView.findViewById(R.id.gymSavePhone));
        this.saveButtonMap.put("address", rootView.findViewById(R.id.gymSaveAddress));
        this.saveButtonMap.put("name", rootView.findViewById(R.id.gymSaveName));
    }

    /**
     * Set map with delete buttons views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setDeleteButtonMap(View rootView) {
        this.deleteButtonMap.put("email", rootView.findViewById(R.id.gymAbortMail));
        this.deleteButtonMap.put("key", rootView.findViewById(R.id.gymAbortKey));
        this.deleteButtonMap.put("phone", rootView.findViewById(R.id.gymAbortPhone));
        this.deleteButtonMap.put("address", rootView.findViewById(R.id.gymAbortAddress));
        this.deleteButtonMap.put("name", rootView.findViewById(R.id.gymAbortName));
    }

    // Texts

    /**
     * Set map with layut text views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setLayoutTextMap(View rootView) {
        this.layoutTextMap.put("email", rootView.findViewById(R.id.gymBoxEmail));
        this.layoutTextMap.put("key", rootView.findViewById(R.id.gymBoxKey));
        this.layoutTextMap.put("phone", rootView.findViewById(R.id.gymBoxPhone));
        this.layoutTextMap.put("address", rootView.findViewById(R.id.gymBoxAddress));
        this.layoutTextMap.put("name", rootView.findViewById(R.id.gymBoxName));

        setEditIconVisibility(false);
    }

    /**
     * Set map with edit text views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setEditTextMap(View rootView) {
        this.editTextMap.put("email", rootView.findViewById(R.id.gymTxtEmail));
        this.editTextMap.put("key", rootView.findViewById(R.id.gymTextKey));
        this.editTextMap.put("phone", rootView.findViewById(R.id.gymTextPhone));
        this.editTextMap.put("address", rootView.findViewById(R.id.gymTextAddress));
        this.editTextMap.put("name", rootView.findViewById(R.id.gymTxtName));
    }

    /**
     * Set map with temporal edit text views.
     */
    private void setTempTextMap() {
        this.tempTextMap.put("email", "");
        this.tempTextMap.put("key", "");
        this.tempTextMap.put("phone", "");
        this.tempTextMap.put("address", "");
        this.tempTextMap.put("name", "");
        this.tempTextMap.put("position", new LatLng(0, 0));
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(View rootView) {
        // init new checked item on navigation Drawer
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);
        navigationView.getMenu().findItem(R.id.nav_menu_home).setChecked(true);

        // Abilities toolbar item options
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_profile_toolbar_title));

        // init origin screen orientation
        this.orientation = rootView.getResources().getConfiguration().orientation;

        // init gym ID from Gym Object
        this.gymUID = this.gym.getUid();

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentGymProfile initialized");
    }

    /**
     * Take from Gym all fields that will be add into layout XML file fields
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     * @param orientation Orientation value to separate the setting of layout XML field
     */
    private void initInterface(View rootView, int orientation) {
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);

        // Get view object
        MaterialTextView menuNameField = navigationView.getHeaderView(0).findViewById(R.id.header_gym_name);
        TextInputEditText nameField = rootView.findViewById(R.id.gymTxtName);
        TextInputEditText emailField = rootView.findViewById(R.id.gymTxtEmail);
        TextInputEditText phoneField = rootView.findViewById(R.id.gymTextPhone);
        TextInputEditText addressField = rootView.findViewById(R.id.gymTextAddress);

        CircleImageView imageField = rootView.findViewById(R.id.gymImgName);
        CircleImageView imageMenu = navigationView.getHeaderView(0).findViewById(R.id.header_gym_image);

        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.gymMapField));
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Set view object
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ImageView imageHeader = rootView.findViewById(R.id.gymImgField);
            Picasso.get().load(this.gym.getImage()).into(imageHeader);
        }
        Picasso.get().load(this.gym.getImage()).into(imageMenu);
        Picasso.get().load(this.gym.getImage()).into(imageField);

        menuNameField.setText(this.gym.getName());
        nameField.setText(this.gym.getName());
        emailField.setText(this.gym.getEmail());
        phoneField.setText(this.gym.getPhone());
        addressField.setText(this.gym.getAddress());

        AppUtils.log(Thread.currentThread().getStackTrace(), "View interface of FragmentGymProfile initialized");
    }

    /**
     * Set and show a long message when there are empty value
     */
    private void setActionForEmptyData() {
        if (this.isEmptyData) {
            Snackbar snackbar = AppUtils.message(this.messageAnchor, getString(R.string.profile_not_completed), Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction(getString(R.string.system_toolbar_edit), v -> onOptionsItemSelected(this.toolbar.findItem(R.id.app_bar_edit)))
                    .setActionTextColor(ResourcesCompat.getColor(getResources(), R.color.tint_message_text, null))
                    .show();
        }
    }

    // Other methods

    /**
     * Add at all field TextLayout the end icon when the flag is true, alternately it will be removed
     *
     * @param isVisible truth flag for button visibility
     */
    private void setEditIconVisibility(boolean isVisible) {
        if (isVisible) {
            this.fabMap.get("editImage").setVisibility(View.VISIBLE);
            this.layoutTextMap.forEach((key, field) -> {
                field.setEndIconVisible(true);

                if (key.equals("phone")) {
                    field.setCounterEnabled(true);
                }
            });
        } else {
            this.fabMap.get("editImage").setVisibility(View.INVISIBLE);
            this.layoutTextMap.forEach((key, field) -> {
                field.setEndIconVisible(false);

                if (key.equals("phone")) {
                    field.setCounterEnabled(false);
                }
            });
        }
    }

    /**
     * Set all TextLayout editable if flag is true, alternately not editable
     *
     * @param isEnable truth flag for field enable
     */
    private void setLayoutTextEnable(boolean isEnable) {
        if (isEnable) {
            this.layoutTextMap.forEach((key, field) -> {
                field.setEnabled(true);
                field.setFocusable(true);
                field.setFocusableInTouchMode(true);
            });
        } else {
            this.layoutTextMap.forEach((key, field) -> {
                field.setEnabled(false);
                field.setFocusable(false);
                field.setFocusableInTouchMode(false);
            });
        }
    }

    private void inputFieldFocused(TextInputLayout box, TextInputEditText text, String helperText, LinearLayout container) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field focused: " + box.getId());

        text.requestFocus();
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, requireActivity().getTheme())));

        box.setHelperTextEnabled(true);
        box.setHelperText(helperText);
        box.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary, requireActivity().getTheme())));

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void inputFieldFocused(TextInputLayout box, TextInputEditText text, LinearLayout container) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field focused: " + box.getId());

        text.requestFocus();
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, requireActivity().getTheme())));

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void inputFieldDispatch(TextInputLayout box, TextInputEditText textField, String originText, boolean helperEnable, LinearLayout container) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field dispatched: " + box.getId());

        textField.setText(originText);
        textField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_high_type, requireActivity().getTheme())));

        box.setEndIconDrawable(R.drawable.ic_edit);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_on_background_emphasis_medium, requireActivity().getTheme())));
        box.setHelperTextEnabled(helperEnable);

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0));
    }

    private void isEmptyData() throws NullPointerException {
        if (this.emptyData.isEmpty()) {
            this.isEmptyData = false;
            AppUtils.log(Thread.currentThread().getStackTrace(), "There are not empty data now");
        } else {
            this.isEmptyData = true;
            setActionForEmptyData();
            AppUtils.log(Thread.currentThread().getStackTrace(), "There are empty data yet: " + this.emptyData.toString());
        }
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
            AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is valid");
            return number.length() <= this.layoutTextMap.get("phone").getCounterMaxLength();
        } else {
            AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is not valid");
            return false;
        }
    }

}