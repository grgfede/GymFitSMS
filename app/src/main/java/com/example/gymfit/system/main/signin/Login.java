package com.example.gymfit.system.main.signin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.gymfit.R;
import com.example.gymfit.gym.main.ActivityGymProfile;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.system.main.ActivitySystemOnBoarding;
import com.example.gymfit.system.main.PasswordRecovery;
import com.example.gymfit.system.main.signup.GymSignUp;
import com.example.gymfit.system.main.signup.SignUp;
import com.example.gymfit.user.main.ActivityUserProfile;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText emailId, password;
    private final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    private SharedPreferences preferences;

    private View messageAnchor;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_login);

        initSystemInterface();
        initInterface();

        // To close automatically keyboard on click on screen
        this.messageAnchor.setOnClickListener(this::hideKeyboard);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Login activity created.");
    }

    // Set interface

    private void initSystemInterface() {
        // its used to support vector in Android API less then 21
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // used to anchor Snackbar messages at interface layout xml
        this.messageAnchor = findViewById(R.id.loginParentLayout);

        // get system preferences to init onboard initialization
        this.preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);

        if (!this.preferences.getBoolean("onboarding_complete", false)) {
            final Intent onboarding = new Intent(this, ActivitySystemOnBoarding.class);
            startActivity(onboarding);
            finish();
        }
        // if the "onboard" key of system preference is null means that this is first time of login for User,
        // otherwise just skip login and start correct activity
        else {
            final String uid = this.preferences.getString("uid", null);
            if (uid != null) {
                signInIntent(uid);
            }
        }
    }

    private void initInterface() {
        this.emailId = findViewById(R.id.txtEmail);
        this.password = findViewById(R.id.txtPassword);
        final TextView forgotPsw = findViewById(R.id.txtForgotPsw);
        final Button btnLogin = findViewById(R.id.btnLogin);

        forgotPsw.setOnClickListener(v -> {
            final Intent intent = new Intent(Login.this, PasswordRecovery.class);
            startActivity(intent);
        });
        btnLogin.setOnClickListener(v -> {
            final String email = emailId.getText().toString();
            final String psw = password.getText().toString();
            if (email.isEmpty()) {
                // TODO: create a message into message.xml
                this.emailId.setError("Attenzione! Inserisci email");
                this.emailId.requestFocus();
            } else if (psw.isEmpty()) {
                // TODO: create a message into message.xml
                this.password.setError("Attenzione! Inserisci password");
                this.password.requestFocus();
            } else {
                this.mFirebaseAuth.signInWithEmailAndPassword(email, psw)
                        .addOnCompleteListener(Login.this, task -> {
                            if (task.isSuccessful()) {
                                final FirebaseUser user = this.mFirebaseAuth.getCurrentUser();
                                final String uid = user != null ? user.getUid() : null;
                                if (uid != null) {
                                    AppUtils.log(Thread.currentThread().getStackTrace(), "Logging successfully with Firebase Auth.");
                                    signInIntent(user.getUid());
                                }
                            } else {
                                AppUtils.log(Thread.currentThread().getStackTrace(), "Logging failed with Firebase Auth.");
                            }
                        }).addOnFailureListener(e -> this.emailId.setError(getResources().getString(R.string.prompt_email_not_found)));
            }
        });
    }

    // Other methods

    private void signInIntent(@NonNull final String userUid) {
        DatabaseUtils.isUserContains(userUid, ((isUserContained, resultUser) -> {
            if (resultUser == DatabaseUtils.RESULT_OK && isUserContained != null) {
                if (isUserContained) {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User found: " + userUid);
                    AppUtils.message(this.messageAnchor, getString(R.string.user_logged), Snackbar.LENGTH_SHORT).show();

                    final Intent intent = new Intent(Login.this, ActivityUserProfile.class);
                    this.preferences.edit().putString("uid", userUid).apply();
                    intent.putExtra("uid", userUid);
                    startActivity(intent);
                } else {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "User not found: " + userUid);
                }
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Users not found");
            }
        }));
        DatabaseUtils.isGymContains(userUid, ((isGymContained, resultGym) -> {
            if (resultGym == DatabaseUtils.RESULT_OK && isGymContained != null) {
                if (isGymContained) {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym found: " + userUid);
                    AppUtils.message(this.messageAnchor, getString(R.string.user_logged), Snackbar.LENGTH_SHORT).show();

                    final Intent intent = new Intent(Login.this, ActivityGymProfile.class);
                    this.preferences.edit().putString("uid", userUid).apply();
                    intent.putExtra("uid", userUid);
                    startActivity(intent);
                } else {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gym not found: " + userUid);
                }
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found");
            }
        }));
    }

    public void signUpIntent(@NonNull final View v) {
        startActivity(new Intent(Login.this, SignUp.class));
    }

    public void signUpGymIntent(@NonNull final View v) {
        startActivity(new Intent(Login.this, GymSignUp.class));
    }

    private void hideKeyboard(@NonNull final View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}