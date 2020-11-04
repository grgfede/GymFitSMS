package com.example.gymfit.user.main;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.OnUserCallback;
import com.example.gymfit.system.conf.recycleview.ListGymAdapter;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.example.gymfit.user.conf.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserListGyms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserListGyms extends Fragment {
    private static final String USER_KEY = "user_key";

    private ListGymAdapter gymAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String[] collections = ResourceUtils.getStringArrayFromID(R.array.collections);

    private View messageAnchor = null;
    private boolean isVisible;
    private boolean isEmptyData = false;

    private User user = null;
    private final List<Gym> gyms = new ArrayList<>();

    public static FragmentUserListGyms newInstance(User user) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentUserListGyms created");

        FragmentUserListGyms fragment = new FragmentUserListGyms();
        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_KEY, user);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@NotNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_list_gyms, container, false);

        initSystemInterface(rootView);
        initInterface();

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentUserListGyms layout XML created");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_list_gyms_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // If there are some subscribers enable actions of new menu, otherwise keep them disabled (no effects on recycle)
        if (!this.isEmptyData) {
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
                            //subscriberAdapter.getFilter().filter(newText);
                            return false;
                        }
                    });
                    break;
                case R.id.app_bar_filter:
                    //subscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_default));
                    break;
                case R.id.action_filter_10:
                    //subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_monthly));
                    break;
                case R.id.action_filter_20:
                    //subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_quarterly));
                    break;
                case R.id.action_filter_50:
                    //subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_six_month));
                    break;
                case R.id.action_sort_by_name:
                    //subscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_name));
                    break;
                default:
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(View rootView) {
        // init new checked item on navigation Drawer
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_user);
        navigationView.getMenu().findItem(R.id.nav_menu_gyms).setChecked(true);

        // Abilities toolbar item options
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.user_gyms_toolbar_title));

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentUserListGyms initialized");
    }

    /**
     * Try to find any subscribers to init Recycle or show a alert message if they aren't for this gym
     *
     */
    private void initInterface() {
        this.isEmptyData = false;

        getGymsUIDFromDatabase(new OnUserCallback() {
            int gymsCount = 0;
            int gymsSize = 0;

            @Override
            public void addOnCallback(List<String> usersID) {
                gymsSize = usersID.size();

                usersID.forEach(userID -> db.collection(collections[0]).document(userID).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                String[] keys = getResources().getStringArray(R.array.gym_field);

                                String uid = documentSnapshot.getString(keys[0]).isEmpty() ? documentSnapshot.getString(keys[0]) : "null";
                                String email = documentSnapshot.getString(keys[2]).isEmpty() ? documentSnapshot.getString(keys[2]) : "null";
                                String phone = documentSnapshot.getString(keys[4]).isEmpty() ? documentSnapshot.getString(keys[4]) : "null";
                                String name = documentSnapshot.getString(keys[1]).isEmpty() ? documentSnapshot.getString(keys[1]) : "null";
                                String address = documentSnapshot.getString(keys[5]).isEmpty() ? documentSnapshot.getString(keys[5]) : "null";
                                String image = documentSnapshot.getString(keys[3]).isEmpty() ? documentSnapshot.getString(keys[3]) : "null";

                                LatLng position = documentSnapshot.getGeoPoint(keys[6]) != null
                                    ? new LatLng(
                                        documentSnapshot.getGeoPoint(keys[6]).getLatitude(),
                                        documentSnapshot.getGeoPoint(keys[6]).getLongitude())
                                    : new LatLng(0, 0);

                                List<String> subscribers = documentSnapshot.get(keys[8]) != null
                                        ? new LinkedList<>(Arrays.asList(stringToArray(documentSnapshot.get(keys[8]).toString())))
                                        : new ArrayList<>();

                                //Log.d("KEY_LOG", documentSnapshot.get(keys[8]).getClass().toString());

                                Gym gym = new Gym(uid, email, phone, name, address, subscribers, position, image);

                                addOnSuccessCallback(gym);
                            } else {
                                // TODO: reaction
                            }
                        })
                        .addOnFailureListener(task -> {
                            // TODO: reaction
                            AppUtils.log(Thread.currentThread().getStackTrace(), Objects.requireNonNull(task.getMessage()));
                        }));
            }

            @Override
            public void addOnCallback(boolean isEmpty) {
                if (isEmpty) {
                    isEmptyData = true;
                    AppUtils.log(Thread.currentThread().getStackTrace(), "No gyms into Database");
                    AppUtils.message(messageAnchor, getString(R.string.recycleview_gyms_void), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public <T extends GenericUser> void addOnSuccessCallback(T user) {
                gyms.add((Gym) user);
                gymsCount++;
                addOnSuccessListener();
            }

            @Override
            public void addOnSuccessListener() {
                if (gymsCount == gymsSize) {
                    setUpRecycleView();
                }
            }
        });
    }

    private void setUpRecycleView() {
        RecyclerView recyclerView = requireView().getRootView().findViewById(R.id.rv_gyms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        this.gymAdapter = new ListGymAdapter(requireActivity(), this.gyms, (viewHolder, position) -> {});
        recyclerView.setAdapter(gymAdapter);
    }

    // Other methods

    /**
     * Find any gyms from Database and return a callback with status of empty
     *
     * @param onUserCallback callback to init gym
     */
    private void getGymsUIDFromDatabase(OnUserCallback onUserCallback) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Searching gyms...");

        this.db.collection("gyms").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<String> gymsID = new ArrayList<>();

                        if (!task.getResult().getDocuments().isEmpty()) {
                            for (QueryDocumentSnapshot ds : task.getResult()) {
                                gymsID.add(ds.getId());
                            }
                            onUserCallback.addOnCallback(gymsID);
                            onUserCallback.addOnCallback(false);
                        } else {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found");
                            onUserCallback.addOnCallback(true);
                        }
                    } else {
                        AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found");
                        onUserCallback.addOnCallback(true);
                    }
                })
                .addOnFailureListener(task -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Gyms not found");
                    onUserCallback.addOnCallback(true);
                });

    }

    /**
     * This methods transform a String into array of String.
     * To obtain it the origin string is cut into substring without first and last character because they are represented with "[,]".
     * Finally, from substring is deleted all whitespaces using Apache method.
     *
     * @param str origin string that will be used for get and return its respective array
     * @return array of strings obtained from origin string
     */
    private String[] stringToArray(String str) {
        str = str.substring(1, str.length()-1);
        str = StringUtils.deleteWhitespace(str);
        return str.split(",");
    }

}