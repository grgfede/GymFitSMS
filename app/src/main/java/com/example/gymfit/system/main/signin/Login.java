package com.example.gymfit.system.main.signin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.system.main.ActivitySystemOnBoarding;
import com.example.gymfit.system.main.PasswordRecovery;
import com.example.gymfit.system.main.signup.GymSignUp;
import com.example.gymfit.user.main.ActivityUserProfile;
import com.example.gymfit.system.main.signup.SignUp;
import com.example.gymfit.gym.main.ActivityGymProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    EditText emailId, password;
    TextView signUp, signUpGym;
    TextView forgotPsw;
    Button btnLogin;
    FirebaseAuth mFirebaseAuth;

    private DatabaseUtils dbUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_login);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // CONTROLLO SE L'ONBOARDING DEVE ESSERE VISUALIZZATO SFRUTTANDO LE PREFERENZE DI SISTEMA DELL'APP



        if(dbUtils.isFirstTime()){
            Intent onboarding = new Intent(this, ActivitySystemOnBoarding.class);
            startActivity(onboarding);
            finish();
            return;
        }
        if (dbUtils.isLogged()){
            signInIntent(dbUtils.getUidLogged());
        }

        mFirebaseAuth = FirebaseAuth.getInstance(); //INSTANZIO L'OGGETTO FIREBASE PER L'AUTENTICAZIONE
        //MI INSTANZIO GLI ELEMENTI
        emailId = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        forgotPsw = findViewById(R.id.txtForgotPsw);
        signUp = findViewById(R.id.textCreate);
        signUpGym = findViewById(R.id.textCreate2);
        btnLogin = findViewById(R.id.btnLogin);



        forgotPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, PasswordRecovery.class);
                startActivity(intent);
            }
        });
        //CREO UN LISTENER CHE E' SEMPRE IN ASCOLTO SUL CLICK DEL BOTTONE DI LOGIN
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
            String email = emailId.getText().toString();            //MI SALVO IL VALORE DELLA TEXTBOX CHE CONTIENE LA EMAIL INSERITA DALL'UTENTE
            String pswd = password.getText().toString();            //MI SALVO IL VALORE DELLA TEXTBOX CHE CONTIENE LA PASSWORD INSERITA DALL'UTENTE
                if (email.isEmpty()) {
                    emailId.setError("Attenzione! Inserisci email");
                    emailId.requestFocus();
                } else if (pswd.isEmpty()) {
                    password.setError("Attenzione! Inserisci password");
                    password.requestFocus();
                } else {
                    //SE I CAMPI RICHIESTI SONO CORRETTAMENTE COMPILATI, VADO AD ESEGUIRE IL LOGIN
                    mFirebaseAuth.signInWithEmailAndPassword(email, pswd).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                /*
                                 * SE IL LOGIN VA A BUON FINE, PASSO ALL'ALTRA ACTIVITY
                                 * E MI MEMORIZZO LE INFORMAZIONI DELL'UTENTE IN UN OGGETTO
                                 */
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                Toast.makeText(Login.this, "Autenticazione riuscita.",
                                        Toast.LENGTH_SHORT).show();

                                assert user != null;
                                signInIntent(user.getUid());

                            } else {
                                // SE IL LOGIN NON VA A BUON FINE, MOSTRO UN MESSAGGIO POPUP ALL'UTENTE PER AVVISARLO
                                Toast.makeText(Login.this, "Autenticazione fallita.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        //CODICE CHE PERMETTE DI CHIUDERE LA TASTIERA IN AUTOMATICO QUANDO SI CLICCA SULLO SCHERMO
        findViewById(R.id.loginParentLayout).

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                hideKeyboard(view);
            }
        });
    }

    private void signInIntent(String uid) {
        // TODO: end with _GYM means open Gym Activity, with _USER means open User Activity
        if(uid.endsWith("2") && !uid.endsWith("Xhy2")) {
            startActivity(new Intent(Login.this, ActivityGymProfile.class));
            //SETTO LE IMPOSTAZIONI PER FAR SI CHE ALLA RIAPERTURA DELL'APP, IL LOGIN SIA AUTOMATICO
            dbUtils.setUidLogged(uid);
        } else {
            startActivity(new Intent(Login.this, ActivityUserProfile.class));
            //SETTO LE IMPOSTAZIONI PER FAR SI CHE ALLA RIAPERTURA DELL'APP, IL LOGIN SIA AUTOMATICO
            dbUtils.setUidLogged(uid);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void signUpIntent(View v){
        startActivity(new Intent(Login.this, SignUp.class));
    }

    public void signUpGymIntent(View v){
        startActivity(new Intent(Login.this, GymSignUp.class));
    }


}