package com.example.gymfit.user;

// TODO: extends and replace methods/attrs with GenericUser!
public class User {

    // TODO: merge name + surname with username
    private String name;
    private String surname;
    private String phone;
    private String email;
    private String uid;
    private String urlImage;
    private String subscription;
    private String[] turns;
    private boolean gender; // 0 for Man and 1 form Female

    public User (String name, String surname, String phone, String email, String uid){
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.uid = uid;
    }

    public User (String name, String surname, String phone, String email, boolean gender, String uid){
        this(name, surname, phone, email, uid);
        this.gender = gender;
    }

    public User (String name, String surname, String phone, String email, boolean gender, String uid, String subscription, String[] turns) {
        this(name, surname, phone, email, gender, uid);
        this.subscription = subscription;
        this.turns = turns;
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

    public boolean isGender() {
        return gender;
    }

    public String getUid(){
        return uid;
    }

    public String getUrlImage(){ return urlImage;}

    public String getSubscription() {
        return subscription;
    }

    public String[] getTurns() {
        return turns;
    }

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

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public void setUrlImage(String urlImage){ this.urlImage = urlImage;}

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public void setTurns(String[] turns) {
        this.turns = turns;
    }

}
