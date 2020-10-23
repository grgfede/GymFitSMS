package com.example.gymfit.user.main.signup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.gymfit.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    private FragmentActivity myContext;

    Button btnContinue;
    private TextInputEditText nameSignUp;
    private TextInputEditText surnameSignUp;
    private TextInputEditText phoneSignUp;
    private AutoCompleteTextView genderSignUp;
    private TextInputEditText birthSignUp;
    //private TextInputEditText locationSignUp;

    final Calendar myCalendar = Calendar.getInstance();


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


        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        btnContinue = view.findViewById(R.id.btnContinue);
        nameSignUp = (TextInputEditText) view.findViewById(R.id.txtNameSignUp);
        surnameSignUp = (TextInputEditText) view.findViewById(R.id.txtSurnameSignUp);
        phoneSignUp = (TextInputEditText) view.findViewById(R.id.txtPhoneSignUp);
        birthSignUp = (TextInputEditText) view.findViewById(R.id.txtBirthSignUp);
        genderSignUp = (AutoCompleteTextView) view.findViewById(R.id.txtGenderSignUp);

        //CREO IL CALENDARIO
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        //CREO EVENTO PER FAR COMPARIRE IL CALENDARIO
        birthSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //INIZIALIZZO LA STRINGA PER IL LUOGO
        //placesInitialize(view);
        //INIZIALIZZO IL CAMPO DEL GENDER
        setAutoComplete(view);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //FACCIO CONTROLLI SUI CAMPI INSERITI DALL'UTENTE
                boolean errors = controlFields(nameSignUp, surnameSignUp, phoneSignUp, genderSignUp, view);

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
        });
        return view;
    }

    //METODO CHE AGGIORNA EDIT TEXT DEL CALENDARIO
    private void updateLabel() {
        String myFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        birthSignUp.setText(sdf.format(myCalendar.getTime()));
    }

    private void changeFragment(FragmentManager fragManager, Fragment fragment2) {
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
        args.putString("gender", genderSignUp.getText().toString());
        args.putString("birth", birthSignUp.getText().toString());
        fragment2.setArguments(args);
    }


    private boolean controlFields(EditText nameSignUp, EditText surnameSignUp, EditText phoneSignUp, AutoCompleteTextView genderSignUp, View view) {
        //MI RECUPERO I LAYOUT DI OGNI COMPONENTE
        TextInputLayout nameL, surnameL, phoneL, genderL;
        nameL = view.findViewById(R.id.txtNameSignUpLayout);
        surnameL = view.findViewById(R.id.txtSurnameSignUpLayout);
        phoneL = view.findViewById(R.id.txtPhoneSignUpLayout);
        genderL = view.findViewById(R.id.txtGenderSignUpLayout);

        boolean error_fields = false;
        String strname = nameSignUp.getText().toString();
        String strsurname = surnameSignUp.getText().toString();
        String phone = phoneSignUp.getText().toString();
        String gender = genderSignUp.getText().toString();
        if (strname.isEmpty()) {
            nameL.setError("Attenzione! Inserisci nome");
            nameL.requestFocus();
            error_fields = true;
        } else if (strsurname.isEmpty()) {
            surnameSignUp.setError("Attenzione! Inserisci cognome");
            surnameL.requestFocus();
            error_fields = true;
        } else if (phone.isEmpty()) {
            phoneSignUp.setError("Attenzione! Inserisci telefono");
            phoneL.requestFocus();
            error_fields = true;
        } else if (gender.isEmpty()) {
            genderSignUp.setError("Attenzione! Inserisci sesso");
            genderL.requestFocus();
            error_fields = true;
        }
        return error_fields;
    }

    /*private void placesInitialize(View view) {
        Places.initialize(view.getContext(), getResources().getString(R.string.map_key));
        PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }*/


    //METODO CHE AGGIUNGE I VALORI UOMO/DONNA NEL CAMPO DEL SESSO
    private void setAutoComplete(View view) {
        String[] COUNTRIES = new String[]{"Uomo", "Donna"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getContext(),
                        R.layout.dropdown_menu_popup_item,
                        COUNTRIES);

        AutoCompleteTextView editTextFilledExposedDropdown = view.findViewById(R.id.txtGenderSignUp);
        editTextFilledExposedDropdown.setAdapter(adapter);
    }
}
