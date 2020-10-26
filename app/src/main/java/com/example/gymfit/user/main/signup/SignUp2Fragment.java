package com.example.gymfit.user.main.signup;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.gymfit.R;
import com.example.gymfit.user.conf.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
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

    public String name;
    public String surname;
    public String phone;
    public String gender;
    public Date dateOfBirth;
    public LatLng location;
    public String address;
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

    public User user = null;

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
        recoverDataFragmentOne();

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
                            user.setUid(uid);
                            user.setEmail(email);
                            writeDb(uid);
                        }
                    }
                });
    }

    private void writeDb(String uid) {
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Fragment finalSignUp = new SignUpProfilePicFragment();
                        Bundle args = new Bundle();
                        saveData(finalSignUp);
                        FragmentManager fragManager = myContext.getSupportFragmentManager();
                        changeFragment(fragManager, finalSignUp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //AGGIUNGERE ERRORE IN CASO DI ERRORE NELLA REGISTRAZIONE
                    }
                });
    }

    private void saveData(Fragment fragment2) {
        User newUser = user;
        Bundle bundle=new Bundle();
        bundle.putSerializable("user", newUser);
        fragment2.setArguments(bundle);
    }

    private void changeFragment(FragmentManager fragManager, Fragment fragment2){
        fragManager.beginTransaction().setCustomAnimations(
                R.anim.enter,  // enter
                R.anim.exit,  // exit
                R.anim.pop_enter,   // popEnter
                R.anim.pop_exit  // popExit
        )
                .replace(R.id.viewPager, fragment2)
                .addToBackStack("frags")
                .commit();
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

    private void recoverDataFragmentOne() {
        Bundle bundle = getArguments();
        user = (User) bundle.getSerializable("user");
        name = user.getName();
        surname = user.getSurname();
        phone = user.getPhone();
        gender = user.getGender();
        dateOfBirth = user.getDateOfBirthday();
        location = user.getLocation();
        address = user.getAddress();
    }
}