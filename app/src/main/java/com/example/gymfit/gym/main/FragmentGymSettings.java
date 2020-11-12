package com.example.gymfit.gym.main;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.OnTurnDialogCallback;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.BooleanUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

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

    private Gym gym;

    // Switch and Checkbox
    private final Map<String, SwitchMaterial> switchMap = new HashMap<>();
    private final Map<String, LinearLayout> checkMap = new HashMap<>();
    private final Map<String, MaterialTextView> checkTextMap = new HashMap<>();

    private View messageAnchor = null;

    public static FragmentGymSettings newInstance(@NonNull final Gym gym) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentGymSettings created");

        final FragmentGymSettings fragment = new FragmentGymSettings();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(GYM_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        assert getArguments() != null;
        this.gym = (Gym) getArguments().getSerializable(GYM_KEY);

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_gym_settings, container, false);

        initSystemInterface(rootView);
        initInterface(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentGymSettings layout XML created");

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresher);
        refreshLayout.setColorSchemeResources(R.color.tint_refresher,
                R.color.tint_refresher_first, R.color.tint_refresher_second, R.color.tint_refresher_third);

        // if pull down with gesture refresh all available gyms adapter
        refreshLayout.setOnRefreshListener(() -> {
            AppUtils.message(this.messageAnchor, getString(R.string.refresh_gym_settings), Snackbar.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                refreshLayout.setRefreshing(false);
                refreshSubscriptionsStatus();
                refreshTurnsStatus();

                AppUtils.message(this.messageAnchor, getString(R.string.refresh_completed), Snackbar.LENGTH_SHORT).show();
                AppUtils.log(Thread.currentThread().getStackTrace(), "Refresh turns and subscriptions.");
            }, AppUtils.getRandomDelayMillis());
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppUtils.log(Thread.currentThread().getStackTrace(), "Orientation changed: " + newConfig.orientation);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            try {
                final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
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
    private void initSystemInterface(@NonNull final View rootView) {
        // init new checked item on navigation Drawer
        final NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);
        navigationView.getMenu().findItem(R.id.nav_menu_setting).setChecked(true);

        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_settings_toolbar_title));

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentGymSettings initialized");
    }

    /**
     * Initialize switch and checkbox map, listener and actions
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initInterface(@NonNull final View rootView) {
        setSwitchMap(rootView);
        setCheckMap(rootView);
        setCheckboxTextMap(rootView);

        // View listener
        /* Switch listener */
        this.switchMap.forEach((key, entry) -> {
            setSwitchStyle(entry);
            setSwitchStatus(key, entry);

            // OnCheckedChange
            entry.setOnCheckedChangeListener((buttonView, isChecked) -> setSwitchOnDatabase(key, isChecked));
        });

        /* Button listener */
        this.checkMap.forEach((key, entry) -> {
            setCheckboxStatus(key);

            final String[] configString = new String[] {
                    "title", "category", "items", "checkedItems"
            };
            final String[] gymsKey = getResources().getStringArray(R.array.gym_field);
            final Map<String, Object> confCheckDialog = new HashMap<>();
            // OnClick
            entry.setOnClickListener(v -> {
                switch (key) {
                    case "morning":
                        confCheckDialog.put(configString[0], rootView.getResources().getString(R.string.morning_session));
                        confCheckDialog.put(configString[1], rootView.getResources().getString(R.string.prompt_morning));
                        confCheckDialog.put(configString[2], rootView.getResources().getStringArray(R.array.morning_session_value));
                        confCheckDialog.put(configString[3], Arrays.stream(Objects.requireNonNull(this.gym.getTurns().get(gymsKey[14]))).collect(BooleanUtils.TO_BOOLEAN_ARRAY));
                        break;
                    case "afternoon":
                        confCheckDialog.put(configString[0], rootView.getResources().getString(R.string.afternoon_session));
                        confCheckDialog.put(configString[1], rootView.getResources().getString(R.string.prompt_afternoon));
                        confCheckDialog.put(configString[2], rootView.getResources().getStringArray(R.array.afternoon_session_value));
                        confCheckDialog.put(configString[3], Arrays.stream(Objects.requireNonNull(this.gym.getTurns().get(gymsKey[15]))).collect(BooleanUtils.TO_BOOLEAN_ARRAY));
                        break;
                    case "evening":
                        confCheckDialog.put(configString[0], rootView.getResources().getString(R.string.evening_session));
                        confCheckDialog.put(configString[1], rootView.getResources().getString(R.string.prompt_evening));
                        confCheckDialog.put(configString[2], rootView.getResources().getStringArray(R.array.evening_session_value));
                        confCheckDialog.put(configString[3], Arrays.stream(Objects.requireNonNull(this.gym.getTurns().get(gymsKey[16]))).collect(BooleanUtils.TO_BOOLEAN_ARRAY));
                        break;
                }

                onCreateTurnDialog(rootView, confCheckDialog, this::setCheckboxOnDatabase);
            });
        });

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentGymSettings interface laid.");
    }

    // Switch methods

    /**
     * Set map with Switch views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setSwitchMap(@NonNull final View rootView) {
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
    private void setSwitchStyle(@NonNull final SwitchMaterial switchMaterial) {
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
    private void setSwitchStatus(@NonNull final String key, @NonNull final SwitchMaterial switchMaterial) {
        this.gym.getSubscription().forEach((subscription, isEnable) -> {
            if (subscription.equals(key)) {
                switchMaterial.setChecked(isEnable);
                AppUtils.log(Thread.currentThread().getStackTrace(), "Subscription " + key + " is init with: " + isEnable);
            }
        });
    }

    /**
     * Set the respective Database subscription node with the value of Switch passed
     *
     * @param key String used for access on the correct node child of database subscription node
     * @param isChecked Value used for update the respective Database node
     */
    private void setSwitchOnDatabase(@NonNull final String key, final boolean isChecked) {
        DatabaseUtils.updateGymSubscription(this.gym.getUid(), key, isChecked, ((data, result) -> {
            if (result == DatabaseUtils.RESULT_OK) {
                this.gym.setSubscription(key, isChecked);
            }
        }));


    }

    // Checkbox methods

    /**
     * Set map with Material Button views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setCheckMap(@NonNull final View rootView) {
        this.checkMap.put("morning", rootView.findViewById(R.id.morning_turn));
        this.checkMap.put("afternoon", rootView.findViewById(R.id.afternoon_turn));
        this.checkMap.put("evening", rootView.findViewById(R.id.evening_turn));
    }

    /**
     * Set map with Material TextView views.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setCheckboxTextMap(@NonNull final View rootView) {
        this.checkTextMap.put("morning", rootView.findViewById(R.id.morning_turn_result));
        this.checkTextMap.put("afternoon", rootView.findViewById(R.id.afternoon_turn_result));
        this.checkTextMap.put("evening", rootView.findViewById(R.id.evening_turn_result));
    }

    /**
     * Set Database and Gym object on the respective turn node with current check
     *
     * @param category String used for access on the correct node child of database turn node
     * @param turnKey String used for access on the correct node child of database turn node
     * @param isChecked Boolean used for set the respective turn node
     */
    private void setCheckboxOnDatabase(@NonNull final String category, @NonNull final String turnKey, final boolean isChecked) {
        final List<String> turnKeys = Arrays.asList(AppUtils.getTurnKeysFromCategory(category));
        final int which = turnKeys.indexOf(turnKey);
        DatabaseUtils.updateGymTurn(this.gym.getUid(), category, turnKey, isChecked, ((data, result) -> {
            if (result == DatabaseUtils.RESULT_OK) {
                this.gym.setTurn(category, which, isChecked);
                setCheckboxStatus(category);
            }
        }));

    }

    /**
     * Set the current Gym object with the content of Database turns node
     *
     * @param key String used for access on the correct node child of database turn node
     */
    private void setCheckboxStatus(@NonNull final String key) {
        this.gym.getTurns().forEach((turn, isEnableArray) -> {
            if (turn.equals(key)) {
                final List<String> checkedTurnList = new ArrayList<>();
                final String[] turnValues = AppUtils.getTurnValuesFromCategory(key);
                for (int i=0; i<isEnableArray.length; i++) {
                    if (isEnableArray[i]) {
                        checkedTurnList.add(turnValues[i]);
                    }
                }
                setCheckboxText(key, checkedTurnList);
            }
        });

        AppUtils.log(Thread.currentThread().getStackTrace(), "Gym turns fields updated from Database values");
    }

    /**
     * Create and set a dialog for user to show a multi choice checkbox. After choice, the value of selected checkbox will be storage into Database and Gym using callback method.
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     * @param confDialog Map with all items, checked, title for dialog configuration
     * @param callback callback method used to set Gym and Database
     */
    private void onCreateTurnDialog(@NonNull final View rootView, @NonNull final Map<String, Object> confDialog, @NonNull final OnTurnDialogCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(rootView.getContext());
        builder.setTitle((String) confDialog.get("title"));
        builder.setMultiChoiceItems(
                (String[]) confDialog.get("items"),
                (boolean[]) confDialog.get("checkedItems"),
                (dialog, which, isChecked) -> {
                    final String[] turnKey = AppUtils.getTurnKeysFromCategory(String.valueOf(confDialog.get("category")));
                    callback.onCallback(String.valueOf(confDialog.get("category")), turnKey[which], isChecked);
                });
        builder.setPositiveButton(rootView.getResources().getString(R.string.prompt_confirm), (dialog, which) -> {});
        builder.setNegativeButton(rootView.getResources().getString(R.string.prompt_delete), (dialog, which) -> {});
        builder.create();
        builder.show();
    }

    /**
     * Set the Material Text View respective of turn node with the new placeholder. It contains only text of true checkbox
     *
     * @param checkedTurnList Strings as List of only turn value checked
     */
    private void setCheckboxText(@NonNull final String category, @NonNull final List<String> checkedTurnList) {
        Collections.sort(checkedTurnList);
        final StringJoiner joiner = new StringJoiner(", ");
        for (String s : checkedTurnList) {
            joiner.add(s);
        }

        this.checkTextMap.get(category).setText(joiner.toString());
    }

    private void refreshSubscriptionsStatus() {
        DatabaseUtils.getGym(this.gym.getUid(), ((data, result) -> {
            if (result == DatabaseUtils.RESULT_OK) {
                this.gym.setSubscriptions(data.getSubscription());
                this.switchMap.forEach(this::setSwitchStatus);
            }
        }));
    }

    private void refreshTurnsStatus() {
        DatabaseUtils.getGym(this.gym.getUid(), ((data, result) -> {
            if (result == DatabaseUtils.RESULT_OK) {
                this.gym.setTurns(data.getTurns());
                this.checkMap.forEach((key, entry) -> setCheckboxStatus(key));
            }
        }));
    }

}