package com.example.gymfit.gym.conf;

import com.example.gymfit.user.conf.User;

import java.util.List;

public interface SubscriberCallback {
    void addOnCallback(List<String> usersID);
    void addOnCallback(boolean isEmpty);
    void addOnSuccessCallback(User user);
    void addOnSuccessListener();
}
