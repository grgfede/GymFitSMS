package com.example.gymfit.user;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String name;
    private String surname;
    private String phone;
    private String email;
    private String gender;
    private String uid;
    private String urlImage;
    private FirebaseFirestore db;


    //COSTRUTTORE DI DEFAULT
    public User() {
    }

    public User (String name, String surname, String phone, String email, String gender, String uid){

        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.uid = uid;
    }

    public User (String name, String surname, String phone, String email, String uid){

        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.uid = uid;
    }

    //METODI GET
    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }



    public String getUid(){
        return uid;
    }

    public String getUrlImage(){ return urlImage;}

    //METODI SET

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setUid(String uid){
        this.uid = uid;
    }

    public void setUrlImage(String urlImage){ this.urlImage = urlImage;}




}
