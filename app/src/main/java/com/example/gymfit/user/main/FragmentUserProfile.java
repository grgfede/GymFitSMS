package com.example.gymfit.user.main;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.user.conf.User;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserProfile extends Fragment {
    private static final String USER_KEY = "user_key";
    private static final String IS_EMPTY_KEY = "is_empty_key";
    private static final String EMPTY_DATA_KEY = "empty_data_key";

    private static final int MY_ADDRESS_REQUEST_CODE = 100, MY_CAMERA_REQUEST_CODE = 10, MY_GALLERY_REQUEST_CODE = 11, MY_CAMERA_PERMISSION_CODE = 9;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // Screen orientation
    private int orientation;
    private View messageAnchor = null;
    private Menu toolbar = null;

    private User user = null;
    private boolean isEmptyData = false;
    private List<String> emptyData = new ArrayList<>();

    private boolean circleBtnClicked = false;

    // Image
    private final Map<String, ImageView> imagesMap = new HashMap<>();

    // Animations
    private final Map<String, Animation> animationsMap = new HashMap<>();
    private final Map<String, FloatingActionButton> fabsMap = new HashMap<>();

    // Texts
    private final Map<String, TextInputLayout> inputTextMap = new HashMap<>();
    private final Map<String, TextInputEditText> editTextMap = new HashMap<>();
    private final Map<String, Object> tempTextMap = new HashMap<>();
    private AutoCompleteTextView editTextGender;

    // Buttons
    private final Map<String, MaterialButton> saveButtonMap = new HashMap<>();
    private final Map<String, MaterialButton> deleteButtonMap = new HashMap<>();


    public static FragmentUserProfile newInstance(@NonNull final User user, final boolean isEmptyData, @NonNull final ArrayList<String> emptyData) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserProfile created");

        final FragmentUserProfile fragment = new FragmentUserProfile();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(USER_KEY, user);
        bundle.putBoolean(IS_EMPTY_KEY, isEmptyData);
        bundle.putStringArrayList(EMPTY_DATA_KEY, emptyData);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
            this.isEmptyData = getArguments().getBoolean(IS_EMPTY_KEY);
            this.emptyData = getArguments().getStringArrayList(EMPTY_DATA_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        initSystemInterface(rootView);
        initInterface(rootView, orientation);
        initListener(rootView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_profile_toolbar, menu);
        this.toolbar = menu;
        super.onCreateOptionsMenu(menu, inflater);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Toolbar User is inflated");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.app_bar_edit) {
            if (item.isChecked()) {
                AppUtils.log(Thread.currentThread().getStackTrace(), "User profile is not more under edit");

                // Restore icon
                item.setIcon(R.drawable.ic_edit);
                setEditIconVisibility(false);
                setInputTextEnable(false);
                item.setChecked(false);

                // show empty data message
                isEmptyData();
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "User profile is under edit");

                // Activate edit icons
                item.setIcon(R.drawable.ic_clear);
                setEditIconVisibility(true);
                setInputTextEnable(true);
                item.setChecked(true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Camera permission granted from Gym profile");
                AppUtils.message(this.messageAnchor, getResources().getString(R.string.permission_camera_success), Snackbar.LENGTH_SHORT).show();

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, MY_CAMERA_REQUEST_CODE);
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Camera permission not authorized from Gym profile");
                AppUtils.message(this.messageAnchor, getResources().getString(R.string.permission_camera_error), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppUtils.log(Thread.currentThread().getStackTrace(), "Activity result: " + requestCode + " " + resultCode);

        if (requestCode == MY_ADDRESS_REQUEST_CODE && !(data == null)) {
            if(resultCode == ActivityUserProfile.RESULT_OK) {
                final Place place = Autocomplete.getPlaceFromIntent(data);
                final TextInputEditText view = this.editTextMap.get("address");
                if (view != null) view.setText(place.getAddress());
                this.tempTextMap.replace("address", place.getAddress());
            }
        } else if (requestCode == MY_GALLERY_REQUEST_CODE && !(data == null)) {
            if(resultCode == ActivityUserProfile.RESULT_OK) {
                this.emptyData.remove("img");

                if (data.getData() != null) setAndUploadNewImage(data.getData());
            }
        } else if (requestCode == MY_CAMERA_REQUEST_CODE && !(data == null)) {
            if(resultCode == ActivityUserProfile.RESULT_OK) {
                this.emptyData.remove("img");

                final Bundle extras = data.getExtras();
                final Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) setAndUploadNewImage(imageBitmap);
            }
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
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
                AppUtils.restartActivity((ActivityUserProfile) requireActivity());
            }
        }
    }

    private void onAddButtons() {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Fab status: " + !circleBtnClicked);

        setVisibility(this.circleBtnClicked);
        setAnimation(this.circleBtnClicked);
        setCircleBtnClickable(this.circleBtnClicked);
        this.circleBtnClicked = !this.circleBtnClicked;
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull final View rootView) {
        // init new checked item on navigation Drawer
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_user);
        navigationView.getMenu().findItem(R.id.nav_menu_home).setChecked(true);

        // Abilities toolbar item options
        setHasOptionsMenu(true);

        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.user_profile_toolbar_title));

        // init origin screen orientation
        this.orientation = rootView.getResources().getConfiguration().orientation;

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentUserProfile initialized");
    }

    /**
     * Take from User all fields that will be add into layout XML file fields
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     * @param orientation Orientation value to separate the setting of layout XML field
     */
    private void initInterface(@NonNull final View rootView, final int orientation) {
        final String[] usersKey = getResources().getStringArray(R.array.user_field);

        // Get view objects
        setImageMap(rootView, orientation);
        setAnimationMap(rootView);
        setFabsMap(rootView);
        setEditTextMap(rootView);
        setInputTextMap(rootView);
        setSaveButtonMap(rootView);
        setDeleteButtonMap(rootView);
        setTempTextMap();

        // Set view objects
        if (this.imagesMap.containsKey("mainImage")) Picasso.get().load(this.user.getImg()).into(this.imagesMap.get("mainImage"));
        Picasso.get().load(this.user.getImg()).into(this.imagesMap.get("smallImage"));
        Picasso.get().load(this.user.getImg()).into(this.imagesMap.get("drawerImage"));

        ((MaterialTextView)
            ((NavigationView) requireActivity().findViewById(R.id.navigation_user))
                .getHeaderView(0).findViewById(R.id.header_user_name))
                .setText(this.user.getFullname());

        this.editTextMap.get(usersKey[1]).setText(this.user.getFullname());
        this.editTextMap.get(usersKey[5]).setText(this.user.getEmail());
        this.editTextMap.get(usersKey[7]).setText(this.user.getPhone());
        this.editTextMap.get(usersKey[9]).setText(this.user.getAddress());
        this.editTextGender.setText(this.user.getGender());

        final Calendar dateOfBirthday = Calendar.getInstance();
        dateOfBirthday.setTime(this.user.getDateOfBirthday());
        final String birthdayText = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateOfBirthday.getTime());
        this.editTextMap.get(usersKey[4]).setText(birthdayText);

        isEmptyData();

        AppUtils.log(Thread.currentThread().getStackTrace(), "View interface of FragmentUserProfile initialized");
    }

    /**
     * Add and set all listener event and actions for all input field, fab and layout components.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initListener(@NonNull final View rootView) {
        final String[] usersKey = getResources().getStringArray(R.array.user_field);
        final AtomicBoolean isVisibleGender = new AtomicBoolean(false);

        /* Floating Action Buttons listener */
        this.fabsMap.forEach((key, fab) -> fab.setOnClickListener(v -> {
            if (!key.equals("main") && !key.equals("editMainImage")) {
                setVisibility(false);
                setAnimation(false);
                setCircleBtnClickable(false);
                this.circleBtnClicked = false;
            }

            switch (key) {
                case "subscription":
                    AppUtils.startFragment((ActivityUserProfile) requireActivity(), FragmentUserListGyms.newInstance(this.user), true);
                    break;
                case "turns":
                    AppUtils.startFragment((ActivityUserProfile) requireActivity(), FragmentUserMainTurn.newInstance(this.user), true);
                    break;
                case "main":
                    onAddButtons();
                    break;
                case "editMainImage":
                    setPickImageDialog();
                    break;
            }

        }));

        /* LayoutText listener */
        this.inputTextMap.forEach((key, field) -> {
            switch (key) {
                case "name":
                    field.setOnClickListener(v -> inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[1])),
                            rootView.findViewById(R.id.buttons_name)));

                    field.setEndIconOnClickListener(v -> inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[1])),
                            rootView.findViewById(R.id.buttons_name)));
                    break;
                case "email":
                    field.setOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[5])),
                                getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.buttons_email));
                        Objects.requireNonNull(this.editTextMap.get(usersKey[5])).setText("");
                    });

                    field.setEndIconOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get("email")),
                                getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.buttons_email));
                        Objects.requireNonNull(this.editTextMap.get(usersKey[5])).setText("");
                    });
                    break;
                case "key":
                    field.setOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get("key")),
                                getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.buttons_key));
                        Objects.requireNonNull(this.editTextMap.get("key")).setText("");
                    });

                    field.setEndIconOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get("key")),
                                getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.buttons_key));
                        Objects.requireNonNull(this.editTextMap.get("key")).setText("");
                    });
                    break;
                case "phone":
                    field.setOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[7])),
                                getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.buttons_phone));
                        Objects.requireNonNull(this.editTextMap.get(usersKey[7])).setText("");
                    });

                    field.setEndIconOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[7])),
                                getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.buttons_phone));
                        Objects.requireNonNull(this.editTextMap.get(usersKey[7])).setText("");
                    });
                    break;
                case "address":
                    field.setOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[9])),
                                getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.buttons_address));
                        Objects.requireNonNull(this.editTextMap.get(usersKey[9])).setText("");
                    });

                    field.setEndIconOnClickListener(v -> {
                        inputFieldFocused(field, Objects.requireNonNull(this.editTextMap.get(usersKey[9])),
                                getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.buttons_address));
                        Objects.requireNonNull(this.editTextMap.get(usersKey[9])).setText("");
                    });
                    break;
                case "gender":
                    field.setOnClickListener(v -> {
                        if (!isVisibleGender.get()) {
                            inputFieldFocused(field, this.editTextGender);
                            isVisibleGender.set(true);
                        } else {
                            inputFieldDispatch(field);
                            isVisibleGender.set(false);
                        }
                    });

                    field.setEndIconOnClickListener(v -> {
                        if (!isVisibleGender.get()) {
                            inputFieldFocused(field, this.editTextGender);
                            isVisibleGender.set(true);
                        } else {
                            inputFieldDispatch(field);
                            isVisibleGender.set(false);
                        }
                    });
                    break;
            }
        });

        /* EditText listener */
        this.editTextMap.forEach((key, field) -> {
            switch (key) {
                case "name":
                    field.setOnClickListener(v -> inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[1])),
                            field, rootView.findViewById(R.id.buttons_name)));

                    field.setOnFocusChangeListener((v, hasFocus) -> {
                        if(hasFocus) {
                            inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[1])),
                                    field, rootView.findViewById(R.id.buttons_name));
                        } else {
                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[1])),
                                    field, this.user.getFullname(), true, rootView.findViewById(R.id.buttons_name));

                            Objects.requireNonNull(this.inputTextMap.get(usersKey[1])).clearFocus();
                            field.clearFocus();
                        }
                    });

                    field.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if(!s.toString().isEmpty() && !(s.toString().equals(user.getFullname()))) {
                                tempTextMap.replace(usersKey[1], s.toString());
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    field.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            // hide virtual keyboard
                            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(field.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                            return true;
                        }
                        return false;
                    });
                    break;
                case "email":
                    field.setOnClickListener(v -> inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[5])),
                            field, getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.buttons_email)));

                    field.setOnFocusChangeListener((v, hasFocus) -> {
                        if(hasFocus) {
                            inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[5])),
                                    field, getResources().getString(R.string.helper_email_hover), rootView.findViewById(R.id.buttons_email));
                            field.setText("");
                        } else {
                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[5])),
                                    field, this.user.getEmail(), false, rootView.findViewById(R.id.buttons_email));
                            Objects.requireNonNull(this.inputTextMap.get(usersKey[5])).clearFocus();
                            field.clearFocus();
                        }
                    });

                    field.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if(!s.toString().isEmpty() && !(s.toString().equals(user.getEmail()))) {
                                tempTextMap.replace(usersKey[5], s.toString());
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    field.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            // hide virtual keyboard
                            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(field.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                            return true;
                        }
                        return false;
                    });
                    break;
                case "key":
                    field.setOnClickListener(v -> inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get("key")),
                            field, getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.buttons_key)));

                    field.setOnFocusChangeListener((v, hasFocus) -> {
                        if(hasFocus) {
                            inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get("key")),
                                    field, getResources().getString(R.string.helper_psw_hover), rootView.findViewById(R.id.buttons_key));
                            field.setText("");
                        } else {
                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get("key")),
                                    field, getResources().getString(R.string.password_hide), false, rootView.findViewById(R.id.buttons_key));
                            Objects.requireNonNull(this.inputTextMap.get("key")).clearFocus();
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

                    field.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            // hide virtual keyboard
                            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(field.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                            return true;
                        }
                        return false;
                    });
                    break;
                case "phone":
                    field.setOnClickListener(v -> inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])),
                            field, getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.buttons_phone)));

                    field.setOnFocusChangeListener((v, hasFocus) -> {
                        if(hasFocus) {
                            inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])),
                                    field, getResources().getString(R.string.helper_phone_hover), rootView.findViewById(R.id.buttons_phone));
                            field.setText("");
                        } else {
                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])),
                                    field, this.user.getPhone(), false, rootView.findViewById(R.id.buttons_phone));
                            Objects.requireNonNull(this.inputTextMap.get(usersKey[7])).clearFocus();
                            field.clearFocus();
                        }
                    });

                    field.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if(!s.toString().isEmpty() && !(s.toString().equals(user.getPhone()))) {
                                tempTextMap.replace(usersKey[7], s.toString());
                            }

                            final TextInputLayout view = inputTextMap.get(usersKey[7]);
                            if (view != null && (s.toString().length() > view.getCounterMaxLength())) {
                                Objects.requireNonNull(inputTextMap.get(usersKey[7])).setError(getResources().getString(R.string.helper_phone_error));
                            } else {
                                Objects.requireNonNull(inputTextMap.get(usersKey[7])).setError(null);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    field.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            // hide virtual keyboard
                            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(field.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                            return true;
                        }
                        return false;
                    });
                    break;
                case "address":
                    field.setOnClickListener(v -> inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[9])),
                            field, getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.buttons_address)));

                    field.setOnFocusChangeListener((v, hasFocus) -> {
                        if(hasFocus) {
                            inputFieldFocused(Objects.requireNonNull(this.inputTextMap.get(usersKey[9])),
                                    field, getResources().getString(R.string.helper_address_hover), rootView.findViewById(R.id.buttons_address));
                            field.setText("");
                            final List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                            final Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(requireContext());
                            startActivityForResult(intent, MY_ADDRESS_REQUEST_CODE);
                        } else {
                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[9])),
                                    field, this.user.getAddress(), false, rootView.findViewById(R.id.buttons_address));
                            Objects.requireNonNull(this.inputTextMap.get(usersKey[9])).clearFocus();
                            field.clearFocus();
                        }
                    });

                    field.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            // hide virtual keyboard
                            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(field.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                            return true;
                        }
                        return false;
                    });
                    break;
                case "dateOfBirthday":
                    field.setOnClickListener(v -> {
                        final Calendar calendar = Calendar.getInstance();
                        new DatePickerDialog(rootView.getContext(), getDefaultDatePickerData(),
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH))
                                .show();
                    });
            }
        });

        /* EditText Gender listener */
        this.editTextGender.setOnClickListener(v -> {
            if (!isVisibleGender.get()) {
                inputFieldFocused(this.inputTextMap.get(usersKey[6]), this.editTextGender);
                isVisibleGender.set(true);
            } else {
                inputFieldDispatch(this.inputTextMap.get(usersKey[6]));
                isVisibleGender.set(false);
            }
        });

        this.editTextGender.setOnItemClickListener((parent, view, position, id) -> {
            final String itemSelected = this.editTextGender.getText().toString();

            if (!itemSelected.equals(this.tempTextMap.get(usersKey[6]))) {
                final List<String> userGenders = Arrays.asList(getResources().getStringArray(R.array.genders_name));

                DatabaseUtils.updateUserField(this.user.getUid(), usersKey[6], userGenders.get(position), ((data, result) -> {
                    this.user.setGender(itemSelected);
                    this.tempTextMap.replace(usersKey[6], itemSelected);
                    this.emptyData.remove(usersKey[6]);
                }));

                AppUtils.log(Thread.currentThread().getStackTrace(), "New gender is updated on Database and User object");
                AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_gender_success), Snackbar.LENGTH_SHORT).show();
            }

            if (!isVisibleGender.get()) {
                inputFieldFocused(inputTextMap.get(usersKey[6]), editTextGender);
                isVisibleGender.set(true);
            } else {
                inputFieldDispatch(inputTextMap.get(usersKey[6]));
                isVisibleGender.set(false);
            }

        });

        /* Save Buttons listener */
        this.saveButtonMap.forEach((key, btn) -> {
            switch (key) {
                case "name":
                    btn.setOnClickListener(v -> {
                        final List<String> fullnameList = Arrays.asList(String.valueOf(this.tempTextMap.get(usersKey[1])).split(" "));
                        final String name = fullnameList.get(0);

                        final StringJoiner joiner = new StringJoiner(" ");
                        for (String s : fullnameList.subList(1, fullnameList.size())) {
                            joiner.add(s);
                        }
                        final String surname = String.valueOf(joiner);
                        final String fullname = name + " " + surname;

                        DatabaseUtils.updateUserField(this.user.getUid(), usersKey[1], name, ((data, result) -> {
                            if (result == DatabaseUtils.RESULT_OK) {
                                this.user.setName(name);
                                this.emptyData.remove(usersKey[1]);
                            }
                        }));
                        DatabaseUtils.updateUserField(this.user.getUid(), usersKey[2], surname, ((data, result) -> {
                            if (result == DatabaseUtils.RESULT_OK) {
                                this.user.setName(surname);
                                this.emptyData.remove(usersKey[2]);
                            }
                        }));
                        DatabaseUtils.updateUserField(this.user.getUid(), usersKey[3], fullname, ((data, result) -> {
                            if (result == DatabaseUtils.RESULT_OK) {
                                this.user.setFullname(fullname);
                            }
                        }));

                        inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[1])), Objects.requireNonNull(this.editTextMap.get(usersKey[1])),
                                fullname, true, rootView.findViewById(R.id.buttons_name));

                        AppUtils.log(Thread.currentThread().getStackTrace(), "New name is updated on Database and User object");
                        AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_name_success), Snackbar.LENGTH_SHORT).show();

                        ((MaterialTextView)
                                ((NavigationView) requireActivity().findViewById(R.id.navigation_user))
                                .getHeaderView(0).findViewById(R.id.header_user_name))
                                .setText(fullname);
                    });
                    break;
                case "email":
                    btn.setOnClickListener(v -> {
                        final String email = String.valueOf(this.tempTextMap.get(usersKey[5]));

                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(email)
                                .addOnSuccessListener(aVoid -> {
                                    DatabaseUtils.updateUserField(this.user.getUid(), usersKey[5], email, ((data, result) -> {
                                        if (result == DatabaseUtils.RESULT_OK) {
                                            this.user.setEmail(email);
                                            this.emptyData.remove(usersKey[5]);
                                        }
                                    }));
                                    inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[5])), Objects.requireNonNull(this.editTextMap.get(usersKey[5])),
                                            email, true, rootView.findViewById(R.id.buttons_email));

                                    AppUtils.log(Thread.currentThread().getStackTrace(), "New email is updated on Database and User object");
                                    AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_email_success), Snackbar.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[5])), Objects.requireNonNull(this.editTextMap.get(usersKey[5])),
                                            this.user.getEmail(), false, rootView.findViewById(R.id.buttons_email));

                                    AppUtils.log(Thread.currentThread().getStackTrace(), "New email is deleted");
                                    AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_email_error), Snackbar.LENGTH_SHORT).show();
                                });
                    });
                    break;
                case "key":
                    btn.setOnClickListener(v -> {
                        final String psw = String.valueOf(this.tempTextMap.get("key"));

                        FirebaseAuth.getInstance().getCurrentUser().updatePassword(psw)
                                .addOnSuccessListener(aVoid -> {
                                    inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get("key")), Objects.requireNonNull(this.editTextMap.get("key")),
                                            String.valueOf(this.tempTextMap.get("key")), false, rootView.findViewById(R.id.buttons_key));

                                            AppUtils.log(Thread.currentThread().getStackTrace(), "New key is updated on Database and Gym object");
                                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_password_success), Snackbar.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                        inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get("key")), Objects.requireNonNull(this.editTextMap.get("key")),
                                        getResources().getString(R.string.password_hide), false, rootView.findViewById(R.id.buttons_key));

                                        AppUtils.log(Thread.currentThread().getStackTrace(), "New key is deleted");
                                        AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_password_error), Snackbar.LENGTH_SHORT).show();
                        });
                    });
                    break;
                case "phone":
                    btn.setOnClickListener(v -> {
                        if(isValidPhoneNumber(String.valueOf(this.tempTextMap.get(usersKey[7])))) {
                            final String phone = String.valueOf(this.tempTextMap.get(usersKey[7]));

                            DatabaseUtils.updateUserField(this.user.getUid(), usersKey[7], phone, ((data, result) -> {
                                this.user.setPhone(phone);
                                this.emptyData.remove(key);
                            }));

                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])), Objects.requireNonNull(this.editTextMap.get(usersKey[7])),
                                    String.valueOf(this.tempTextMap.get(usersKey[7])), false, rootView.findViewById(R.id.buttons_phone));

                            AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is updated on Database and Gym object");
                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_phone_success), Snackbar.LENGTH_SHORT).show();
                        } else {
                            inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])), Objects.requireNonNull(this.editTextMap.get(usersKey[7])),
                                    this.user.getPhone(), false, rootView.findViewById(R.id.buttons_phone));

                            AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is deleted");
                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_phone_error), Snackbar.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case "address":
                    btn.setOnClickListener(v -> {
                        final String address = String.valueOf(this.tempTextMap.get(usersKey[9]));
                        DatabaseUtils.updateUserField(this.user.getUid(), usersKey[9], address, ((data, result) -> {
                            this.user.setAddress(String.valueOf(this.tempTextMap.get(usersKey[9])));
                            this.emptyData.remove(key);
                        }));

                        inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[9])), Objects.requireNonNull(this.editTextMap.get(usersKey[9])),
                                String.valueOf(this.tempTextMap.get(usersKey[9])), false, rootView.findViewById(R.id.buttons_address));

                        AppUtils.log(Thread.currentThread().getStackTrace(), "New address is updated on Database and Gym object");
                        AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_address_success), Snackbar.LENGTH_SHORT).show();
                    });
                    break;
            }
        });

        /* Delete Buttons listener */
        this.deleteButtonMap.forEach((key, btn) -> {
            switch (key) {
                case "name":
                    btn.setOnClickListener(v -> inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[1])), Objects.requireNonNull(this.editTextMap.get(usersKey[1])),
                            this.user.getFullname(), true, rootView.findViewById(R.id.buttons_name)));
                    break;
                case "email":
                    btn.setOnClickListener(v -> inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[5])), Objects.requireNonNull(this.editTextMap.get(usersKey[5])),
                            this.user.getEmail(), true, rootView.findViewById(R.id.buttons_email)));
                    break;
                case "key":
                    btn.setOnClickListener(v -> inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get("key")), Objects.requireNonNull(this.editTextMap.get("key")),
                            getResources().getString(R.string.password_hide), false, rootView.findViewById(R.id.buttons_key)));
                    break;
                case "phone":
                    btn.setOnClickListener(v -> inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])), Objects.requireNonNull(this.editTextMap.get("phone")),
                            this.user.getPhone(), false, rootView.findViewById(R.id.buttons_phone)));
                    break;
                case "address":
                    btn.setOnClickListener(v -> inputFieldDispatch(Objects.requireNonNull(this.inputTextMap.get(usersKey[7])), Objects.requireNonNull(this.editTextMap.get(usersKey[7])),
                            this.user.getAddress(), false, rootView.findViewById(R.id.buttons_address)));
                    break;
            }
        });

        AppUtils.log(Thread.currentThread().getStackTrace(), "Listener of interface of FragmentUserProfile initialized");
    }

    // Image methods

    /**
     * Set map with ImageView and CircleImageView views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     * @param orientation Orientation value to separate the setting of map
     */
    private void setImageMap(@NonNull final View rootView, final int orientation) {
        final NavigationView navigationView = requireActivity().findViewById(R.id.navigation_user);
        final View viewHeader = navigationView.getHeaderView(0);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) this.imagesMap.put("mainImage", rootView.findViewById(R.id.main_image));
        this.imagesMap.put("smallImage", rootView.findViewById(R.id.small_image));
        this.imagesMap.put("drawerImage", viewHeader.findViewById(R.id.header_user_image));
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
     */
    private void setAndUploadNewImage(@NonNull final Uri data) {
        if (this.imagesMap.containsKey("mainImage")) Picasso.get().load(data).into(this.imagesMap.get("mainImage"));
        Picasso.get().load(data).into(this.imagesMap.get("smallImage"));
        Picasso.get().load(data).into(this.imagesMap.get("drawerImage"));

        StorageReference storageReference = this.storage.getReference().child("img/users/" + this.user.getUid() + "/profilePic");
        storageReference.putFile(data)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    final String uriString = uri.toString();
                    this.user.setImg(data.toString());
                    DatabaseUtils.updateUserImg(this.user.getUid(), uriString, ((data1, result) -> {
                        if (result == DatabaseUtils.RESULT_OK) {
                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_image_success), Snackbar.LENGTH_SHORT).show();
                            AppUtils.log(Thread.currentThread().getStackTrace(), "New image is updated on Storage and User object");
                        } else {
                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_image_error), Snackbar.LENGTH_SHORT).show();
                        }
                    }));
                }))
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "New image is deleted");
                    AppUtils.message(this.messageAnchor, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });

    }

    /**
     * Set views with Bitmap image and upload it into Database and Storage
     *
     * @param data Bitmap of image taken from Camera that will be used to store image into ImageViews, Database and Storage
     */
    private void setAndUploadNewImage(@NonNull final Bitmap data) {
        if (this.imagesMap.containsKey("mainImage")) ((ImageView) Objects.requireNonNull(this.imagesMap.get("mainImage"))).setImageBitmap(data);
        ((CircleImageView) Objects.requireNonNull(this.imagesMap.get("smallImage"))).setImageBitmap(data);
        ((CircleImageView) Objects.requireNonNull(this.imagesMap.get("drawerImage"))).setImageBitmap(data);

        Objects.requireNonNull(this.imagesMap.get("smallImage")).setDrawingCacheEnabled(true);
        Objects.requireNonNull(this.imagesMap.get("smallImage")).buildDrawingCache();
        final Bitmap bitmap = Objects.requireNonNull(this.imagesMap.get("smallImage")).getDrawingCache();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] bytes = baos.toByteArray();

        StorageReference storageReference = this.storage.getReference().child("img/users/" + this.user.getUid() + "/profilePic");
        storageReference.putBytes(bytes)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    final String uriString = uri.toString();
                    this.user.setImg(data.toString());
                    DatabaseUtils.updateUserImg(this.user.getUid(), uriString, ((data1, result) -> {
                        if (result == DatabaseUtils.RESULT_OK) {
                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_image_success), Snackbar.LENGTH_SHORT).show();
                            AppUtils.log(Thread.currentThread().getStackTrace(), "New image is updated on Storage and User object");
                        } else {
                            AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_image_error), Snackbar.LENGTH_SHORT).show();
                        }
                    }));
                }))
                .addOnFailureListener(e -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "New image is deleted");
                    AppUtils.message(this.messageAnchor, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        });
    }

    // Texts

    /**
     * Set map with layut text views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setInputTextMap(@NonNull final View rootView) {
        this.inputTextMap.put("name", rootView.findViewById(R.id.input_name));
        this.inputTextMap.put("email", rootView.findViewById(R.id.input_email));
        this.inputTextMap.put("key", rootView.findViewById(R.id.input_key));
        this.inputTextMap.put("phone", rootView.findViewById(R.id.input_phone));
        this.inputTextMap.put("address", rootView.findViewById(R.id.input_address));
        this.inputTextMap.put("gender", rootView.findViewById(R.id.input_gender));
        this.inputTextMap.put("dateOfBirthday", rootView.findViewById(R.id.input_birthday));

        setEditIconVisibility(false);
    }

    /**
     * Set map with edit text views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setEditTextMap(@NonNull final View rootView) {
        this.editTextMap.put("name", rootView.findViewById(R.id.text_name));
        this.editTextMap.put("email", rootView.findViewById(R.id.text_email));
        this.editTextMap.put("key", rootView.findViewById(R.id.text_key));
        this.editTextMap.put("phone", rootView.findViewById(R.id.text_phone));
        this.editTextMap.put("address", rootView.findViewById(R.id.text_address));
        this.editTextMap.put("dateOfBirthday", rootView.findViewById(R.id.text_birthday));

        final List<String> userGenders = new ArrayList<String>() {
            {
                add(getString(R.string.prompt_gender_man));
                add(getString(R.string.prompt_gender_woman));
                add(getString(R.string.prompt_gender_other));
            }
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(), R.layout.layout_system_dropdown, userGenders);
        this.editTextGender = rootView.findViewById(R.id.text_gender);
        this.editTextGender.setAdapter(adapter);
        this.editTextGender.setThreshold(1);

    }

    /**
     * Set map with temporal edit text views.
     */
    private void setTempTextMap() {
        this.tempTextMap.put("name", "");
        this.tempTextMap.put("email", "");
        this.tempTextMap.put("key", "");
        this.tempTextMap.put("phone", "");
        this.tempTextMap.put("address", "");
        this.tempTextMap.put("dateOfBirthday", "");
        this.tempTextMap.put("gender", this.user.getGender());
    }

    // Buttons

    /**
     * Set map with save buttons views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setSaveButtonMap(@NonNull final View rootView) {
        this.saveButtonMap.put("name", rootView.findViewById(R.id.save_name));
        this.saveButtonMap.put("email", rootView.findViewById(R.id.save_email));
        this.saveButtonMap.put("key", rootView.findViewById(R.id.save_key));
        this.saveButtonMap.put("phone", rootView.findViewById(R.id.save_phone));
        this.saveButtonMap.put("address", rootView.findViewById(R.id.save_address));
    }

    /**
     * Set map with delete buttons views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setDeleteButtonMap(@NonNull final View rootView) {
        this.deleteButtonMap.put("name", rootView.findViewById(R.id.cancel_name));
        this.deleteButtonMap.put("email", rootView.findViewById(R.id.cancel_email));
        this.deleteButtonMap.put("key", rootView.findViewById(R.id.cancel_key));
        this.deleteButtonMap.put("phone", rootView.findViewById(R.id.cancel_phone));
        this.deleteButtonMap.put("address", rootView.findViewById(R.id.cancel_address));
    }

    // Animation methods

    /**
     * Set map with animation resources.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setAnimationMap(@NonNull final View rootView) {
        this.animationsMap.put("rotateOpen", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.rotate_open_anim));
        this.animationsMap.put("rotateClose", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.rotate_close_anim));
        this.animationsMap.put("fromButton", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.from_bottom_anim));
        this.animationsMap.put("toButton", AnimationUtils.loadAnimation(rootView.getContext(), R.anim.to_bottom_anim));
    }

    /**
     * Set map with Floating Buttons views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setFabsMap(@NonNull final View rootView) {
        this.fabsMap.put("main", rootView.findViewById(R.id.fab_add));
        this.fabsMap.put("subscription", rootView.findViewById(R.id.fab_subscription));
        this.fabsMap.put("turns", rootView.findViewById(R.id.fab_turns));
        this.fabsMap.put("editMainImage", rootView.findViewById(R.id.edit_main_img));
    }

    /**
     * Set the visibility of button when the first "fab" is clicked
     *
     * @param clicked truth flag for button visibility
     */
    private void setVisibility(final boolean clicked) {
        if(!clicked) {
            Objects.requireNonNull(this.fabsMap.get("subscription")).setVisibility(View.VISIBLE);
            Objects.requireNonNull(this.fabsMap.get("turns")).setVisibility(View.VISIBLE);
        } else {
            Objects.requireNonNull(this.fabsMap.get("subscription")).setVisibility(View.INVISIBLE);
            Objects.requireNonNull(this.fabsMap.get("turns")).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Set the animation of button when the first "fab" is clicked
     *
     * @param clicked truth flag for button visibility
     */
    private void setAnimation(final boolean clicked) {
        if(!clicked) {
            Objects.requireNonNull(this.fabsMap.get("subscription")).startAnimation(this.animationsMap.get("fromButton"));
            Objects.requireNonNull(this.fabsMap.get("turns")).startAnimation(this.animationsMap.get("fromButton"));
            Objects.requireNonNull(this.fabsMap.get("main")).startAnimation(this.animationsMap.get("rotateOpen"));
        } else {
            Objects.requireNonNull(this.fabsMap.get("subscription")).startAnimation(this.animationsMap.get("toButton"));
            Objects.requireNonNull(this.fabsMap.get("turns")).startAnimation(this.animationsMap.get("toButton"));
            Objects.requireNonNull(this.fabsMap.get("main")).startAnimation(this.animationsMap.get("rotateClose"));
        }
    }

    /**
     * Set the value of truth flag
     *
     * @param circleBtnClicked truth flag for button visibility
     */
    private void setCircleBtnClickable(final boolean circleBtnClicked) {
        if(!circleBtnClicked) {
            Objects.requireNonNull(this.fabsMap.get("subscription")).setClickable(true);
            Objects.requireNonNull(this.fabsMap.get("turns")).setClickable(true);
        } else {
            Objects.requireNonNull(this.fabsMap.get("subscription")).setClickable(false);
            Objects.requireNonNull(this.fabsMap.get("turns")).setClickable(false);
        }
    }

    // Other methods

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

    @NonNull
    private DatePickerDialog.OnDateSetListener getDefaultDatePickerData() {
        final Calendar calendar = Calendar.getInstance();
        return (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateOfBirthday(calendar);
        };
    }

    private void updateDateOfBirthday(@NonNull final Calendar calendar) {
        final String[] usersKey = getResources().getStringArray(R.array.user_field);
        final String dateOfBirthday = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
        this.editTextMap.get(usersKey[4]).setText(dateOfBirthday);

        DatabaseUtils.updateUserDateOfBirthday(this.user.getUid(), calendar.getTime(), ((data, result) -> {
            this.user.setDateOfBirthday(calendar.getTime());
            this.emptyData.remove(usersKey[4]);
        }));

        AppUtils.log(Thread.currentThread().getStackTrace(), "New date of birthday is updated on Database and Gym object");
        AppUtils.message(this.messageAnchor, getResources().getString(R.string.update_birthday_success), Snackbar.LENGTH_SHORT).show();
    }

    private void isEmptyData() {
        if (this.emptyData.isEmpty()) {
            this.isEmptyData = false;
            AppUtils.log(Thread.currentThread().getStackTrace(), "There are not empty data now");
        } else {
            this.isEmptyData = true;
            setActionForEmptyData();
            AppUtils.log(Thread.currentThread().getStackTrace(), "There are empty data yet: " + this.emptyData.toString());
        }
    }

    private boolean isValidPhoneNumber(@NonNull final String number) {
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
            final AtomicBoolean isGreaterOfLimit = new AtomicBoolean(false);
            final TextInputLayout view = this.inputTextMap.get("phone");

            if (view != null) {
                isGreaterOfLimit.set(number.length() <= view.getCounterMaxLength());
            } else {
                isGreaterOfLimit.set(false);
            }
            return isGreaterOfLimit.get();
        } else {
            AppUtils.log(Thread.currentThread().getStackTrace(), "New phone number is not valid");
            return false;
        }
    }

    /**
     * Add at all field TextLayout the end icon when the flag is true, alternately it will be removed
     *
     * @param isVisible truth flag for button visibility
     */
    private void setEditIconVisibility(final boolean isVisible) {
        final String[] userKeys = getResources().getStringArray(R.array.user_field);

        if (isVisible) {
            Objects.requireNonNull(this.fabsMap.get("editMainImage")).setVisibility(View.VISIBLE);
            this.inputTextMap.forEach((key, field) -> {
                field.setEndIconVisible(true);

                if (userKeys[7].equals(key)) {
                    field.setCounterEnabled(true);
                }
            });
        } else {
            Objects.requireNonNull(this.fabsMap.get("editMainImage")).setVisibility(View.INVISIBLE);
            this.inputTextMap.forEach((key, field) -> {
                field.setEndIconVisible(false);

                if (userKeys[7].equals(key)) {
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
    private void setInputTextEnable(final boolean isEnable) {
        if (isEnable) {
            this.inputTextMap.forEach((key, field) -> {
                field.setEnabled(true);
                field.setFocusable(true);
                field.setFocusableInTouchMode(true);
            });
        } else {
            this.inputTextMap.forEach((key, field) -> {
                field.setEnabled(false);
                field.setFocusable(false);
                field.setFocusableInTouchMode(false);
            });
        }
    }

    private void inputFieldFocused(@NonNull final TextInputLayout box, @NonNull final TextInputEditText text, @NonNull final String helperText,
                                   @NonNull final LinearLayout container) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field focused: " + box.getId());

        text.requestFocus();
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.tint_image_icon_light, requireActivity().getTheme())));

        box.setHelperTextEnabled(true);
        box.setHelperText(helperText);
        box.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.tint_helper_text_focused, requireActivity().getTheme())));

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    @SuppressWarnings("unchecked")
    private void inputFieldFocused(@NonNull final TextInputLayout box, @NonNull final AutoCompleteTextView text) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field focused: " + box.getId());

        box.setBoxStrokeWidthFocused(6);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.tint_hint_focuses, requireActivity().getTheme())));
        box.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.tint_hint_focuses, requireActivity().getTheme())));

        if (!text.getText().toString().equals("")) {
            ((ArrayAdapter<String>) text.getAdapter()).getFilter().filter(null);
        }
        text.showDropDown();
    }

    private void inputFieldFocused(@NonNull final TextInputLayout box, @NonNull final TextInputEditText text, @NonNull final LinearLayout container) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field focused: " + box.getId());

        text.requestFocus();
        box.setEndIconDrawable(R.drawable.ic_clear);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.tint_image_icon_light, requireActivity().getTheme())));

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void inputFieldDispatch(@NonNull final TextInputLayout box, @NonNull final TextInputEditText textField, @NonNull final String originText,
                                    final boolean helperEnable, @NonNull final LinearLayout container) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field dispatched: " + box.getId());

        textField.setText(originText);
        textField.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.tint_input_text, requireActivity().getTheme())));

        box.setEndIconDrawable(R.drawable.ic_edit);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.tint_image_icon_light, requireActivity().getTheme())));
        box.setHelperTextEnabled(helperEnable);

        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0));
    }

    private void inputFieldDispatch(@NonNull final TextInputLayout box) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Field dispatched: " + box.getId());

        box.setBoxStrokeWidthFocused(0);
        box.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.tint_image_icon_light, requireActivity().getTheme())));
        box.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.tint_hint_no_focused, requireActivity().getTheme())));
    }

}