package com.example.gymfit.user.main.signup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
import com.example.gymfit.gym.main.ActivityGymProfile;
import com.example.gymfit.user.conf.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private TextInputEditText locationSignUp;


    private LatLng locationLatLng;

    private static final int MY_ADDRESS_REQUEST_CODE = 100;
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
        locationSignUp = (TextInputEditText) view.findViewById(R.id.txtLocationSignUp);

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
                    //SALVO I DATI DA PASSARE AL SECONDO FRAGMENT
                    saveData(fragment2, fragManager);
                    //FACCIO IL CAMBIO DEL FRAGMENT
                    changeFragment(fragManager, fragment2);
                }
            }
        });



        //CREO EVENTO PER FAR COMPARIRE IL CALENDARIO
        birthSignUp.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        locationSignUp.setOnFocusChangeListener((v, hasFocus) -> {

            if(hasFocus) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(requireContext());
                startActivityForResult(intent, MY_ADDRESS_REQUEST_CODE);
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

    private void saveData(Fragment fragment2, FragmentManager fragmentManager) {
        String name, surname, phone, gender, position;
        Date dateOfBirth = null;
        name = nameSignUp.getText().toString();
        surname = surnameSignUp.getText().toString();
        phone = phoneSignUp.getText().toString();
        gender = genderSignUp.getText().toString();
        position = locationSignUp.getText().toString();


        User newUser = new User(name, surname, gender, dateOfBirth, locationLatLng, position, phone);

        Bundle bundle=new Bundle();
        bundle.putSerializable("user", newUser);

        fragment2.setArguments(bundle);
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
            nameL.setError(getResources().getString(R.string.helper_name_hover));
            nameL.requestFocus();
            error_fields = true;
        } else if (strsurname.isEmpty()) {
            surnameSignUp.setError(getResources().getString(R.string.helper_surname_hover));
            surnameL.requestFocus();
            error_fields = true;
        } else if (phone.isEmpty()) {
            phoneSignUp.setError(getResources().getString(R.string.helper_phone_error));
            phoneL.requestFocus();
            error_fields = true;
        } else if (gender.isEmpty()) {
            genderSignUp.setError(getResources().getString(R.string.helper_gender_hover));
            genderL.requestFocus();
            error_fields = true;
        }
        return error_fields;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_ADDRESS_REQUEST_CODE && !(data == null)) {
            if (resultCode == ActivityGymProfile.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationSignUp.setText(place.getAddress());
                locationLatLng = place.getLatLng();
            }
        }
    }

    //METODO CHE AGGIUNGE I VALORI UOMO/DONNA NEL CAMPO DEL SESSO
    private void setAutoComplete(View view) {
        String[] COUNTRIES = new String[]{"Uomo", "Donna"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getContext(),
                        R.layout.layout_system_dropdown,
                        COUNTRIES);

        AutoCompleteTextView editTextFilledExposedDropdown = view.findViewById(R.id.txtGenderSignUp);
        editTextFilledExposedDropdown.setAdapter(adapter);
    }
}
