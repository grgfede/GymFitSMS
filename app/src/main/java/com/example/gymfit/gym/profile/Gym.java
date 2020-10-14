package com.example.gymfit.gym.profile;

import com.example.gymfit.system.GenericUser;

import java.util.HashMap;
import java.util.Map;

public class Gym extends GenericUser {
    private Map<String, String> address;
    private String name;

    public Gym(String uid, String email, String phone, String name, HashMap<String, String> address ) {
        super(uid, email, phone);
        this.name = name;
        this.address = address;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

}
