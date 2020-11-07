package com.example.gymfit.user.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.OnUserCallback;
import com.example.gymfit.system.conf.recycleview.ItemTouchHelperRecycleCallback;
import com.example.gymfit.system.conf.recycleview.ListGymAdapter;
import com.example.gymfit.system.conf.recycleview.OnItemSwipeListener;
import com.example.gymfit.system.conf.recycleview.OnUserSubscriptionResultCallback;
import com.example.gymfit.system.conf.recycleview.ListUserSubscribedAdapter;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.system.conf.utils.DatabaseUtils;
import com.example.gymfit.system.conf.utils.ResourceUtils;
import com.example.gymfit.user.conf.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUserListGyms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUserListGyms extends Fragment implements OnItemSwipeListener {
    private static final int MY_GPS_TRACKER_CODE = 10;
    private static final String USER_KEY = "user_key";

    private ListGymAdapter listGymAdapter;
    private ListUserSubscribedAdapter listUserSubscribedAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String[] collections = ResourceUtils.getStringArrayFromID(R.array.collections);

    private View messageAnchor = null;
    private final Map<String, Boolean> viewVisibility = new HashMap<>();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If there are some subscribers so enable actions of new menu, otherwise keep them disabled (no effects on recycle) to avoid exception
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
                            listGymAdapter.getFilter().filter(newText);
                            return false;
                        }
                    });
                    break;
                case R.id.action_sort_by_name:
                    listGymAdapter.getSort().filter(getResources().getString(R.string.prompt_name));
                    break;
                case R.id.action_filter_10:
                    listGymAdapter.setCurrentLocation(getLastKnownLocation());
                    listGymAdapter.getMenuFilter().filter(getResources().getString(R.string.filter_by_distance_10));
                    break;
                case R.id.action_filter_25:
                    listGymAdapter.setCurrentLocation(getLastKnownLocation());
                    listGymAdapter.getMenuFilter().filter(getResources().getString(R.string.filter_by_distance_25));
                    break;
                case R.id.action_filter_50:
                    listGymAdapter.setCurrentLocation(getLastKnownLocation());
                    listGymAdapter.getMenuFilter().filter(getResources().getString(R.string.filter_by_distance_50));
                    break;
                default:
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ListGymAdapter.MyViewHolder) {
            final String name = this.gyms.get(position).getName();
            final String uid = this.gyms.get(position).getUid();
            final Gym item = this.gyms.get(position);

            if (!this.user.getSubscription()[0].equals("null")) {
                AppUtils.log(Thread.currentThread().getStackTrace(), this.user.getFullname() + " has already a Gym subscription: " + this.user.getSubscription()[0]);
                AppUtils.message(this.messageAnchor, getString(R.string.recycleview_already_subscribed), Snackbar.LENGTH_SHORT).show();
                this.listGymAdapter.notifyItemChanged(position);
            } else {
                createSubscriptionDialog(item, result -> {
                    // Restore same item in the same position for abord action
                    if (result == null) {
                        this.listGymAdapter.notifyItemChanged(position);
                        AppUtils.log(Thread.currentThread().getStackTrace(), this.user.getFullname() + " abort action");
                    } else {
                        // Backup gym item to restore
                        this.listGymAdapter.removeItem(position);
                        AppUtils.message(this.messageAnchor, getString(R.string.recycleview_user_subscribing) + name, Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.prompt_cancel), v -> this.listGymAdapter.restoreItem(item, position))
                                .setActionTextColor(requireContext().getColor(R.color.tint_message_text))
                                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        super.onDismissed(transientBottomBar, event);
                                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                            AppUtils.log(Thread.currentThread().getStackTrace(), "Gym " + item.getName() + " is removed from GymAdapter");
                                            setUpSubscribedRecycleView(item);
                                            addGymIntoUser(uid, name, result);
                                        } else if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                            AppUtils.log(Thread.currentThread().getStackTrace(), "Gym " + item.getName() + " is restored into GymAdapter");
                                        }
                                    }
                                })
                                .show();
                    }
                });
            }
        } else if (viewHolder instanceof ListUserSubscribedAdapter.MyViewHolder) {
            DatabaseUtils.getGymByUID(this.user.getSubscription()[0], item -> createUnsubscribeDialog(item, result -> {
                // Restore same item in the same position for abort action
                if (result == null) {
                    this.listUserSubscribedAdapter.notifyItemChanged(position);
                    AppUtils.log(Thread.currentThread().getStackTrace(), "Abort unsubscribe of: " + item.getName());
                } else {
                    // Backup gym item to restore
                    this.listUserSubscribedAdapter.removeItem(position);

                    // Show message for user to restore or unsubscribe from current Gym
                    AppUtils.message(this.messageAnchor, getString(R.string.recycleview_user_unsubscribed) + item.getName(), Snackbar.LENGTH_SHORT)
                            .setAction(getResources().getString(R.string.prompt_cancel), v -> this.listUserSubscribedAdapter.restoreItem(item, position))
                            .setActionTextColor(requireContext().getColor(R.color.tint_message_text))
                            .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    super.onDismissed(transientBottomBar, event);
                                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                        // Add Gym into Gym list for Recycle
                                        listGymAdapter.addItem(item);
                                        // Remove subscribed Gym from User and Gym node Database
                                        removeGymIntoUser(item);

                                        AppUtils.log(Thread.currentThread().getStackTrace(), item.getName() + " is removed from UserSubscriberAdapter, Database and User");
                                    } else if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                        AppUtils.log(Thread.currentThread().getStackTrace(), item.getName() + " is restored into UserSubscriberAdapter");
                                    }
                                }
                            })
                            .show();
                }
            }));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_GPS_TRACKER_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppUtils.log(Thread.currentThread().getStackTrace(), "GPS permission granted from ListGymAdapter");
            } else {
                AppUtils.log(Thread.currentThread().getStackTrace(), "GPS permission not granted ListGymAdapter");
            }
        }
    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(@NonNull View rootView) {
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

        if (!this.user.getSubscription()[0].equals("null")) {
            // Get from Database subscribed Gym for current User
            DatabaseUtils.getGymByUID(this.user.getSubscription()[0], this::setUpSubscribedRecycleView);
            AppUtils.log(Thread.currentThread().getStackTrace(), "Subscribed Gym cardview is created and shown into interface");
        }
        getGymsUIDFromDatabase(new OnUserCallback() {
            int gymsCount = 0;
            int gymsSize = 0;

            @Override
            public void addOnCallback(List<String> usersID) {
                gymsSize = usersID.size();
                usersID.forEach(userID -> DatabaseUtils.getGymByUID(userID, this::addOnSuccessCallback));
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

    private void setUpSubscribedRecycleView(@NonNull Gym gym) {
        RecyclerView listUserSubscriptionRecycle = requireView().getRootView().findViewById(R.id.rv_subscribed);
        listUserSubscriptionRecycle.setHasFixedSize(false);
        listUserSubscriptionRecycle.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        this.listUserSubscribedAdapter = new ListUserSubscribedAdapter(requireActivity(), gym, (viewHolder, position) ->
                isListVisibility(
                        viewHolder.itemView.findViewById(R.id.m_content_toggle_container),
                        viewHolder.itemView.findViewById(R.id.m_end_icon),
                        gym.getUid()));
        listUserSubscriptionRecycle.setAdapter(this.listUserSubscribedAdapter);

        final ItemTouchHelper.Callback callback = new ItemTouchHelperRecycleCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listUserSubscriptionRecycle);
    }

    private void setUpRecycleView() {
        RecyclerView listGymRecycle = requireView().getRootView().findViewById(R.id.rv_gyms);
        listGymRecycle.setHasFixedSize(false);
        listGymRecycle.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        this.listGymAdapter = new ListGymAdapter(requireActivity(), this.gyms, (viewHolder, position) ->
                isListVisibility(
                        viewHolder.itemView.findViewById(R.id.content_toggle_container),
                        viewHolder.itemView.findViewById(R.id.end_icon),
                        this.gyms.get(position).getUid()
                    ));
        listGymRecycle.setAdapter(this.listGymAdapter);

        final ItemTouchHelper.Callback callback = new ItemTouchHelperRecycleCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listGymRecycle);
    }

    // Other methods

    private void isListVisibility(@NonNull final LinearLayout container, @NonNull final ImageView arrow, @NonNull final String viewName) {

        // reaction of null pointer with a new creation of card visibility
        if (!this.viewVisibility.containsKey(viewName)) {
            this.viewVisibility.put(viewName, false);
        }

        // if card selected is not in visible mode so enable it and replace its state, icon and layout height
        if (!this.viewVisibility.get(viewName)) {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up));
            AppUtils.expandCard(container);
            this.viewVisibility.replace(viewName, true);
        } else {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down));
            AppUtils.collapseCard(container);
            this.viewVisibility.replace(viewName, false);
        }
    }

    /**
     * Create and set a Material dialog to show which are current Gym' subscriptions.
     * Return item checked if positive, otherwise nothing
     *
     * @param gym Gym object current of swipe event from recycle Adapter
     */
    private void createSubscriptionDialog(@NonNull Gym gym, OnUserSubscriptionResultCallback callback) {
        List<String> subscriptionList = new ArrayList<>();
        gym.getTranslatedSubscriptions().forEach((key, isAvailable) -> {
            if (isAvailable) {
                subscriptionList.add(key);
            }
        });

        if (subscriptionList.isEmpty()) {
            callback.onCallback(null);
        } else {
            final String[] subscriptionArray = subscriptionList.toArray(new String[0]);
            final String[] result = {subscriptionArray[0]};

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle(getString(R.string.prompt_subscribe));
            builder.setSingleChoiceItems(subscriptionArray, 0, (dialog, which) -> result[0] = subscriptionArray[which]);
            builder.setPositiveButton(getString(R.string.prompt_subscribe), (dialog, which) -> callback.onCallback(result[0]));
            builder.setNegativeButton(getString(R.string.prompt_cancel), (dialog, which) -> callback.onCallback(null));
            builder.setOnCancelListener(dialog -> callback.onCallback(null));
            builder.create();
            builder.show();

        }
    }

    /**
     * Create and set a Material dialog to show which are current Gym' subscriptions.
     * Return item checked if positive, otherwise nothing
     *
     * @param gym Gym object current of swipe event from recycle Adapter
     */
    private void createUnsubscribeDialog(@NonNull Gym gym, OnUserSubscriptionResultCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.prompt_unsubscribe));
        final String placeholder = getString(R.string.recycleview_user_unsubscribing) + " " + gym.getName() + " ?";
        builder.setMessage(placeholder);
        builder.setPositiveButton(getString(R.string.prompt_unsubscribe), (dialog, which) -> callback.onCallback(Boolean.toString(true)));
        builder.setNegativeButton(getString(R.string.prompt_cancel), (dialog, which) -> callback.onCallback(null));
        builder.setOnCancelListener(dialog -> callback.onCallback(null));
        builder.create();
        builder.show();
    }

    /**
     * Try to find a current GPS coordinate location and return their lat lng
     *
     * @return current location of User
     */
    @NonNull
    private LatLng getLastKnownLocation() {
        final LocationManager mLocationManager = (LocationManager) requireActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        final List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {

            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, MY_GPS_TRACKER_CODE);
            }

            final Location location = mLocationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = location;
            }
        }

        return new LatLng(
                bestLocation != null ? bestLocation.getLatitude() : 0,
                bestLocation != null ? bestLocation.getLongitude() : 0);
    }

    // Database methods

    /**
     * Find any gyms from Database and return a callback with status of empty
     *
     * @param onUserCallback callback to init gym
     */
    private void getGymsUIDFromDatabase(OnUserCallback onUserCallback) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Searching gyms...");

        this.db.collection(collections[1]).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final List<String> gymsID = new ArrayList<>();

                        if (!task.getResult().getDocuments().isEmpty()) {
                            for (QueryDocumentSnapshot ds : task.getResult()) {
                                // Take all Gym document from Database without UID already subscribed for current User
                                if (!ds.getId().equals(this.user.getSubscription()[0])) {
                                    gymsID.add(ds.getId());
                                }
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
     * Find and set User document with current Gym UID and add current User UID into respective Gym document
     *
     * @param uid current Gym UID of recycleview. Used to find respective Database document and set new subscribers
     * @param name current Gym name of recycleview. Used into Log and Snackbar message
     * @param result current subscription key resource to add into Gym document
     */
    private void addGymIntoUser(@NonNull final String uid, @NonNull final String name, @NonNull final String result) {
        final String subscriptionKey = Gym.getSubscriptionFromTranslated(result);
        // Add Gym into User object
        this.user.setSubscription(new String[] {
                Objects.requireNonNull(uid), Objects.requireNonNull(subscriptionKey)
        });

        // Add Gym into User Database
        final String[] userKeys = getResources().getStringArray(R.array.user_field);
        this.db.collection(collections[0]).document(this.user.getUid())
                .update(userKeys[10], FieldValue.arrayUnion(uid, subscriptionKey))
                .addOnSuccessListener(task -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(),"Gym is added into User node database");
                    AppUtils.message(this.messageAnchor, getString(R.string.recycleview_user_subscribed) + Objects.requireNonNull(name), Snackbar.LENGTH_LONG)
                            .show();
                })
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),task.getMessage()));

        // Add User UID into Gym node Database
        final String[] gymKeys = getResources().getStringArray(R.array.gym_field);
        this.db.collection(collections[1]).document(uid)
                .update(gymKeys[8], FieldValue.arrayUnion(this.user.getUid()))
                .addOnSuccessListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),"Current User is added into Gym subscribers node database"))
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),task.getMessage()));
    }

    /**
     * Find and remove User document with current Gym UID and add current User UID into respective Gym document
     *
     * @param gym current Gym of recycleview. Used to find respective Database document and set new subscribers
     */
    private void removeGymIntoUser(@NonNull final Gym gym) {
        // Remove Gym into User object
        this.user.setSubscription(new String[] {
                "null", "null"
        });

        // Remove Gym into User Database
        final String[] userKeys = getResources().getStringArray(R.array.user_field);
        this.db.collection(collections[0]).document(this.user.getUid())
                .update(userKeys[10], null)
                .addOnSuccessListener(task -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(),"Gym is removed into User node database");
                    AppUtils.message(this.messageAnchor, getString(R.string.recycleview_user_unsubscribed) + Objects.requireNonNull(gym.getName()), Snackbar.LENGTH_LONG)
                            .show();
                })
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),task.getMessage()));

        // Remove User UID into Gym node Database
        final String[] gymKeys = getResources().getStringArray(R.array.gym_field);
        this.db.collection(collections[1]).document(gym.getUid())
                .update(gymKeys[8], FieldValue.arrayRemove(this.user.getUid()))
                .addOnSuccessListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),"Current User is removed from Gym subscribers node database"))
                .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),task.getMessage()));

        // Null Gym node Databse is empty
        DatabaseUtils.isGymSubscribersEmpty(gym.getUid(), result -> {
            if (result) {
                this.db.collection(collections[1]).document(gym.getUid())
                        .update(gymKeys[8], null)
                        .addOnSuccessListener(task -> AppUtils.log(Thread.currentThread().getStackTrace()," Gym subscribers node database is clear"))
                        .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(),task.getMessage()));
            }
        });
    }

}