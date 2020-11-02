package com.example.gymfit.system.conf.exception;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NullDataException extends NullPointerException {
    private final static String message = "there is an empty data";
    private List<String> emptyData = new ArrayList<>();

    public NullDataException(List<String> emptyData) {
        this.emptyData = emptyData;
    }

    public NullDataException(String s) {
        super(s);
    }

    public List<String> getEmptyData() {
        return this.emptyData;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }
}
