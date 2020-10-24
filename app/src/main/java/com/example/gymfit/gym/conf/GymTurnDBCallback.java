package com.example.gymfit.gym.conf;

public interface GymTurnDBCallback {
    void onCallback(Gym gym, String key, int which, boolean isChecked);
}
