package com.example.gymfit.gym.main;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymfit.R;
import com.example.gymfit.gym.conf.Gym;
import com.example.gymfit.gym.conf.SubscriberCallback;
import com.example.gymfit.system.conf.recycleview.ItemTouchHelperCallback;
import com.example.gymfit.system.conf.recycleview.OnItemSwipeListener;
import com.example.gymfit.system.conf.recycleview.SubscriberAdapter;
import com.example.gymfit.user.conf.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentGymSubs extends Fragment implements OnItemSwipeListener {
    private static final String DESCRIBABLE_KEY = "describable_key";
    private static final String LOG = "KEY_LOG";
    private static final String DRAWER_INSTANCE = "drawer_instance";

    private SubscriberAdapter subscriberAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private View messageAnchor = null;
    private boolean isVisible;

    private Gym gym = null;
    private final List<User> users = new ArrayList<>();

    private Bundle savedState = null;

    public static FragmentGymSubs newInstance(Gym gym) {
        FragmentGymSubs fragment = new FragmentGymSubs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESCRIBABLE_KEY, gym);
        fragment.setArguments(bundle);

        return fragment;
    }

    // Overrated methods

    @Override
    @Nullable
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
        initInterface(new SubscriberCallback() {
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
                    .addOnFailureListener(task -> Log.d(LOG, Objects.requireNonNull(task.getMessage()))));
            }

            @Override
            public void addOnSuccessCallback(User user) {
                users.add(user);
                subscriberCount++;
                addOnSuccessListener();
            }

            @Override
            public void addOnCompleteCallback() {
                setUpRecycleView();
            }

            @Override
            public void addOnSuccessListener() {
                if (subscriberCount == subscriberSize) {
                    addOnCompleteCallback();
                }
            }

        });

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
                        subscriberAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
                break;
            case R.id.app_bar_filter:
                subscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_default));
                break;
            case R.id.action_filter_monthly:
                subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_monthly));
                break;
            case R.id.action_filter_quarterly:
                subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_quarterly));
                break;
            case R.id.action_filter_six_month:
                subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_six_month));
                break;
            case R.id.action_filter_annual:
                subscriberAdapter.getFilterSub().filter(getResources().getString(R.string.prompt_annual));
                break;
            case R.id.action_sort_by_name:
                subscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_name));
                break;
            case R.id.action_sort_by_surname:
                subscriberAdapter.getSort().filter(getResources().getString(R.string.prompt_surname));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int position) {
        String username = this.users.get(position).getUsername();

        // Backup before delete for undo action
        final User item = this.users.get(position);

        // Remove the item from recycleView
        this.subscriberAdapter.removeItem(position);
        setMessage(username + " " + getResources().getString(R.string.message_user_removing))
            .setAction(getResources().getString(R.string.prompt_cancel), v -> this.subscriberAdapter.restoreItem(item, position))
            .setActionTextColor(requireContext().getColor(R.color.tint_message_text))
            .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        Log.d(LOG, "User " + item.getUsername() + " is removed from UserAdapter");
                        removeUserFromGym(item.getUid());
                    } else if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        Log.d(LOG, "User " + item.getUsername() + " is restored into UserAdapter");
                    }
                }
            })
            .show();

    }

    // Set methods

    private void initInterface(SubscriberCallback subscriberCallback) {

        // TODO: take Array of Subs from Gym and if it's empty take it from DB
        this.db.collection("gyms").document(this.gym.getUid()).get()
            .addOnCompleteListener(task -> {

                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;

                    String[] subscribersUID = stringToArray(Objects.requireNonNull(documentSnapshot.get("subscribers")).toString());
                    subscriberCallback.addOnCallback(Arrays.asList(subscribersUID));
                }
            })
            .addOnFailureListener(task -> Log.d(LOG, Objects.requireNonNull(task.getMessage())));

    }

    private void setUpRecycleView() {
        RecyclerView recyclerView = requireView().getRootView().findViewById(R.id.rv_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        this.subscriberAdapter = new SubscriberAdapter(requireActivity(), this.users, (viewHolder, position) -> {
            isListVisibility(
                    viewHolder.itemView.findViewById(R.id.content_toggle_container),
                    viewHolder.itemView.findViewById(R.id.end_icon),
                    isVisible);
            isVisible = !isVisible;
        });
        recyclerView.setAdapter(subscriberAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    /**
     * Set the container anchor for Snackbar object and its methods "make"
     *
     * @param rootView Root View object of Fragment. From it can be get the context.
     */
    private void setMessageAnchor(View rootView) {
        // Initialize the container that will be used for Snackbar methods
        this.messageAnchor = rootView.findViewById(R.id.anchor);
    }

    /**
     * Show a message on the screen
     *
     * @param text string to show in SnackBar method
     * @return SnackBar;
     */
    private Snackbar setMessage(String text) {
        return Snackbar.make(this.messageAnchor, text, Snackbar.LENGTH_SHORT);
    }

    // Interface methods

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

    private void closeFragment() {
        getChildFragmentManager().popBackStack();
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
                .addOnCompleteListener(v -> Log.d(LOG, "User is removed from Gym node database"))
                .addOnFailureListener(v -> Log.d(LOG, "User is not removed from Gym node database"));

            // remove gym from user db node and clear all his turns
            this.db.collection("users").document(uid)
                .update("subscription", null,
                        "turns", null)
                .addOnCompleteListener(v -> Log.d(LOG, "Gym and all Users' turns are removed from user node database"))
                .addOnFailureListener(v -> Log.d(LOG, "Gym and all Users' turns are not removed from user node database"));
        } else {
            Log.d(LOG, "User is removed from gym node database");
        }
    }

    // Log

    private void addLog(StackTraceElement[] stackTrace, String text) {
        int lineNumber = stackTrace[2].getLineNumber();
        String methodName = stackTrace[2].getMethodName();
        String className = stackTrace[2].getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);

        String message = className + " " + methodName + " " + "[" + lineNumber + "]: " + text;
        Log.d(LOG, message);
    }

}