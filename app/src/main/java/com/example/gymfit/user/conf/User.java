package com.example.gymfit.user.conf;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

// TODO: extends and replace methods/attrs with GenericUser!
public class User implements Serializable, Parcelable {
    private final String username; // Just for search filter
    private String name;
    private String surname;
    private String phone;
    private LatLng location;
    private String address;
    private String email;
    private String uid;
    private String urlImage;
    private String subscription;
    private Date dateOfBirthday; // TODO: change this field with Date or Calendar
    private List<Map<String, Object>> turns;
    private String gender;



    public User(String name, String surname, String gender, Date dateOfBirthday, LatLng location, String address, String phone){
        this.username = name + " " + surname;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.location = location;
        this.address = address;
        this.dateOfBirthday = dateOfBirthday;
        this.phone = phone;
    }

    public User (String name, String surname, String phone, String email, String gender, String uid){
        this.username = name + " " + surname;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.uid = uid;
    }

    public User(String name, String surname, String phone, String email, String gender, String uid, String img, String subscription, List<Map<String, Object>> turns) {
        this(name, surname, phone, email, gender, uid);
        this.urlImage = img;
        this.subscription = subscription;
        this.turns = turns;
    }


    // Get methods

    public String getUsername() {
        return username;
    }

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

    public String getGender() {
        return gender;
    }

    public Date getDateOfBirthday() {
        return dateOfBirthday;
    }

    public String getUid(){
        return uid;
    }

    public String getUrlImage(){ return urlImage;}

    public LatLng getLocation(){return location;}

    public String getAddress(){return address;}

    public String getSubscription() {
        return subscription;
    }

    public List<Map<String, Object>> getTurns() {
        return turns;
    }

    // Set methods

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

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLocation(LatLng location){this.location = location;}

    public void setDateOfBirthday(Date dateOfBirthday) {
        this.dateOfBirthday = dateOfBirthday;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public void setUrlImage(String urlImage){ this.urlImage = urlImage;}

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public void setTurns(List<Map<String, Object>> turns) {
        this.turns = turns;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}

