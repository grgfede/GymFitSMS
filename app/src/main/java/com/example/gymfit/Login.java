package com.example.gymfit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    EditText emailId, password;
    Button btnLogin;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //INSTANZIO L'OGGETTO FIREBASE PER L'AUTENTICAZIONE
        mFirebaseAuth = FirebaseAuth.getInstance();
        //MI INSTANZIO GLI ELEMENTI
        emailId = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        //CREO UN LISTENER CHE E' SEMPRE IN ASCOLTO SUL CLICK DEL BOTTONE DI LOGIN
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();            //MI SALVO IL VALORE DELLA TEXTBOX CHE CONTIENE LA EMAIL INSERITA DALL'UTENTE
                String pswd = password.getText().toString();            //MI SALVO IL VALORE DELLA TEXTBOX CHE CONTIENE LA PASSWORD INSERITA DALL'UTENTE
                if (email.isEmpty()) {
                    emailId.setError("Attenzione! Inserisci email");
                    emailId.requestFocus();
                } else if (pswd.isEmpty()) {
                    password.setError("Attenzione! Inserisci password");
                    password.requestFocus();
                } else if (!(email.isEmpty()) && !(pswd.isEmpty())) {
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
                                //updateUI(user);
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
        findViewById(R.id.loginParentLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
    }


    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}