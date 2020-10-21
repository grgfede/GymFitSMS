package com.example.gymfit.gym.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.UserConfCallback;
import com.example.gymfit.user.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentGymSubs extends Fragment {
    private static final String DESCRIBABLE_KEY = "describable_key";
    private static final String INFO_LOG = "INFO: ";
    private static final String ERROR_LOG = "INFO: ";

    private RecyclerView recyclerView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Gym gym = null;
    private ArrayList<User> users = null;
    private View activityView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        this.gym = (Gym) getArguments().getSerializable(DESCRIBABLE_KEY);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_subs, container, false);

        // Recycle View
        this.recyclerView = rootView.findViewById(R.id.rv_users);
        this.activityView = rootView.findViewById(R.id.constraintLayout);

        setSubs(userID -> {
            this.db.collection("users").document(userID).get().addOnCompleteListener(task -> {

                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;


                } else {
                    Log.d(ERROR_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        });

        return rootView;
    }

    public static FragmentGymSubs newInstance(Gym gym) {
        FragmentGymSubs fragment = new FragmentGymSubs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void setSubs(UserConfCallback callback) {

        this.db.collection("gyms").document(this.gym.getUid()).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;

                String[] subscribersUID = (String[]) documentSnapshot.get("subscribers");
                assert subscribersUID != null;
                for (String entry : subscribersUID) {
                    callback.onCallback(entry);
                }

            } else {
                Log.d(ERROR_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });

                /*
        UserAdapter userAdapter = new UserAdapter(requireActivity(), this.xxx, this.xxx);
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));*/

    }

}