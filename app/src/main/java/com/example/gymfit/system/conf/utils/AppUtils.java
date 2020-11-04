package com.example.gymfit.system.conf.utils;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gymfit.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

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
    public static void startFragment(AppCompatActivity activity, Fragment fragment, boolean isAddedToStack) {
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
