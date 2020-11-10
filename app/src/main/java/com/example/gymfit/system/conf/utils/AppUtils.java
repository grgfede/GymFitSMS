package com.example.gymfit.system.conf.utils;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gymfit.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class AppUtils {
    private static final String LOG = "KEY_LOG";

    // Utils

    /**
     * Start a new Fragment from a current Activity or Fragment.
     *
     * @param activity Activity object instance of FragmentActivity which contains Fragment
     * @param fragment Fragment object to commit
     * @param isAddedToStack authorize this transaction to the back stack.
     */
    public static void startFragment(AppCompatActivity activity, @NotNull Fragment fragment, boolean isAddedToStack) {
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        if (isAddedToStack) {
            transaction.addToBackStack(AppCompatActivity.class.getSimpleName());
            transaction.setReorderingAllowed(true);
        } else {
            transaction.disallowAddToBackStack();
        }
        transaction.replace(R.id.fragment_container_view_tag, fragment).commit();
    }

    /**
     * Override actual activity with same. It means that activity will be destroyed and restored.
     *
     * @param activity Activity object to restart
     */
    public static void restartActivity(AppCompatActivity activity) {
        Intent intent = activity.getIntent();
        activity.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
    }

    /**
     * Expand a View container from height 0
     *
     * @param view View object to expand
     */
    public static void expandCard(@NotNull final View view) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(300);
        view.startAnimation(a);
    }

    /**
     * Collapse a View container at height 0
     *
     * @param view View object to expand
     */
    public static void collapseCard(@NotNull final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(150);
        view.startAnimation(a);
    }

    // Users class

    /**
     * This methods converts all turn key and turn values strings as lists.
     * So find from value list the correspondents key passed by param and return its.
     *
     * @param key database turn key (morningFirst, afternoonSecond...) used to find correspondents a turn value (10:00 - 11:00, 17:00-18:00)
     * @return turn value as String
     */
    @NonNull
    public static String getTurnValueFromKey(@NonNull final String key) {
        final List<String> keys = new LinkedList<String>() {
            {
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.morning_session_name)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.afternoon_session_name)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.evening_session_name)));
            }
        };
        final List<String> values = new LinkedList<String>() {
            {
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.morning_session_value)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.afternoon_session_value)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.evening_session_value)));
            }
        };

        return values.get(keys.indexOf(key));
    }

    /**
     * This methods converts all turn key and turn values strings as lists.
     * So find from key list the correspondents value passed by param and return its.
     *
     * @param value database turn value (10:00 - 11:00, 17:00-18:00) used to find correspondents a turn key (morningFirst, afternoonSecond...)
     * @return turn value as String
     */
    @NonNull
    public static String getTurnKeyFromValue(@NonNull final String value) {
        final List<String> keys = new LinkedList<String>() {
            {
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.morning_session_name)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.afternoon_session_name)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.evening_session_name)));
            }
        };
        final List<String> values = new LinkedList<String>() {
            {
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.morning_session_value)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.afternoon_session_value)));
                addAll(Arrays.asList(ResourceUtils.getStringArrayFromID(R.array.evening_session_value)));
            }
        };

        return keys.get(values.indexOf(value));
    }

    /**
     * This methods converts all turn keys (of morning, afternoon and evening) as a list of string arrays and all category in another string array.
     * So find, in a loop, the correspondents category of which passed by param and return its keys.
     * In the end (with all negative responses) return a string array fill of "null" string value.
     *
     * @param category project turn key (morning, afternoon, evening) used to find correspondents a same turn keys (morningFirst, morningSecond, morningThird)
     * @return turn value as String array
     */
    @NonNull
    public static String[] getTurnKeysFromCategory(@NonNull final String category) {
        final String[] types = new String[] {"morning", "afternoon", "evening"};
        final List<String[]> keys = new LinkedList<String[]>() {
            {
                add(ResourceUtils.getStringArrayFromID(R.array.morning_session_name));
                add(ResourceUtils.getStringArrayFromID(R.array.afternoon_session_name));
                add(ResourceUtils.getStringArrayFromID(R.array.evening_session_name));
            }
        };

        for (int i=0; i<types.length; i++) {
            if (types[i].equals(category)) {
                return keys.get(i);
            }
        }
        return new String[] {"null", "null", "null"};
    }

    @NonNull
    public static String getCategoryFromTurnKey(@NonNull final String key) {
        final String[] gymKeys = ResourceUtils.getStringArrayFromID(R.array.gym_field);

        final List<String> types = new ArrayList<String>() {
            {
                add(gymKeys[14]);
                add(gymKeys[15]);
                add(gymKeys[16]);
            }
        };

        final AtomicReference<String> type = new AtomicReference<>();
        types.stream().filter(key::contains).findFirst()
                .ifPresent(type::set);

        return type.get();
    }

    // Log and Message

    /**
     * Create a simple Snackbar
     *
     * @param viewAnchor View object of layout XML where Snackbar will be attached
     * @param text message to show with Snackbar
     * @param duration constant for determinate Snackbar duration
     * @return Snackbar object obtained
     */
    public static Snackbar message(View viewAnchor, String text, int duration) {
        Snackbar snackbar = Snackbar.make(viewAnchor, text, duration).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);

        // Set max lines
        View snackView = snackbar.getView();
        MaterialTextView tv = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(3);

        MaterialButton btn = snackView.findViewById(com.google.android.material.R.id.snackbar_action);
        ColorStateList myColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}
                },
                new int[] {
                        ResourceUtils.getColorFromID(R.color.background_ripple_message),
                        ResourceUtils.getColorFromID(R.color.background_message),
                }
        );
        btn.setRippleColor(myColorStateList);

        return snackbar;
    }

    /**
     * Show a Debug Log
     *
     * @param stackTrace array of stack' elements
     * @param text message to show with Log
     */
    public static void log(StackTraceElement[] stackTrace, String text) {
        int lineNumber = stackTrace[2].getLineNumber();
        String methodName = stackTrace[2].getMethodName();
        String className = stackTrace[2].getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);

        String message = className + " " + methodName + " " + "[" + lineNumber + "]: " + text;
        Log.d(LOG, message);
    }

}
