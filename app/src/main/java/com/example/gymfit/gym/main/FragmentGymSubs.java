package com.example.gymfit.gym.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.system.conf.GenericUser;
import com.example.gymfit.system.conf.OnUserCallback;
import com.example.gymfit.system.conf.recycleview.ItemTouchHelperCallback;
import com.example.gymfit.system.conf.recycleview.OnItemSwipeListener;
import com.example.gymfit.system.conf.recycleview.ListSubscriberAdapter;
import com.example.gymfit.system.conf.utils.AppUtils;
import com.example.gymfit.user.conf.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentGymSubs extends Fragment implements OnItemSwipeListener {
    private static final String GYM_KEY = "gym_key";

    private ListSubscriberAdapter listSubscriberAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private View messageAnchor = null;
    private boolean isVisible;
    private boolean isEmptyData = false;

    private Gym gym = null;
    private final List<User> users = new ArrayList<>();

    public static FragmentGymSubs newInstance(Gym gym) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Instance of FragmentGymSubs created");

        FragmentGymSubs fragment = new FragmentGymSubs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(GYM_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.gym = (Gym) getArguments().getSerializable(GYM_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_gym_subs, container, false);

        initSystemInterface(rootView);
        initInterface(new OnUserCallback() {
            int subscriberCount = 0;
            int subscriberSize = 0;

            @Override
            public void addOnCallback(List<String> usersID) {
                subscriberSize = usersID.size();

                usersID.forEach(userID -> db.collection("users").document(userID).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;

                            String uid = documentSnapshot.getString("uid");
                            String name = documentSnapshot.getString("name");
                            String surname = documentSnapshot.getString("surname");
                            String phone = documentSnapshot.getString("phone");
                            String img = documentSnapshot.getString("img");
                            String email = documentSnapshot.getString("email");
                            String[] subscription = stringToArray(Objects.requireNonNull(documentSnapshot.get("subscription")).toString());
                            String gender = documentSnapshot.getString("gender");

                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> turns = (List<Map<String, Object>>) documentSnapshot.get("turns");
                            User user = new User(name, surname, phone, email, gender, uid, img, subscription[1], turns);

                            addOnSuccessCallback(user);
                        }
                    })
                    .addOnFailureListener(task -> AppUtils.log(Thread.currentThread().getStackTrace(), Objects.requireNonNull(task.getMessage()))));
            }

            @Override
            public void addOnCallback(boolean isEmpty) {}

            @Override
            public <T extends GenericUser> void addOnSuccessCallback(T user) {
                users.add((User) user);
                subscriberCount++;
                addOnSuccessListener();
            }

            @Override
            public void addOnSuccessListener() {
                if (subscriberCount == subscriberSize) {
                    setUpRecycleView();
                }
            }

        });

        AppUtils.log(Thread.currentThread().getStackTrace(), "FragmentGymSubs layout XML created");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gym_subs_toolbar, menu);
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
                            listSubscriberAdapter.getFilter().filter(newText);
                            return false;
                        }
                    });
                    break;
                case R.id.app_bar_filter:
                    listSubscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_default));
                    break;
                case R.id.action_filter_monthly:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_monthly));
                    break;
                case R.id.action_filter_quarterly:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_quarterly));
                    break;
                case R.id.action_filter_six_month:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_six_month));
                    break;
                case R.id.action_filter_annual:
                    listSubscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_annual));
                    break;
                case R.id.action_sort_by_name:
                    listSubscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_name));
                    break;
                case R.id.action_sort_by_surname:
                    listSubscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_surname));
                    break;
                default:
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int position) {
        String username = this.users.get(position).getFullname();

        // Backup before delete for undo action
        final User item = this.users.get(position);

        // Remove the item from recycleView
        this.listSubscriberAdapter.removeItem(position);
        AppUtils.message(this.messageAnchor, username + " " + getResources().getString(R.string.message_user_removing), Snackbar.LENGTH_SHORT)
            .setAction(getResources().getString(R.string.prompt_cancel), v -> this.listSubscriberAdapter.restoreItem(item, position))
            .setActionTextColor(requireContext().getColor(R.color.tint_message_text))
            .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        AppUtils.log(Thread.currentThread().getStackTrace(),"User " + item.getFullname() + " is removed from UserAdapter");
                        removeUserFromGym(item.getUid());
                    } else if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        AppUtils.log(Thread.currentThread().getStackTrace(),"User " + item.getFullname() + " is restored into UserAdapter");
                    }
                }
            })
            .show();

    }

    // Interface methods

    /**
     * Initialize toolbar option and title, Snackbar anchor, gym ID and default screen orientation
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void initSystemInterface(View rootView) {
        // init new checked item on navigation Drawer
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_gym);
        navigationView.getMenu().findItem(R.id.nav_menu_subs).setChecked(true);

        // Abilities toolbar item options
        setHasOptionsMenu(true);
        // Change toolbar title
        requireActivity().setTitle(getResources().getString(R.string.gym_subs_toolbar_title));

        // init message Anchor for Snackbar
        this.messageAnchor = rootView.findViewById(R.id.anchor);

        AppUtils.log(Thread.currentThread().getStackTrace(), "System interface of FragmentGymSubs initialized");
    }

    /**
     * Try to find any subscribers to init Recycle or show a alert message if they aren't for this gym
     *
     * @param onUserCallback callback to init recycleView
     */
    private void initInterface(OnUserCallback onUserCallback) {
        this.isEmptyData = false;

        if (!this.gym.getSubscribers().isEmpty()) {
            onUserCallback.addOnCallback(this.gym.getSubscribers());
        } else {
            getSubsUIDFromDatabase(new OnUserCallback() {
                @Override
                public void addOnCallback(boolean isEmpty) {
                    if (!isEmpty) {
                        onUserCallback.addOnCallback(gym.getSubscribers());
                    } else {
                        isEmptyData = true;
                        AppUtils.log(Thread.currentThread().getStackTrace(), "No subscribers for this gym");

                        Snackbar message = AppUtils.message(messageAnchor, getString(R.string.recycleview_subs_void), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.prompt_update), v -> {})
                                .setActionTextColor(ResourcesCompat.getColor(getResources(), R.color.tint_message_text, null));
                        Button actionBtn = message.getView().findViewById(com.google.android.material.R.id.snackbar_action);
                        actionBtn.setOnClickListener(v -> getSubsUIDFromDatabase(new OnUserCallback() {

                            @Override
                            public void addOnCallback(boolean isEmpty) {
                                if (!isEmpty) {
                                    isEmptyData = false;
                                    message.dismiss();
                                    onUserCallback.addOnCallback(gym.getSubscribers());
                                }
                            }

                            @Override
                            public void addOnCallback(List<String> usersID) {}

                            @Override
                            public <T extends GenericUser> void addOnSuccessCallback(T user) {}

                            @Override
                            public void addOnSuccessListener() {}
                        }));
                        message.show();
                    }
                }

                @Override
                public void addOnCallback(List<String> usersID) {}

                @Override
                public <T extends GenericUser> void addOnSuccessCallback(T user) {}

                @Override
                public void addOnSuccessListener() {}
            });
        }
    }

    /**
     * Find any subscribers from Database and return a callback with status of empty
     *
     * @param onUserCallback callback to init gym subscribers
     */
    private void getSubsUIDFromDatabase(OnUserCallback onUserCallback) {
        AppUtils.log(Thread.currentThread().getStackTrace(), "Searching new subscribers...");

        this.db.collection("gyms").document(this.gym.getUid()).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.get("subscribers") != null) {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "New subscribers found: " + documentSnapshot.get("subscribers").toString());
                            List<String> subscribersUID = new LinkedList<>(Arrays.asList(stringToArray(documentSnapshot.get("subscribers").toString())));
                            this.gym.setSubscribers(subscribersUID);
                            onUserCallback.addOnCallback(false);
                        } else {
                            AppUtils.log(Thread.currentThread().getStackTrace(), "New subscribers not found");
                            onUserCallback.addOnCallback(true);
                        }
                    }
                })
                .addOnFailureListener(task -> {
                    AppUtils.log(Thread.currentThread().getStackTrace(), "New subscribers not found");
                    onUserCallback.addOnCallback(true);
                });
    }

    private void setUpRecycleView() {
        RecyclerView recyclerView = requireView().getRootView().findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        this.listSubscriberAdapter = new ListSubscriberAdapter(requireActivity(), this.users, (viewHolder, position) -> {
            isListVisibility(
                    viewHolder.itemView.findViewById(R.id.content_toggle_container),
                    viewHolder.itemView.findViewById(R.id.end_icon),
                    isVisible);
            isVisible = !isVisible;
        });
        recyclerView.setAdapter(listSubscriberAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    // Other methods

    private String[] stringToArray(String str) {
        str = str.substring(1, str.length()-1);
        str = StringUtils.deleteWhitespace(str);
        return str.split(",");
    }

    private void isListVisibility(LinearLayout container, ImageView arrow, boolean isVisible) {

        if (!isVisible) {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up));
            expandCard(container);
        } else {
            arrow.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down));
            collapseCard(container);
        }
    }

    private static void expandCard(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(300);
        v.startAnimation(a);
    }

    private static void collapseCard(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms: (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)
        a.setDuration(150);
        v.startAnimation(a);
    }

    // Database methods

    private void removeUserFromGym(String uid) {
        List<String> usersUID;

        if (!this.gym.getSubscribers().isEmpty() && this.gym.getSubscribers().contains(uid)) {
            // remove this user from gym object
            this.gym.removeSubscriber(uid);
            usersUID = this.gym.getSubscribers();

            // remove this user from gym db node
            this.db.collection("gyms").document(this.gym.getUid())
                .update("subscribers", usersUID)
                .addOnCompleteListener(v -> AppUtils.log(Thread.currentThread().getStackTrace(),"User is removed from Gym node database"))
                .addOnFailureListener(v -> AppUtils.log(Thread.currentThread().getStackTrace(),"User is not removed from Gym node database"));

            // remove gym from user db node and clear all his turns
            this.db.collection("users").document(uid)
                .update("subscription", null,
                        "turns", null)
                .addOnCompleteListener(v -> AppUtils.log(Thread.currentThread().getStackTrace(),"Gym and all Users' turns are removed from user node database"))
                .addOnFailureListener(v -> AppUtils.log(Thread.currentThread().getStackTrace(),"Gym and all Users' turns are not removed from user node database"));
        } else {
            AppUtils.log(Thread.currentThread().getStackTrace(),"User is removed from gym node database");
        }
    }

}