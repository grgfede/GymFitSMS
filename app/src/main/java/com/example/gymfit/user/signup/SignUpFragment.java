package com.example.gymfit.user.signup;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.gymfit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    private FragmentActivity myContext;

    Button btnContinue;
    EditText nameSignUp;
    EditText surnameSignUp;
    EditText phoneSignUp;
    Spinner genderSignUp;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
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
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
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
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        btnContinue = view.findViewById(R.id.btnContinue);
        nameSignUp = view.findViewById(R.id.txtNameSignUp);
        surnameSignUp = view.findViewById(R.id.txtSurnameSignUp);
        phoneSignUp = view.findViewById(R.id.txtPhoneSignUp);
        genderSignUp = view.findViewById(R.id.spinnerGender);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //FACCIO CONTROLLI SUI CAMPI INSERITI DALL'UTENTE
                boolean errors = controlFields(nameSignUp, surnameSignUp, phoneSignUp, genderSignUp);

                if (!(errors)) {
                    Fragment fragment2 = new SignUp2Fragment();

                    FragmentManager fragManager = myContext.getSupportFragmentManager();


                    Bundle args = new Bundle();

                    //SALVO I DATI DA PASSARE AL SECONDO FRAGMENT
                    saveData(args, fragment2);

                    //FACCIO IL CAMBIO DEL FRAGMENT
                    changeFragment(fragManager, fragment2);
                }
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
            private void saveData(Bundle args, Fragment fragment2) {
                args.putString("name", nameSignUp.getText().toString());
                args.putString("surname", surnameSignUp.getText().toString());
                args.putString("phone", phoneSignUp.getText().toString());
                fragment2.setArguments(args);
            }


            private boolean controlFields(EditText nameSignUp, EditText surnameSignUp, EditText phoneSignUp, Spinner genderSignUp) {
                String name, surname, phone;
                int gender;
                boolean error_fields = false;
                name = nameSignUp.getText().toString();
                surname = surnameSignUp.getText().toString();
                phone = phoneSignUp.getText().toString();
                gender = genderSignUp.getSelectedItemPosition();
                if (name.isEmpty()) {
                    nameSignUp.setError("Attenzione! Inserisci nome");
                    nameSignUp.requestFocus();
                    error_fields = true;
                } else if (surname.isEmpty()) {
                    surnameSignUp.setError("Attenzione! Inserisci cognome");
                    surnameSignUp.requestFocus();
                    error_fields = true;
                } else if (phone.isEmpty()) {
                    phoneSignUp.setError("Attenzione! Inserisci telefono");
                    nameSignUp.requestFocus();
                    error_fields = true;
                }
                return error_fields;
            }
        });
        return view;
    }
}
