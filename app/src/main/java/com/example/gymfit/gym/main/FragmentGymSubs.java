package com.example.gymfit.gym.main;

import android.annotation.SuppressLint;
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
    private static final String INFO_LOG = "info";
    private static final String ERROR_LOG = "error";

    private UserAdapter userAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View activityView = null;
    private Gym gym = null;
    private final List<User> users = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        this.gym = (Gym) getArguments().getSerializable(DESCRIBABLE_KEY);

        // Change toolbar
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_subs, container, false);

        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_subs_toolbar_title));

        // View initialization
        setMessageAnchor(rootView);
        setSubs(subID -> this.db.collection("users").document(subID).get().addOnCompleteListener(task -> {

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
                String gender = documentSnapshot.getString("gender");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> turns = (List<Map<String, Object>>) documentSnapshot.get("turns");

                this.users.add(new User(name, surname, phone, email, gender, uid, img, subscription, turns));

            } else {
                Log.d(ERROR_LOG, "ERROR: " + Objects.requireNonNull(task.getException()).getMessage());
            }

            if (task.isComplete()) {
                setUpRecycleView();
            }
        }));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gym_subs_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.app_bar_search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        userAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
                break;
            case R.id.app_bar_filter:
                userAdapter.getSort().filter(getResources().getString(R.string.prompt_default));
                break;
            case R.id.action_filter_monthly:
                userAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_monthly));
                break;
            case R.id.action_filter_quarterly:
                userAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_quarterly));
                break;
            case R.id.action_filter_six_month:
                userAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_six_month));
                break;
            case R.id.action_filter_annual:
                userAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_annual));
                break;
            case R.id.action_sort_by_name:
                userAdapter.getSort().filter(getResources().getString(R.string.prompt_name));
                break;
            case R.id.action_sort_by_surname:
                userAdapter.getSort().filter(getResources().getString(R.string.prompt_surname));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static FragmentGymSubs newInstance(Gym gym) {
        FragmentGymSubs fragment = new FragmentGymSubs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void setUpRecycleView() {
        RecyclerView recyclerView = requireView().getRootView().findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);

        this.userAdapter = new UserAdapter(requireActivity(), this.users);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(userAdapter);
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

    /**
     * Set the container anchor for Snackbar object and its methods "make"
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setMessageAnchor(View rootView) {
        // Initialize the container that will be used for Snackbar methods
        this.activityView = rootView.findViewById(R.id.constraintLayout);
    }

}