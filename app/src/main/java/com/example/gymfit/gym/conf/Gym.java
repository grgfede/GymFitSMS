package com.example.gymfit.gym.conf;

import com.example.gymfit.system.conf.GenericUser;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Gym extends GenericUser implements Serializable {
    private String address;
    private String name;
    private String image;
    private LatLng position;

    private final Map<String, Boolean> subscription;

    public Gym(String uid, String email, String phone, String name, String address, LatLng position, String image) {
        super(uid, email, phone);
        this.name = name;
        this.address = address;
        this.position = position;
        this.image = image;
        this.subscription = new HashMap<String, Boolean>() {
            {
                put("monthly", true);
                put("quarterly", true);
                put("sixMonth", true);
                put("annual", true);
            }
        };
    }

    // GET METHODS
    public Map<String, Boolean> getSubscription() {
        return subscription;
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

    // SET METHODS
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

    public void setSubscription(String key, Boolean value) {
        this.subscription.remove(key);
        this.subscription.put(key, value);
    }

}
