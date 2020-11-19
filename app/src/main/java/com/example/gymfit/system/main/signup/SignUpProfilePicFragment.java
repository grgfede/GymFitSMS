
package com.example.gymfit.system.main.signup;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gymfit.R;
import com.example.gymfit.user.conf.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission_group.CAMERA;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.motion.widget.Debug.getLocation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpProfilePicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpProfilePicFragment extends Fragment {

    private static final int SELECT_PHOTO = 1;
    private static final int REQUEST_WRITE_PERMISSION = 786;

    FirebaseStorage storage;
    StorageReference storageReference;
    private CircleImageView profilePic;
    private FirebaseFirestore db;

    ProgressBar progressBar;
    private FragmentActivity myContext;
    Button btnSkip;

    public User user = null;

    public String name;
    public String surname;
    public String phone;
    public String gender;
    public Date dateOfBirth;
    public LatLng location;
    public String address;
    public String email;
    private String uid;
    public String urlImg;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpProfilePicFragment() {
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
     * @return A new instance of fragment SignUpProfilePicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpProfilePicFragment newInstance(String param1, String param2) {
        SignUpProfilePicFragment fragment = new SignUpProfilePicFragment();
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
        View view = inflater.inflate(R.layout.fragment_system_signupprofilepic_user, container, false);


        //CODICE CHE EVITA IL RITORNO INDIETRO DEL FRAGMENT
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        return true;
                    }
                }
                return false;
            }
        });


        progressBar = view.findViewById(R.id.progressBar2);
        //INSTANZIO GLI OGGETTI DI FIRESTORE
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //RECUPERO L'UID
        recoverDataFragment2();


        btnSkip = view.findViewById(R.id.btnSkip);

        profilePic = view.findViewById(R.id.profile_image);
        //ALL'ONCLICK DEL CIRCLE IMAGE, L'UTENTE PUO' SCEGLIERE CHE IMMAGINE CARICARE
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            }
        });



        //CREO UN LISTENER SULL'ONCLICK DEL PULSANTE PER SKIPPARE IL CARICAMENTO DELLA FOTO
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploadDefaultProfilePic(uid);
            }
        });
        return view;


    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            progressBar.setVisibility(View.VISIBLE);
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);        }
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        //storage = FirebaseStorage.getInstance();
        //storageReference = storage.getReference();
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {

                    final Uri selectedImage = imageReturnedIntent.getData();
                    profilePic.setImageURI(selectedImage);
                    //SETTO IL PERCORSO DELLO STORAGE DOVE SALVARE LE IMMAGINI
                    final StorageReference ref = storageReference.child("img/users/" + uid + "/profilePic");

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
                                            uploadInfoImageUser(uri);
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
                break;
        }
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

    private void uploadInfoImageUser(Uri selectedImage) {
        db = FirebaseFirestore.getInstance();
        String uriString = selectedImage.toString();

        db.collection("users").document(uid).update("img", uriString)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Fragment finalSignUp = new SignUpFinish();
                        FragmentManager fragManager = myContext.getSupportFragmentManager();
                        changeFragment(fragManager, finalSignUp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    private void recoverDataFragment2() {
        Bundle bundle = getArguments();
        user = (User) bundle.getSerializable("user");
        name = user.getName();
        surname = user.getSurname();
        phone = user.getPhone();
        gender = user.getGender();
        dateOfBirth = user.getDateOfBirthday();
        location = user.getLocation();
        address = user.getAddress();
        uid = user.getUid();
    }


    /*
     * METODO CHE CARICA SUL FIRESTORE L'IMMAGINE DI DEFAULT NEL CASO IN CUI L'UTENTE DECIDESSE
     * DI SKIPPARE L'UPLOAD DELLA FOTO DEL PROFILO
     */
    private void uploadDefaultProfilePic(String uid) {

        //MI CREO UN BITMAP CONTENENTE L'IMMAGINE DI DEFAULT AVATAR NELLA CARTELLA DRAWABLE
        Bitmap imageBitMap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.default_user);
        //CREO UN ARRAY DI BYTE CHE RAPPRESENTA L'IMMAGINE IN BITMAP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //POPOLO L'ARRAY DI BYTE
        byte[] data = baos.toByteArray();
        final StorageReference ref = storageReference.child("img/users/" + uid + "/profilePic");
        //CARICO L'ARRAY DI BYTE
        ref.putBytes(data).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uploadInfoImageUser(uri);
                            }
                        });
                    }
                }
        );

    }

}