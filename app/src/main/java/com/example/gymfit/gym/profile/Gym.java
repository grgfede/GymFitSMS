package com.example.gymfit.gym.profile;

import com.example.gymfit.system.GenericUser;
import com.google.android.gms.maps.model.LatLng;

public class Gym extends GenericUser {
    private String address;
    private String name;
    private String image;
    private LatLng position;

    public Gym(String uid, String email, String phone, String name, String address, LatLng position, String image) {
        super(uid, email, phone);
        this.name = name;
        this.address = address;
        this.position = position;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
