package com.example.gymfit.user.conf;

import java.util.List;
import java.util.Map;

// TODO: extends and replace methods/attrs with GenericUser!
public class User {
    private final String username; // Just for search filter
    private String name;
    private String surname;
    private String phone;
    private String email;
    private String uid;
    private String urlImage;
    private String subscription;
    private String dateOfBirthday; // TODO: change this field with Date or Calendar
    private List<Map<String, Object>> turns;
    private boolean gender; // 0 for Man and 1 form Female

    public User (String name, String surname, String phone, String email, String uid){
        this.username = name + " " + surname;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.uid = uid;
    }

    public User (String name, String surname, String phone, String dateOfBirthday, String email, String uid) {
        this(name, surname, phone, email, uid);
        this.dateOfBirthday = dateOfBirthday;
    }

    public User (String name, String surname, String phone, String email, boolean gender, String uid){
        this(name, surname, phone, email, uid);
        this.gender = gender;
    }

    public User(String name, String surname, String phone, String email, boolean gender, String uid, String img, String subscription, List<Map<String, Object>> turns) {
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

    public boolean getGender() {
        return gender;
    }

    public String getDateOfBirthday() {
        return dateOfBirthday;
    }

    public String getUid(){
        return uid;
    }

    public String getUrlImage(){ return urlImage;}

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

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setDateOfBirthday(String dateOfBirthday) {
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

}

