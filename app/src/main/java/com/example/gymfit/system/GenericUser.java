package com.example.gymfit.system;

public abstract class GenericUser {
    private final String uid;
    private final String email;
    private final int number;

    public GenericUser(String uid, String email, int number) {
        this.uid = uid;
        this.email = email;
        this.number = number;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public int getNumber() {
        return number;
    }
}
