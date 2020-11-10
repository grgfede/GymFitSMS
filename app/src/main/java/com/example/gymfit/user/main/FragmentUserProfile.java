package com.example.gymfit.user.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gymfit.R;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserProfile extends Fragment {
    private static final String USER_KEY = "user_key";
    private static final String IS_EMPTY_KEY = "is_empty_key";
    private static final String EMPTY_DATA_KEY = "empty_data_key";

    // Screen orientation
    private int orientation;
    private View messageAnchor = null;
    private Menu toolbar = null;

    private String userUID = null;
    private User user = null;
    private boolean isEmptyData = false;
    private List<String> emptyData = new ArrayList<>();

    public static FragmentUserProfile newInstance(@NonNull final User user, final boolean isEmptyData, @NonNull final ArrayList<String> emptyData) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserProfile created");

        FragmentUserProfile fragment = new FragmentUserProfile();
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_KEY, user);
        bundle.putBoolean(IS_EMPTY_KEY, isEmptyData);
        bundle.putStringArrayList(EMPTY_DATA_KEY, emptyData);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@NotNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
            this.isEmptyData = getArguments().getBoolean(IS_EMPTY_KEY);
            this.emptyData = getArguments().getStringArrayList(EMPTY_DATA_KEY);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        initSystemInterface(rootView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_profile_toolbar, menu);
        this.toolbar = menu;
        super.onCreateOptionsMenu(menu, inflater);

        AppUtils.log(Thread.currentThread().getStackTrace(), "Toolbar User is inflated");
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull final View rootView) {
        // init new checked item on navigation Drawer
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_user);
        navigationView.getMenu().findItem(R.id.nav_menu_home).setChecked(true);

        // Abilities toolbar item options
        setHasOptionsMenu(true);

        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.user_profile_toolbar_title));

        // init origin screen orientation
        this.orientation = rootView.getResources().getConfiguration().orientation;

        // init gym ID from Gym Object
        this.userUID = this.user.getUid();

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentUserProfile initialized");
    }

}