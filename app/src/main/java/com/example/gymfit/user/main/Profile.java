package com.example.gymfit.user.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;


import com.example.gymfit.R;
import com.example.gymfit.user.main.signin.Login;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final static int SELECT_PHOTO = 1;

    CircleImageView profilePic;
    ImageView profileZoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        profilePic = (CircleImageView) findViewById(R.id.profile_image);

        Button edit = (Button) findViewById(R.id.Editprofile);
        edit.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(Profile.this, EditProfile.class));
                    }
                }
        );

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popup = new PopupMenu(Profile.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popuppic, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.one) {
                            // inflate the layout of the popup window
                            LayoutInflater inflater;
                            inflater = (LayoutInflater)
                                    getSystemService(LAYOUT_INFLATER_SERVICE);
                            View popupView = inflater.inflate(R.layout.popup_window, null);

                            // create the popup window
                            int width = LinearLayout.LayoutParams.MATCH_PARENT;
                            int height = LinearLayout.LayoutParams.MATCH_PARENT;
                            boolean focusable = true; // lets taps outside the popup also dismiss it
                            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                            // show the popup window
                            // which view you pass in doesn't matter, it is only used for the window tolken
                            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                            profileZoom = (ImageView) popupView.findViewById(R.id.imageZoom);
                            profileZoom.setImageDrawable(profilePic.getDrawable());
                            // dismiss the popup window when touched
                            popupView.setOnTouchListener(new View.OnTouchListener() {

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    popupWindow.dismiss();
                                    return true;
                                }
                            });


                        } else if (id == R.id.two) {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                        }
                        return false;
                    }
                });
            }
        });



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

                }
                break;
        }
    }

    public void logoutIntent(View v) {
        mAuth.signOut();
        startActivity(new Intent(Profile.this, Login.class));

        Toast.makeText(Profile.this, "Torna a trovarci, a presto!", Toast.LENGTH_SHORT).show();
    }


}
