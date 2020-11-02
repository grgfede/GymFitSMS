package com.example.gymfit.gym.main;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.InitGymTurnCallback;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.BooleanUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class FragmentGymSettings extends Fragment {
    private static final String GYM_KEY = "gym_key";
    private static final String LOG = FragmentGymSettings.class.getSimpleName();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Gym gym;

    // Switch and Checkbox
    private final Map<String, SwitchMaterial> switchMap = new HashMap<>();
    private final Map<String, MaterialButton> checkButtonMap = new HashMap<>();
    private final Map<String, MaterialTextView> checkTextMap = new HashMap<>();

    private View messageAnchor = null;

    public static FragmentGymSettings newInstance(Gym gym) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentGymSettings created");

        FragmentGymSettings fragment = new FragmentGymSettings();
        Bundle bundle = new Bundle();
        bundle.putSerializable(GYM_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        assert getArguments() != null;
        this.gym = (Gym) getArguments().getSerializable(GYM_KEY);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_settings, container, false);

        initSystemInterface(rootView);

        // View initialization
        setSwitchMap(rootView);
        setCheckButtonMap(rootView);
        setCheckboxTextMap(rootView);

        // View listener
        try {
            /* Switch listener */
            this.switchMap.forEach((key, entry) -> {
                setSwitchStyle(entry);
                setSwitchFromDatabase(key, entry);

                // OnCheckedChange
                entry.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitchOnDatabase(key, isChecked));
            });

            /* Button listener */
            this.checkButtonMap.forEach((key, entry) -> {
                Map<String, Object> confCheckDialog = new HashMap<>();
                setCheckboxFromDatabase(key);

                // OnClick
                entry.setOnClickListener(v -> {
                    switch (key) {
                        case "morning":
                            confCheckDialog.put("title", rootView.getResources().getString(R.string.morning_session));
                            confCheckDialog.put("keyTurn", rootView.getResources().getString(R.string.prompt_morning));
                            confCheckDialog.put("items", rootView.getResources().getStringArray(R.array.morning_session_value));
                            confCheckDialog.put("checkedItems", Arrays.stream(Objects.requireNonNull(this.gym.getTurns().get("morning"))).collect(BooleanUtils.TO_BOOLEAN_ARRAY));
                            break;
                        case "afternoon":
                            confCheckDialog.put("title", rootView.getResources().getString(R.string.afternoon_session));
                            confCheckDialog.put("keyTurn", rootView.getResources().getString(R.string.prompt_afternoon));
                            confCheckDialog.put("items", rootView.getResources().getStringArray(R.array.afternoon_session_value));
                            confCheckDialog.put("checkedItems", Arrays.stream(Objects.requireNonNull(this.gym.getTurns().get("afternoon"))).collect(BooleanUtils.TO_BOOLEAN_ARRAY));
                            break;
                        case "evening":
                            confCheckDialog.put("title", rootView.getResources().getString(R.string.evening_session));
                            confCheckDialog.put("keyTurn", rootView.getResources().getString(R.string.prompt_evening));
                            confCheckDialog.put("items", rootView.getResources().getStringArray(R.array.evening_session_value));
                            confCheckDialog.put("checkedItems", Arrays.stream(Objects.requireNonNull(this.gym.getTurns().get("evening"))).collect(BooleanUtils.TO_BOOLEAN_ARRAY));
                            break;
                    }

                    onCreateTurnDialog(rootView, confCheckDialog, (gym, turnKey, which, isChecked) -> setCheckboxOnDatabase(turnKey, which, isChecked));
                });
            });
        } catch (Exception e) {
            AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
            AppUtils.restartActivity((AppCompatActivity) requireActivity());
        }

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentGymSettings layout XML created");

        return rootView;
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

            } catch (Exception e) {
                AppUtils.log(Thread.currentThread().getStackTrace(), e.getMessage());
                AppUtils.message(this.messageAnchor, e.toString(), Snackbar.LENGTH_SHORT).show();
                AppUtils.restartActivity((AppCompatActivity) requireActivity());
            }
        }
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(View rootView) {
        // Abilities toolbar item options
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_settings_toolbar_title));

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentGymSettings initialized");
    }


    // Switch methods

    /**
     * Set map with Switch views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setSwitchMap(View rootView) {
        this.switchMap.put("monthly", rootView.findViewById(R.id.monthly_subscription_switch));
        this.switchMap.put("quarterly", rootView.findViewById(R.id.quarterly_subscription_switch));
        this.switchMap.put("sixMonth", rootView.findViewById(R.id.six_month_subscription_switch));
        this.switchMap.put("annual", rootView.findViewById(R.id.annual_subscription_switch));
    }

    /**
     * Set the color of thumb and track of current switch view.
     *
     * @param switchMaterial Switch View that will be set in color thumb and track
     */
    private void setSwitchStyle(SwitchMaterial switchMaterial) {
        ColorStateList colorListThumb = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},
                        new int[]{},
                },
                new int[] {
                        getResources().getColor(R.color.quantum_deeppurpleA700, null),
                        getResources().getColor(R.color.quantum_grey100, null)
                }
        );

        ColorStateList colorListTrack = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},
                        new int[]{}
                },
                new int[] {
                        getResources().getColor(R.color.quantum_deeppurpleA100, null),
                        getResources().getColor(R.color.quantum_grey300, null)
                }
        );

        switchMaterial.setUseMaterialThemeColors(false);
        switchMaterial.setThumbTintList(colorListThumb);
        switchMaterial.setTrackTintList(colorListTrack);
    }

    /**
     * Set the current Switch view with the content of Database subscription node
     *
     * @param key String used for access on the correct node child of database subscription node
     * @param switchMaterial Switch view set from database subscription node value
     */
    private void setSwitchFromDatabase(String key, SwitchMaterial switchMaterial) {
        this.db.collection("gyms").document(this.gym.getUid()).get()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Boolean dbValue = documentSnapshot.getBoolean("subscription." + key);
                    assert dbValue != null;
                    switchMaterial.setChecked(dbValue);
                    this.gym.updateSubscription(key, dbValue);
                }
        });

        AppUtils.log(Thread.currentThread().getStackTrace(), "Gym subscriptions fields updated from Database values");
    }

    /**
     * Set the respective Database subscription node with the value of Switch passed
     *
     * @param key String used for access on the correct node child of database subscription node
     * @param isChecked Value used for update the respective Database node
     */
    private void setSwitchOnDatabase(String key, boolean isChecked) {
        this.db.collection("gyms").document(this.gym.getUid()).update("subscription." + key, isChecked)
                .addOnCompleteListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), "Subscription updated on Database with: " + key + " " + isChecked))
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), "Subscription is not updated on Database"));
        this.gym.updateSubscription(key, isChecked);
    }

    // Checkbox methods

    /**
     * Set map with Material Button views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setCheckButtonMap(View rootView) {
        this.checkButtonMap.put("morning", rootView.findViewById(R.id.morning_turn_button));
        this.checkButtonMap.put("afternoon", rootView.findViewById(R.id.afternoon_turn_button));
        this.checkButtonMap.put("evening", rootView.findViewById(R.id.evening_turn_button));
    }

    /**
     * Set map with Material TextView views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setCheckboxTextMap(View rootView) {
        this.checkTextMap.put("morning", rootView.findViewById(R.id.morning_turn_result));
        this.checkTextMap.put("afternoon", rootView.findViewById(R.id.afternoon_turn_result));
        this.checkTextMap.put("evening", rootView.findViewById(R.id.evening_turn_result));
    }

    /**
     * Set Database and Gym object on the respective turn node with current check
     *
     * @param turnKey String used for access on the correct node child of database turn node
     * @param which Integer used for access on the correct node child of database turn sub-node
     * @param isChecked Boolean used for set the respective turn node
     */
    private void setCheckboxOnDatabase(String turnKey, int which, boolean isChecked) {
        String subNode = null;

        if (turnKey.equals(getString(R.string.prompt_morning))) {
            subNode = getResources().getStringArray(R.array.morning_session_name)[which];
        } else if (turnKey.equals(getString(R.string.prompt_afternoon))) {
            subNode = getResources().getStringArray(R.array.afternoon_session_name)[which];
        } else if (turnKey.equals(getString(R.string.prompt_evening))) {
            subNode = getResources().getStringArray(R.array.evening_session_name)[which];
        }

        String finalSubNode = subNode;
        this.db.collection("gyms").document(this.gym.getUid()).update("turn." + turnKey + "." + subNode, isChecked)
                .addOnCompleteListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), "Turn updated on Database with: " + turnKey + "." + finalSubNode + " " + isChecked))
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), "Turn is not updated on Database"));;
        this.gym.setTurn(turnKey, which, isChecked);
        setCheckboxText(turnKey);
    }

    /**
     * Set the current Gym object with the content of Database turns node
     *
     * @param key String used for access on the correct node child of database turn node
     */
    @SuppressWarnings("unchecked")
    private void setCheckboxFromDatabase(String key) {
        this.db.collection("gyms").document(this.gym.getUid()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Map<String, Boolean> dbValue = (Map<String, Boolean>) documentSnapshot.get("turn." + key);
                assert dbValue != null;
                dbValue.forEach((keyTurn, value) -> {
                    int position = 0;

                    if (key.equals(getString(R.string.prompt_morning))) {
                        String[] turnsName = getResources().getStringArray(R.array.morning_session_name);
                        position = Arrays.asList(turnsName).indexOf(keyTurn);
                    } else if (key.equals(getString(R.string.prompt_afternoon))) {
                        String[] turnsName = getResources().getStringArray(R.array.afternoon_session_name);
                        position = Arrays.asList(turnsName).indexOf(keyTurn);
                    } else if (key.equals(getString(R.string.prompt_evening))) {
                        String[] turnsName = getResources().getStringArray(R.array.evening_session_name);
                        position = Arrays.asList(turnsName).indexOf(keyTurn);
                    }
                    this.gym.setTurn(key, position, value);
                    setCheckboxText(key);
                });
            }
        });
        AppUtils.log(Thread.currentThread().getStackTrace(), "Gym turns fields updated from Database values");
    }

    /**
     * Create and set a dialog for user to show a multi choice checkbox. After choice, the value of selected checkbox will be storage into Database and Gym using callback method.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     * @param confDialog Map with all items, checked, title for dialog configuration
     * @param turnDBCallback callback method used to set Gym and Database
     */
    private void onCreateTurnDialog(View rootView, Map<String, Object> confDialog, InitGymTurnCallback turnDBCallback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(rootView.getContext());
        builder.setTitle((String) confDialog.get("title"));
        builder.setMultiChoiceItems((String[]) confDialog.get("items"), (boolean[]) confDialog.get("checkedItems"),
                (dialog, which, isChecked) -> turnDBCallback.onCallback(this.gym, (String) confDialog.get("keyTurn"), which, isChecked));
        builder.setPositiveButton(rootView.getResources().getString(R.string.prompt_confirm), (dialog, which) -> {});
        builder.setNegativeButton(rootView.getResources().getString(R.string.prompt_delete), (dialog, which) -> {});
        builder.create();
        builder.show();
    }

    /**
     * Set the Material Text View respective of turn node with the new placeholder. It contains only text of true checkbox
     *
     * @param key String used for access on the correct node child of database turn node
     */
    private void setCheckboxText(String key) {
        String[] nameTurnArray = null;
        List<String> checkedTurnList = new ArrayList<>();
        Boolean[] checkedTurnArray = this.gym.getTurns().get(key);

        switch (key) {
            case "morning":
                nameTurnArray = getResources().getStringArray(R.array.morning_session_value);
                break;
            case "afternoon":
                nameTurnArray = getResources().getStringArray(R.array.afternoon_session_value);
                break;
            case "evening":
                nameTurnArray = getResources().getStringArray(R.array.evening_session_value);
                break;
        }

        for (int i = 0; i< Objects.requireNonNull(checkedTurnArray).length; i++) {
            if (checkedTurnArray[i]) {
                assert nameTurnArray != null;
                checkedTurnList.add(nameTurnArray[i]);
            }
        }

        Collections.sort(checkedTurnList);
        StringJoiner joiner = new StringJoiner(", ");
        for (String s : checkedTurnList) {
            joiner.add(s);
        }
        String placeholder = joiner.toString();
        Objects.requireNonNull(this.checkTextMap.get(key)).setText(placeholder);
    }

}