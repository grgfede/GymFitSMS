package com.example.gymfit.system.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gymfit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import static android.content.ContentValues.TAG;

public class PasswordRecovery extends AppCompatActivity {

    private TextInputLayout emailL;
    private TextInputEditText txtEmailRecovery;
    private ProgressBar progressBarRecoery;
    private Button submitRecovery;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String emailRecovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        emailL = findViewById(R.id.EmailLayoutRecovery);
        txtEmailRecovery = findViewById(R.id.txtEmailRecovery);
        submitRecovery = findViewById(R.id.btnSubmitRecovery);
        progressBarRecoery = findViewById(R.id.progressBarRecoery);

        //AL CLICK DEL PULSANTE, RECUPERO LA PASSWORD
        submitRecovery.setOnClickListener(v -> {
            progressBarRecoery.setVisibility(View.VISIBLE);
            boolean error = fieldControls();
            if (!error) {
                sendNewPassword();
            }
        });
    }


    private boolean fieldControls() {
        emailRecovery = txtEmailRecovery.getText().toString();
        boolean error = false;
        if (emailRecovery.isEmpty()) {
            emailL.setError("Attenzione! Inserire email valida");
            emailL.requestFocus();
            progressBarRecoery.setVisibility(View.INVISIBLE);
            error = true;
        }
        return error;
    }


    private void sendNewPassword() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.sendPasswordResetEmail(emailRecovery)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                emailL.setError(getString(R.string.error_invalid_email));
                                progressBarRecoery.setVisibility(View.INVISIBLE);
                            } catch (FirebaseAuthInvalidUserException e){
                                emailL.setError(getString(R.string.prompt_email_not_found));
                                progressBarRecoery.setVisibility(View.INVISIBLE);
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        } else {
                            progressBarRecoery.setVisibility(View.INVISIBLE);
                            Toast.makeText(PasswordRecovery.this, "EMAIL INVIATA", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}