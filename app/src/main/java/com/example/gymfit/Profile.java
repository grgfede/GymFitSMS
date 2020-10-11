package com.example.gymfit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymfit.R;
import com.example.gymfit.user.EditProfile;
import com.example.gymfit.user.signin.Login;
import com.example.gymfit.user.signup.SignUp;
import com.example.gymfit.gym.profile.GymProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Button;

import java.util.Objects;

public class Profile extends AppCompatActivity {

private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().hide();

        Button button = (Button)findViewById(R.id.Editprofile);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(Profile.this, EditProfile.class));
                    }
                }
        );

        final ImageButton profile = (ImageButton) findViewById(R.id.profilepic);

        profile.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //startActivity(new Intent(Profile.this, EditProfile.class));

                        PopupMenu popup = new PopupMenu(Profile.this, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.popuppic, popup.getMenu());
                        popup.show();


                    }
                }
        );


    }

    public void logoutIntent (View v) {
        mAuth.signOut();
        startActivity(new Intent(Profile.this, Login.class));

        Toast.makeText(Profile.this, "Torna a trovarci, a presto!", Toast.LENGTH_SHORT).show();
    }

}

