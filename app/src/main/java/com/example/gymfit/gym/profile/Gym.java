package com.example.gymfit.gym.profile;

import com.example.gymfit.system.GenericUser;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class Gym extends GenericUser {
    private Map<String, String> address;
    private String name;

    private LatLng position;

    public Gym(String uid, String email, String phone, String name, HashMap<String, String> address, LatLng position) {
        super(uid, email, phone);
        this.name = name;
        this.address = address;
        this.position = position;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getAddressToString() {
        return  this.address.get("street") + ", " +
                this.address.get("numberStreet") + ", " +
                this.address.get("zipCode") + " " + this.address.get("city") + ", " +
                this.address.get("country");
    }

    public String getName() {
        return name;
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

}
