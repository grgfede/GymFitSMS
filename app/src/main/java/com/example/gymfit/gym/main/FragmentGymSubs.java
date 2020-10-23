package com.example.gymfit.gym.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.UserConfCallback;
import com.example.gymfit.system.conf.recycleview.UserAdapter;
import com.example.gymfit.user.conf.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentGymSubs extends Fragment {
    private static final String DESCRIBABLE_KEY = "describable_key";
    private static final String INFO_LOG = "INFO: ";
    private static final String ERROR_LOG = "ERROR: ";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private boolean adapterSet = false;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Gym gym = null;
    private List<User> users = new ArrayList<>();
    private View activityView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        this.gym = (Gym) getArguments().getSerializable(DESCRIBABLE_KEY);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_subs, container, false);
        this.activityView = rootView.findViewById(R.id.constraintLayout);

        // Change toolbar
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_subs_toolbar_title));

        setSubs(subID -> {
            this.db.collection("users").document(subID).get().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;

                    String uid = documentSnapshot.getString("uid");
                    String name = documentSnapshot.getString("name");
                    String surname = documentSnapshot.getString("surname");
                    String phone = documentSnapshot.getString("phone");
                    String img = documentSnapshot.getString("img");
                    String email = documentSnapshot.getString("email");
                    String subscription = documentSnapshot.getString("subscription");
                    Boolean gender = documentSnapshot.getBoolean("gender");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> turns = (List<Map<String, Object>>) documentSnapshot.get("turns");

                    // TODO: wait for change to User with wrapper class Boolean
                    //this.users.add(new User(name, surname, phone, email, gender.booleanValue(), uid, img, subscription, turns));

                } else {
                    Log.d(ERROR_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
                }

                if (task.isComplete()) {
                    adapterSet = true;
                    setUpRecycleView();
                }
            });
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gym_subs_toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapterSet) {
                    userAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public static FragmentGymSubs newInstance(Gym gym) {
        FragmentGymSubs fragment = new FragmentGymSubs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void setUpRecycleView() {
        this.recyclerView = requireView().getRootView().findViewById(R.id.rv_users);
        this.recyclerView.setHasFixedSize(true);

        this.userAdapter = new UserAdapter(requireActivity(), this.users);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        this.recyclerView.setAdapter(userAdapter);
    }

    private void setSubs(UserConfCallback callback) {

        this.db.collection("gyms").document(this.gym.getUid()).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;

                String[] subscribersUID = stringToArray(Objects.requireNonNull(documentSnapshot.get("subscribers")).toString());

                for (String entry : subscribersUID) {
                    callback.onCallback(entry);
                }

            } else {
                Log.d(ERROR_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });

    }

    private String[] stringToArray(String str) {
        str = str.substring(1, str.length()-1);
        str = StringUtils.deleteWhitespace(str);
        return str.split(",");
    }

}