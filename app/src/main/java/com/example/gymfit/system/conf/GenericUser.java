package com.example.gymfit.system.conf;

import androidx.annotation.NonNull;

import java.io.Serializable;

public abstract class GenericUser implements Serializable {
    private String uid;
    private String email;
    private String phone;

    public GenericUser(@NonNull final String uid, @NonNull final String email, @NonNull final String phone) {
        this.uid = uid;
        this.email = email;
        this.phone = phone;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPhone() {
        return phone;
    }

    public void setUid(@NonNull final String uid) {
        this.uid = uid;
    }

    public void setEmail(@NonNull final String email) {
        this.email = email;
    }

    public void setPhone(@NonNull final String phone) {
        this.phone = phone;
    }

}
