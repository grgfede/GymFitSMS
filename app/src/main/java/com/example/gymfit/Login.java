package com.example.gymfit;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymfit.gym.GymProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {

    EditText emailId, password;
    TextView signUp;
    Button btnLogin;
    FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).hide(); //NASCONDO LA TITLE BAR


        mFirebaseAuth = FirebaseAuth.getInstance(); //INSTANZIO L'OGGETTO FIREBASE PER L'AUTENTICAZIONE
        //MI INSTANZIO GLI ELEMENTI
        emailId = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        signUp = findViewById(R.id.textCreate);
        btnLogin = findViewById(R.id.btnLogin);

        //CREO UN LISTENER CHE E' SEMPRE IN ASCOLTO SUL CLICK DEL BOTTONE DI LOGIN
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
                //startActivity(new Intent(Login.this, GymProfile.class));

            String email = emailId.getText().toString();            //MI SALVO IL VALORE DELLA TEXTBOX CHE CONTIENE LA EMAIL INSERITA DALL'UTENTE
            String pswd = password.getText().toString();            //MI SALVO IL VALORE DELLA TEXTBOX CHE CONTIENE LA PASSWORD INSERITA DALL'UTENTE
                if (email.isEmpty()) {
                    emailId.setError("Attenzione! Inserisci email");
                    emailId.requestFocus();
                } else if (pswd.isEmpty()) {
                    password.setError("Attenzione! Inserisci password");
                    password.requestFocus();
                } else if (!(email.isEmpty()) && !(pswd.isEmpty())) {
                    Toast.makeText(Login.this, "ENTRATA IF.",
                            Toast.LENGTH_SHORT).show();
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
                                //TODO: UserProfile (Execution)
                                //TODO: GymProfile (Under Test)
                                startActivity(new Intent(Login.this, GymProfile.class));
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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void signUpIntent(View v){
        startActivity(new Intent(Login.this, SignUp.class));
    }


}