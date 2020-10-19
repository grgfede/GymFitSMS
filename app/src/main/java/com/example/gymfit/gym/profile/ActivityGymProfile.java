package com.example.gymfit.gym.profile;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gymfit.R;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.textfield.TextInputEditText;

public class ActivityGymProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), getResources().getString(R.string.map_key));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_profile);
        openFragment();

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

    private void openFragment() {
        FragmentGymProfile fragment = new FragmentGymProfile();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_slide_up, R.anim.exit_slide_down, R.anim.enter_slide_up, R.anim.exit_slide_down);
        transaction.replace(R.id.fragment_container_view_tag, fragment).commit();
    }

}