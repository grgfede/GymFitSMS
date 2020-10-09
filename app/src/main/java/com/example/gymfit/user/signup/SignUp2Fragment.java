package com.example.gymfit.user.signup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.gymfit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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
                emailSignUp.setText(name);
            }
        });
        //emailSignUp.setText(name);


        return view;
    }

    private void recoverDataFragmentTwo(String emailP, String passwordP, String repeatPasswordP) {
        email = emailSignUp.getText().toString();
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
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(myContext, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(),"ERROR!",Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getActivity(),"Success!",Toast.LENGTH_SHORT).show();
                        }
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