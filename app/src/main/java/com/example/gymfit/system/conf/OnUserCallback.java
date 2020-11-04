package com.example.gymfit.system.conf;

import com.example.gymfit.user.conf.User;

import java.util.List;

public interface OnUserCallback {
    void addOnCallback(List<String> usersID);
    void addOnCallback(boolean isEmpty);
    <T extends GenericUser> void addOnSuccessCallback(T user);
    void addOnSuccessListener();
}
