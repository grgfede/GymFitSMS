package com.example.gymfit.system;

public abstract class GenericUser {
    private String uid;
    private String email;
    private String phone;

    public GenericUser(String uid, String email, String phone) {
        this.uid = uid;
        this.email = email;
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
