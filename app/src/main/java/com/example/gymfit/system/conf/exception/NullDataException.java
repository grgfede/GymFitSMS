package com.example.gymfit.system.conf.exception;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NullDataException extends NullPointerException {
    private final static String message = "there is an empty data";

    public NullDataException() {
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }
}
