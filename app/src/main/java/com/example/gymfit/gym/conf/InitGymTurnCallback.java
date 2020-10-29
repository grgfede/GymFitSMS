package com.example.gymfit.gym.conf;

public interface InitGymTurnCallback {
    void onCallback(Gym gym, String key, int which, boolean isChecked);
}
