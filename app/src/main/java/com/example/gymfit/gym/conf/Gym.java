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
    private final Double[] positionArray;
    private final Map<String, Boolean> subscription;
    private final Map<String, Boolean[]> turns;

    public Gym(String uid, String email, String phone, String name, String address, LatLng position, String image) {
        super(uid, email, phone);
        this.name = name;
        this.address = address;
        this.positionArray = new Double[]{position.latitude, position.longitude};
        this.image = image;
        this.subscription = new HashMap<String, Boolean>() {
            {
                put("monthly", true);
                put("quarterly", true);
                put("sixMonth", true);
                put("annual", true);
            }
        };
        this.turns = new HashMap<String, Boolean[]>() {
            {
                put("morning", new Boolean[] {true, true, true});
                put("afternoon", new Boolean[] {true, true, true});
                put("evening", new Boolean[] {true, true, true});
            }
        };
    }

    // Get methods

    public Map<String, Boolean[]> getTurns() {
        return turns;
    }

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
        return new LatLng(this.positionArray[0], this.positionArray[1]);
    }

    public String getName() {
        return name;
    }

    // Set methods

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.positionArray[0] = position.latitude;
        this.positionArray[1] = position.longitude;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSubscription(String key, Boolean value) {
        this.subscription.replace(key, value);
    }

    public void setTurn(String key, int position, boolean value) {
        this.turns.get(key)[position] = value;
    }

}
