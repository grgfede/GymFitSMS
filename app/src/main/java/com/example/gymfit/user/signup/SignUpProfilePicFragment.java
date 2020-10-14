
package com.example.gymfit.user.signup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.gymfit.R;
import com.example.gymfit.system.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.Cursor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpProfilePicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpProfilePicFragment extends Fragment {

    private String uid;
    private static final int SELECT_PHOTO = 1;
    FirebaseStorage storage;
    StorageReference storageReference;
    private CircleImageView profilePic;
    private FirebaseFirestore db;

    private FragmentActivity myContext;
    Button btnSkip;

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
        View view = inflater.inflate(R.layout.fragment_sign_up_profile_pic, container, false);

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
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });


        //CREO UN LISTENER SULL'ONCLICK DEL PULSANTE PER SKIPPARE IL CARICAMENTO DELLA FOTO
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDefaultProfilePic(uid);
            }
        });
        return view;


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
                        Fragment finalSignUp = new SignUpFinish();
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


    private void recoverDataFragment2() {
        uid = getArguments().getString("uid");
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