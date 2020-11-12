package com.example.gymfit.gym.conf;

import androidx.annotation.NonNull;

public interface OnTurnDialogCallback {
    void onCallback(@NonNull final String category, @NonNull final String key, final boolean isChecked);
}
