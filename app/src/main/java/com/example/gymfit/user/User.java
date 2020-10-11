package com.example.gymfit.user;

public class User {

    private String name;
    private String surname;
    private String phone;
    private String email;
    private String uid;
    /*
     * PER IL GENDER VIENE UTILIZATO UN FLAG BOOLEAN
     * Uomo = 0
     * Donna = 1
     * (Nessun membro del team è maschilista e/o femminista)
     */
    private boolean gender;
    //COSTRUTTORE DI DEFAULT
    public User() {
    }

    public User (String name, String surname, String phone, String email, boolean gender, String uid){

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

    public boolean isGender() {
        return gender;
    }

    public String getUid(){
        return uid;
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




}
