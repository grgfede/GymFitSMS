package com.example.gymfit.user.signup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.gymfit.R;
import com.example.gymfit.system.MainActivity;
import com.example.gymfit.user.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUp2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUp2Fragment extends Fragment {

    private FragmentActivity myContext;

    public static String name;
    public String surname;
    public String phone;
    public String email;
    public String password;
    public String repeatPassword;


    //OGGETTI CHE RECUPERO DAL LAYOUT
    EditText emailSignUp;
    EditText passwordSignUp;
    EditText repeatPasswordSignUp;
    Button btnFinishSignUp;


    FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUp2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUp2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUp2Fragment newInstance(String param1, String param2) {
        SignUp2Fragment fragment = new SignUp2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up2, container, false);

        emailSignUp = view.findViewById(R.id.txtEmailSignUp);
        passwordSignUp = view.findViewById(R.id.txtPasswordSignUp);
        repeatPasswordSignUp = view.findViewById(R.id.txtRepeatPasswordSignUp);
        btnFinishSignUp = view.findViewById(R.id.btn_register);
        //RECUPERO I VALORI PASSATI DAL FRAGMENT 1
        recoverDataFragmentOne(name, surname, phone);
        btnFinishSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //RECUPERO I DATI INSERITI DALL'UTENTE NEL FRAGMENT 2
                recoverDataFragmentTwo(email, password, repeatPassword);
            }
        });
        return view;
    }

    private void recoverDataFragmentTwo(String emailP, String passwordP, String repeatPasswordP) {
        email = emailSignUp.getText().toString().trim();
        password = passwordSignUp.getText().toString();
        repeatPassword = repeatPasswordSignUp.getText().toString();

        //ESEGUO CONTROLLO SUI CAMPI INSERITI DALL'UTENTE NEL FRAGMENT 2
        boolean errors_fields = controlFields(emailSignUp, passwordSignUp, repeatPasswordSignUp);
        boolean error_passwords = false;
        //SE NON CI SONO ERRORI NEI CAMPI, CONTROLLO SE LE DUE PASSWORD COINCIDONO
        if (!(errors_fields)) {
            error_passwords = controlPasswords(password, repeatPassword);
            if (!(error_passwords)) {
                doSignUp();
            }
        }

    }

    private void doSignUp() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        final ProgressBar progressBar = myContext.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        //create user
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(myContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                passwordSignUp.setError(getString(R.string.error_weak_password));
                                passwordSignUp.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                emailSignUp.setError(getString(R.string.error_invalid_email));
                                emailSignUp.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                emailSignUp.setError(getString(R.string.error_user_exists));
                                emailSignUp.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        } else {
                            String uid = mFirebaseAuth.getUid();
                            User user = new User(name, surname, phone, email, uid);
                            writeDb(user, uid);
                        }
                    }
                });
    }

    private void writeDb(User user, String uid) {
        db = FirebaseFirestore.getInstance();
        Map<String, String> newUser = new HashMap<>();
        newUser.put("name", user.getName());
        newUser.put("username", user.getSurname());
        newUser.put("phoneNumber", user.getPhone());
        newUser.put("email", user.getEmail());
        newUser.put("uid", user.getUid());
        db.collection("users").document(uid).set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //TODO: Far apparire il fragment di avvenuta registrazione
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast errorToast = Toast.makeText(getContext(), "Errore durante la registrazione. Riprovare più tardi", Toast.LENGTH_LONG);
                        errorToast.show();
                        getActivity().onBackPressed();
                    }
                });


    }

    private boolean controlPasswords(String password, String repassword) {
        boolean error = false;
        if (!(password.equals(repassword))) {
            passwordSignUp.setError("Attenzione! Le due password non coincidono");
            repeatPasswordSignUp.setError("Attenzione! Le due password non coincidono");
            passwordSignUp.requestFocus();
            error = true;
        }
        return error;
    }

    private boolean controlFields(EditText emailSignUp, EditText passwordSignUp, EditText repeatPasswordSignUp) {
        //VARIABILI DI CLASSE CHE CONTERRANNO IL TESTO CON LE INFO SCRITTE DALL'UTENTE
        String email = emailSignUp.getText().toString();
        String password = passwordSignUp.getText().toString();
        String repeatPassword = repeatPasswordSignUp.getText().toString();

        boolean error = false;
        if (email.isEmpty()) {
            emailSignUp.setError("Attenzione! Inserisci email");
            emailSignUp.requestFocus();
            error = true;
        } else if (password.isEmpty()) {
            passwordSignUp.setError("Attenzione! Inserisci password");
            passwordSignUp.requestFocus();
            error = true;
        } else if (repeatPassword.isEmpty()) {
            repeatPasswordSignUp.setError("Attenzione! Inserisci password");
            repeatPasswordSignUp.requestFocus();
            error = true;
        }
        return error;
    }

    private void recoverDataFragmentOne(String nameP, String surnameP, String phoneP) {
        name = getArguments().getString("name");
        surname = getArguments().getString("surname");
        phone = getArguments().getString("phone");
    }
}