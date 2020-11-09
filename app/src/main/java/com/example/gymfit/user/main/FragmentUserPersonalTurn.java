package com.example.gymfit.user.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.User;
import com.google.android.material.navigation.NavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserPersonalTurn#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserPersonalTurn extends Fragment {
    private static final String USER_KEY = "user_key";

    private View messageAnchor = null;

    private User user = null;

    public static FragmentUserPersonalTurn newInstance(@NonNull User user) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserPersonalTurn created");

        FragmentUserPersonalTurn fragment = new FragmentUserPersonalTurn();
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_KEY, user);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_personal_turn, container, false);

        initSystemInterface(rootView);

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentUserPersonalTurn layout XML created");

        return rootView;
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull View rootView) {
        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentUserPersonalTurn initialized");
    }
}