package com.example.gymfit.gym.main;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FragmentGymSettings extends Fragment {
    private static final String DESCRIBABLE_KEY = "describable_key";
    private static final String INFO_LOG = "INFO: ";
    private static final String ERROR_LOG = "INFO: ";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Gym gym = null;

    // Switch and Checkbox
    private final Map<String, SwitchMaterial> switches = new HashMap<>();
    private final Map<String, MaterialCheckBox> checkboxParent = new HashMap<>();
    private final Map<String, MaterialCheckBox> checkboxChildes = new HashMap<>();

    // Text and List
    private final Map<String, Object[]> turnList = new HashMap<>();
    private final Map<String, Boolean> isVisibleTurnList = new HashMap<>();

    private View activityView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        assert getArguments() != null;
        this.gym = (Gym) getArguments().getSerializable(DESCRIBABLE_KEY);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_settings, container, false);

        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_settings_toolbar_title));

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.activityView = view.findViewById(R.id.constraintLayout);

        // Switch initialization
        this.switches.put("monthly", view.findViewById(R.id.monthly_subscription_switch));
        this.switches.put("quarterly", view.findViewById(R.id.quarterly_subscription_switch));
        this.switches.put("sixMonth", view.findViewById(R.id.six_month_subscription_switch));
        this.switches.put("annual", view.findViewById(R.id.annual_subscription_switch));

        // Checkbox initialization
        this.checkboxParent.put("morning", view.findViewById(R.id.morning_turn_checkbox));
        this.checkboxChildes.put("morningFirst", view.findViewById(R.id.morning_turn_first_checkbox));
        this.checkboxChildes.put("morningSecond", view.findViewById(R.id.morning_turn_second_checkbox));
        this.checkboxChildes.put("morningThird", view.findViewById(R.id.morning_turn_third_checkbox));
        this.checkboxParent.put("afternoon", view.findViewById(R.id.afternoon_turn_checkbox));
        this.checkboxChildes.put("afternoonFirst", view.findViewById(R.id.afternoon_turn_first_checkbox));
        this.checkboxChildes.put("afternoonSecond", view.findViewById(R.id.afternoon_turn_second_checkbox));
        this.checkboxChildes.put("afternoonThird", view.findViewById(R.id.afternoon_turn_third_checkbox));
        this.checkboxParent.put("evening", view.findViewById(R.id.evening_turn_checkbox));
        this.checkboxChildes.put("eveningFirst", view.findViewById(R.id.evening_turn_first_checkbox));
        this.checkboxChildes.put("eveningSecond", view.findViewById(R.id.evening_turn_second_checkbox));
        this.checkboxChildes.put("eveningThird", view.findViewById(R.id.evening_turn_third_checkbox));

        this.isVisibleTurnList.put("morning", false);
        this.isVisibleTurnList.put("afternoon", false);
        this.isVisibleTurnList.put("evening", false);

        this.turnList.put("morning", new Object[] {
                view.findViewById(R.id.morning_turn_text),
                view.findViewById(R.id.morning_turn_list)
        });
        this.turnList.put("afternoon", new Object[] {
                view.findViewById(R.id.afternoon_turn_text),
                view.findViewById(R.id.afternoon_turn_list)
        });
        this.turnList.put("evening", new Object[] {
                view.findViewById(R.id.evening_turn_text),
                view.findViewById(R.id.evening_turn_list)
        });


        /* Switch comp event */
        for (Map.Entry<String, SwitchMaterial> entry : this.switches.entrySet()) {
            setSwitchStyle(entry.getValue());
            setSwitchFromDatabase(entry.getKey(), entry.getValue());

            // OnCheckedChange
            entry.getValue().setOnCheckedChangeListener((buttonView, isChecked) -> setSwitchOnDatabase(entry.getKey(), isChecked));
        }

        /* Checkbox child comp event */
        for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
            setCheckboxStyle(entry.getValue());
            setCheckboxFromDatabase(entry.getKey(), entry.getValue());

            // OnCheckedChange
            entry.getValue().setOnCheckedChangeListener((buttonView, isChecked) -> {
                setParentIfAllChecked(entry.getKey(), isChecked);
                setCheckboxOnDatabase(entry.getKey(), isChecked);
            });
        }

        /* Checkbox parent comp event */
        for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxParent.entrySet()) {
            setCheckboxStyle(entry.getValue());
            setCheckboxFromDatabase(entry.getKey(), entry.getValue());

            // OnCheckedChange
            entry.getValue().setOnCheckedChangeListener((buttonView, isChecked) -> setCheckboxOnDatabase(entry.getKey(), isChecked));

            // OnCLick
            entry.getValue().setOnClickListener(v -> {
                boolean isVisible = Objects.requireNonNull(this.isVisibleTurnList.get(entry.getKey()));
                LinearLayout list = (LinearLayout) Objects.requireNonNull(this.turnList.get(entry.getKey()))[1];
                TextInputLayout text = (TextInputLayout) Objects.requireNonNull(this.turnList.get(entry.getKey()))[0];

                if (!isVisible) {
                    setListVisibility(list, text, false);
                    this.isVisibleTurnList.put(entry.getKey(), true);
                }
                setCheckboxIfParent(entry.getKey(), entry.getValue().isChecked());

            });
        }

        /* Turn text event */
        for (Map.Entry<String, Object[]> entry : this.turnList.entrySet()) {
            TextInputLayout text = (TextInputLayout) entry.getValue()[0];
            LinearLayout list = (LinearLayout) entry.getValue()[1];

            // OnEndIconClick
            text.setEndIconOnClickListener(v -> {
                boolean isVisible = Objects.requireNonNull(this.isVisibleTurnList.get(entry.getKey()));
                setListVisibility(list, text, isVisible);

                this.isVisibleTurnList.put(entry.getKey(), !isVisible);
            });
        }
    }

    public static FragmentGymSettings newInstance(Gym gym) {
        FragmentGymSettings fragment = new FragmentGymSettings();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    /* Switch methods */

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

    private void setSwitchFromDatabase(String key, SwitchMaterial switchMaterial) {
        this.db.collection("gyms").document(this.gym.getUid()).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Boolean dbValue = (Boolean) documentSnapshot.get("subscription." + key);

                assert dbValue != null;
                switchMaterial.setChecked(dbValue);
                this.gym.setSubscription(key, dbValue);
            } else {
                Log.e(ERROR_LOG, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
    }

    private void setSwitchOnDatabase(String key, boolean isChecked) {
        this.db.collection("gyms").document(this.gym.getUid())
                .update("subscription." + key, isChecked);
        this.gym.setSubscription(key, isChecked);

        showSnackSwitch(key, isChecked);
    }

    /* Checkbox methods */

    private void setCheckboxStyle(MaterialCheckBox materialCheckBox) {
        ColorStateList colorListButton = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled, -android.R.attr.state_checked},
                },
                new int[] {
                        getResources().getColor(R.color.quantum_deeppurpleA700, null),
                        getResources().getColor(R.color.quantum_grey300, null)
                }
        );

        materialCheckBox.setUseMaterialThemeColors(false);
        materialCheckBox.setButtonTintList(colorListButton);

    }

    private void setCheckboxFromDatabase(String key, MaterialCheckBox materialCheckBox) {
        this.db.collection("gyms").document(this.gym.getUid()).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Boolean dbValue;

                if (key.contains("morning")) {
                    dbValue = (Boolean) documentSnapshot.get("turn.morning." + key);
                    assert dbValue != null;
                    materialCheckBox.setChecked(dbValue);
                } else if (key.contains("afternoon")) {
                    dbValue = (Boolean) documentSnapshot.get("turn.afternoon." + key);
                    assert dbValue != null;
                    materialCheckBox.setChecked(dbValue);
                } else if (key.contains("evening")) {
                    dbValue = (Boolean) documentSnapshot.get("turn.evening." + key);
                    assert dbValue != null;
                    materialCheckBox.setChecked(dbValue);
                }

            } else {
                Log.e(ERROR_LOG, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()));
            }
        });
    }

    private void setCheckboxIfParent(String key, boolean isChecked) {
        switch (key) {
            case "morning":
                for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
                    if (entry.getKey().contains("morning")) {
                        entry.getValue().setChecked(isChecked);
                    }
                }
                break;
            case "afternoon":
                for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
                    if (entry.getKey().contains("afternoon")) {
                        entry.getValue().setChecked(isChecked);
                    }
                }
                break;
            case "evening":
                for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
                    if (entry.getKey().contains("evening")) {
                        entry.getValue().setChecked(isChecked);
                    }
                }
                break;
        }
    }

    private void setParentIfAllChecked(String key, boolean isChecked) {
        boolean flag = false;

        if (key.contains("morning")) {
            for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
                if (entry.getKey().contains("morning") && (Boolean.compare(isChecked, entry.getValue().isChecked()) != 0)) {
                    flag = true;
                }
            }

            if (!flag) {
                Objects.requireNonNull(this.checkboxParent.get("morning")).setChecked(isChecked);
            }
        } else if (key.contains("afternoon")) {
            for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
                if (entry.getKey().contains("afternoon") && (Boolean.compare(isChecked, entry.getValue().isChecked()) != 0)) {
                    flag = true;
                }
            }
            if (!flag) {
                Objects.requireNonNull(this.checkboxParent.get("afternoon")).setChecked(isChecked);
            }
        } else if (key.contains("evening")) {
            for (Map.Entry<String, MaterialCheckBox> entry : this.checkboxChildes.entrySet()) {
                if (entry.getKey().contains("evening") && (Boolean.compare(isChecked, entry.getValue().isChecked()) != 0)) {
                    flag = true;
                }
            }
            if (!flag) {
                Objects.requireNonNull(this.checkboxParent.get("evening")).setChecked(isChecked);
            }
        }
    }

    private void setCheckboxOnDatabase(String key, boolean isChecked) {

        // Parent if node: split on correct DB node
        // Childes if nodes: in the first case update all childes in DB, alternately update the selected child
        if (key.contains("morning")) {
            this.db.collection("gyms").document(this.gym.getUid())
                .update("turn.morning." + key, isChecked);
            showSnackCheckbox(key, isChecked);
        } else if (key.contains("afternoon")) {
            this.db.collection("gyms").document(this.gym.getUid())
                .update("turn.afternoon." + key, isChecked);
            showSnackCheckbox(key, isChecked);
        } else if (key.contains("evening")) {
            this.db.collection("gyms").document(this.gym.getUid())
                .update("turn.evening." + key, isChecked);
            showSnackCheckbox(key, isChecked);
        }
    }

    /* Snackbar methods */

    private void showSnackSwitch(String key, boolean value) {
        switch (key) {
            case "monthly":
                if (value) {
                    Snackbar.make(activityView, getResources().getString(R.string.monthly_subscription_enabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.monthly_subscription_enabled));
                } else {
                    Snackbar.make(activityView, getResources().getString(R.string.monthly_subscription_disabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.monthly_subscription_disabled));
                }
                break;
            case "quarterly":
                if (value) {
                    Snackbar.make(activityView, getResources().getString(R.string.quarterly_subscription_enabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.quarterly_subscription_enabled));
                } else {
                    Snackbar.make(activityView, getResources().getString(R.string.quarterly_subscription_disabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.quarterly_subscription_disabled));
                }
                break;
            case "sixMonth":
                if (value) {
                    Snackbar.make(activityView, getResources().getString(R.string.sixMonth_subscription_enabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.sixMonth_subscription_enabled));
                } else {
                    Snackbar.make(activityView, getResources().getString(R.string.sixMonth_subscription_disabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.sixMonth_subscription_disabled));
                }
                break;
            case "annual":
                if (value) {
                    Snackbar.make(activityView, getResources().getString(R.string.annual_subscription_enabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.annual_subscription_enabled));
                } else {
                    Snackbar.make(activityView, getResources().getString(R.string.annual_subscription_disabled), Snackbar.LENGTH_SHORT).show();
                    Log.i(INFO_LOG, getResources().getString(R.string.annual_subscription_disabled));
                }
                break;

        }
    }

    private void showSnackCheckbox(String key, boolean value) {
        if (key.contains("morning")) {
            if (value) {
                Snackbar.make(activityView, getResources().getString(R.string.morning_turn_enabled), Snackbar.LENGTH_SHORT).show();
                Log.i(INFO_LOG, getResources().getString(R.string.morning_turn_enabled));
            } else {
                Snackbar.make(activityView, getResources().getString(R.string.morning_turn_disabled), Snackbar.LENGTH_SHORT).show();
                Log.i(INFO_LOG, getResources().getString(R.string.morning_turn_disabled));
            }
        } else if (key.contains("afternoon")) {
            if (value) {
                Snackbar.make(activityView, getResources().getString(R.string.afternoon_turn_enabled), Snackbar.LENGTH_SHORT).show();
                Log.i(INFO_LOG, getResources().getString(R.string.afternoon_turn_enabled));
            } else {
                Snackbar.make(activityView, getResources().getString(R.string.afternoon_turn_disabled), Snackbar.LENGTH_SHORT).show();
                Log.i(INFO_LOG, getResources().getString(R.string.afternoon_turn_disabled));
            }
        } else if (key.contains("evening")) {
            if (value) {
                Snackbar.make(activityView, getResources().getString(R.string.evening_turn_enabled), Snackbar.LENGTH_SHORT).show();
                Log.i(INFO_LOG, getResources().getString(R.string.evening_turn_enabled));
            } else {
                Snackbar.make(activityView, getResources().getString(R.string.evening_turn_disabled), Snackbar.LENGTH_SHORT).show();
                Log.i(INFO_LOG, getResources().getString(R.string.evening_turn_disabled));
            }
        }
    }

    /* Other methods */

    private void setArrowTurnStyle(TextInputLayout text) {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled},
                        new int[]{},
                },
                new int[] {
                        getResources().getColor(R.color.quantum_grey600, null),
                        getResources().getColor(R.color.quantum_grey600, null)
                }
        );

        text.setEndIconTintList(colorStateList);
    }

    private void setListVisibility(LinearLayout container, TextInputLayout text, boolean isVisible) {

        if (!isVisible) {
            text.setEndIconDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_up, null));
            expandCard(container);
        } else {
            text.setEndIconDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_down, null));
            collapseCard(container);
        }

        setArrowTurnStyle(text);
    }

    private static void expandCard(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(300);
        v.startAnimation(a);
    }

    private static void collapseCard(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(150);
        v.startAnimation(a);
    }

}