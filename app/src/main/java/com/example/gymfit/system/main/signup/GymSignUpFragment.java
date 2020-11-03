package com.example.gymfit.system.main.signup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.main.ActivityGymProfile;
import com.example.gymfit.user.conf.interfaces.GymCallBack;
import com.example.gymfit.user.main.signup.SignUpFinish;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GymSignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GymSignUpFragment extends Fragment {

    private FragmentActivity myContext;

    private static final int SELECT_PHOTO = 1;
    private static final int MY_ADDRESS_REQUEST_CODE = 100;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    Gym gym = null;

    private CircleImageView profilePic;
    private Uri selectedImage;
    private LatLng locationLatLng;
    private Button btnSignUpGym;

    //private String uid;

    //Variabili che contengono i valori che immette l'utente
    private String name;
    private String address;
    private LatLng location;
    private String phone;
    private String email;
    private String psw1;
    private String psw2;

    //Oggetti Layout
    private TextInputLayout nameL;
    private TextInputLayout addressL;
    private TextInputLayout phoneL;
    private TextInputLayout emailL;
    private TextInputLayout psw1L;
    private TextInputLayout psw2L;

    //Oggetti TextInputText
    private TextInputEditText nameSignUp;
    private TextInputEditText addressSignUp;
    private TextInputEditText phoneSignUp;
    private TextInputEditText emailSignUp;
    private TextInputEditText psw1SignUp;
    private TextInputEditText psw2SignUp;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GymSignUpFragment() {
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
     * @return A new instance of fragment GymSignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GymSignUpFragment newInstance(String param1, String param2) {
        GymSignUpFragment fragment = new GymSignUpFragment();
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
        View view = inflater.inflate(R.layout.fragment_gym_sign_up, container, false);


        profilePic = view.findViewById(R.id.profile_image);
        btnSignUpGym = view.findViewById(R.id.btn_register);
        initializeObjects(view);

        btnSignUpGym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean error = controlfields();
                if (!error) {
                    //SE NON CI SONO ERRORI NEI CAMPI, CREO UN ACCOUNT SU FIREBASE
                    GymCallBack gymCallBack = null;
                    createAuthFirebase();
                }
            }
        });
        //ALL'ONCLICK DEL CIRCLE IMAGE, L'UTENTE PUO' SCEGLIERE CHE IMMAGINE CARICARE
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar.setVisibility(View.VISIBLE);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        addressSignUp.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(requireContext());
                startActivityForResult(intent, MY_ADDRESS_REQUEST_CODE);
            }
        });

        return view;
    }


    private void createAuthFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.createUserWithEmailAndPassword(email, psw1).addOnCompleteListener(myContext, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        psw1L.setError(getString(R.string.error_weak_password));
                        psw1L.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        emailL.setError(getString(R.string.error_invalid_email));
                        emailL.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        emailL.setError(getString(R.string.error_user_exists));
                        emailL.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    String uid = mFirebaseAuth.getUid();
                    writeDb(uid);
                    uploadPic(uid);
                }
            }
        });
    }

    private void uploadPic(String uid) {
        //INSTANZIO GLI OGGETTI DI FIRESTORE
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        final StorageReference ref = storageReference.child("img/gyms/" + uid + "/profilePic");

        //CARICO L'IMMAGINE SU FIRESTORAGE
                    ref.putFile(selectedImage).addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(
                                        //SE L'UPLOAD VA A BUON FINE, MI RECUPERO L'URL DELL'IMMAGINE
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Uri download = uri;
                                            //AGGIORNO I DATI DELL'UTENTE SUL DATABASE AGGIUNGENDO L'URL DELL'IMMAGINE CARICATA
                                            uploadInfoImageUser(uri, uid);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Caricamento immagine fallita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void uploadInfoImageUser(Uri selectedImage, String uid) {
        db = FirebaseFirestore.getInstance();
        String uriString = selectedImage.toString();

        db.collection("gyms").document(uid).update("img", uriString)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        gym.setImage(uriString);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Caricamento immagine fallita", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void writeDb(String uid) {
        String[] keys = getResources().getStringArray(R.array.gym_field);
        String urlImageString = selectedImage.toString();
        Double latitude = locationLatLng.latitude;
        Double longitude = locationLatLng.longitude;
        GeoPoint point = new GeoPoint(latitude, longitude);
        //CREO LA MAPPA CHE CARICO SU FIREBASE
        Map<String, Object> data = new HashMap<>();
        data.put("UserID", uid);
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("img", null);
        data.put("position", point);
        data.put("subscribers", new ArrayList<>());


        //INIZIALIZZO I TURNI
        Map<String, Map<String, Boolean>> turns = initializeTurns(keys);
        //INIZIALIZZO LE SUBSCRIPTION
        Map<String, Boolean> subscriptions = initializeSubs(keys);

        data.put("turns", turns);
        data.put("subscriptions", subscriptions);


        gym = new Gym(uid, email, phone, name, address, null, new LatLng(latitude, longitude), null);


        db = FirebaseFirestore.getInstance();
        db.collection("gyms").document(uid).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //L'UTENTE VIENE REGISTRATO CON SUCCESSO, PROCEDO AD AGGIORNARE I TURNI E SUBS
                        db.collection("gyms").document(uid).update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("gyms").document(uid).set(data)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getActivity(), "PO",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private Map<String, Boolean> initializeSubs(String[] keys) {
        Map<String, Boolean> subscriptions = new HashMap<String, Boolean>() {
            {
                put(keys[10], true);
                put(keys[11], true);
                put(keys[12], true);
                put(keys[13], true);
            }
        };
        return subscriptions;
    }

    private Map<String, Map<String, Boolean>> initializeTurns(String[] keys) {
        String[] keysMorning = getResources().getStringArray(R.array.morning_session_name);
        String[] keysAfternoon = getResources().getStringArray(R.array.afternoon_session_name);
        String[] keysEvening = getResources().getStringArray(R.array.evening_session_name);
        Map<String, Map<String, Boolean>> turns = new HashMap<String, Map<String, Boolean>>() {
            {
                put(keys[14], new HashMap<String, Boolean>() {
                    {
                        put(keysMorning[0], true);
                        put(keysMorning[1], true);
                        put(keysMorning[2], true);
                    }
                });
                put(keys[15], new HashMap<String, Boolean>() {
                    {
                        put(keysAfternoon[0], true);
                        put(keysAfternoon[1], true);
                        put(keysAfternoon[2], true);
                    }
                });
                put(keys[16], new HashMap<String, Boolean>() {
                    {
                        put(keysEvening[0], true);
                        put(keysEvening[1], true);
                        put(keysEvening[2], true);
                    }
                });
            }
        };

        return turns;
    }

    private void initializeObjects(View v) {
        //Inizializzo i layout degli ogetti
        nameL = v.findViewById(R.id.txtNameSignUpGymLayout);
        addressL = v.findViewById(R.id.txtLocationSignUpGymLayout);
        phoneL = v.findViewById(R.id.txtPhoneSignUpGymLayout);
        emailL = v.findViewById(R.id.txtEmailSignUpGymLayout);
        psw1L = v.findViewById(R.id.txtPasswordSignUpGymLayout);
        psw2L = v.findViewById(R.id.txtRepeatPasswordSignUpGymLayout);

        //Inizializzo le edit text
        nameSignUp = v.findViewById(R.id.txtNameSignUpGym);
        addressSignUp = v.findViewById(R.id.txtLocationSignUpGym);
        phoneSignUp = v.findViewById(R.id.txtPhoneSignUpGym);
        emailSignUp = v.findViewById(R.id.txtEmailSignUpGym);
        psw1SignUp = v.findViewById(R.id.txtPasswordSignUpGym);
        psw2SignUp = v.findViewById(R.id.txtRepeatPasswordSignUpGym);
    }

    private boolean controlfields() {
        boolean error = false;
        name = nameSignUp.getText().toString();
        address = addressSignUp.getText().toString();
        phone = phoneSignUp.getText().toString();
        email = emailSignUp.getText().toString();
        psw1 = psw1SignUp.getText().toString();
        psw2 = psw2SignUp.getText().toString();

        if (name.isEmpty()) {
            nameL.setError("Nome non valido");
            nameL.requestFocus();
            error = true;
        } else if (address.isEmpty()) {
            addressL.setError("Indirizzo non valido");
            addressL.requestFocus();
            error = true;
        } else if (phone.isEmpty()) {
            phoneL.setError("Numero non valido");
            phoneL.requestFocus();
            error = true;
        } else if (email.isEmpty()) {
            emailL.setError("Email non valida");
            emailL.requestFocus();
            error = true;
        } else if (psw1.isEmpty()) {
            psw1L.setError("Password non valida");
            psw1L.requestFocus();
            error = true;
        } else if (psw2.isEmpty()) {
            psw2L.setError("Password non valida");
            psw2L.requestFocus();
            error = true;
        } else if ((!psw1.isEmpty()) && (!psw2.isEmpty())) {
            if (!(psw1.equals(psw2))) {
                psw1L.setError("Le email non sono uguali");
            }
        }
        return error;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //storage = FirebaseStorage.getInstance();
        //storageReference = storage.getReference();

        //CODICE PER ACQUISIRE INFO DALL'INDIRIZZO
        if (requestCode == MY_ADDRESS_REQUEST_CODE && !(data == null)) {
            if (resultCode == ActivityGymProfile.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addressSignUp.setText(place.getAddress());
                locationLatLng = place.getLatLng();
            }
        }

        //CODICE PER ACQUISIRE IMMAGINE DA GALLERIA
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    selectedImage = data.getData();
                    profilePic.setImageURI(selectedImage);

                }
                break;
        }
    }
}